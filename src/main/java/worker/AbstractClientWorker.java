package worker;

import com.google.common.collect.Maps;
import common.ClientUploadStatus;
import common.FileHandlerHelper;
import common.RetryStrategyEnum;
import common.ThreadPoolManager;
import config.ConfigDataHelper;
import cons.BusinessConstant;
import cons.CommonConstant;
import handler.AbstractUploadFileProgressCallback;
import handler.FileUploadExceptionRetryStrategy;
import handler.RetryStrategy;
import handler.ShowUploadProgressSpeedProgressCallback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.util.AllocateSourcesUtils;
import org.apache.thrift.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTransferWorker;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 客户端处理文件上传、下载服务类
 */
public abstract class AbstractClientWorker extends AbstractUploadFileProgressCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientWorker.class);
    protected ExecutorService parallelUploadExecutor = ThreadPoolManager.getClientParallelUploadFileNumExecutorService();
    protected boolean shouldShowUploadSpeed = Boolean.parseBoolean(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.SHOW_CLIENT_UPLOAD_SPEED_SWITCH));
    /**
     * 标志位，标记{@link #showUploadProgressSpeedProgressCallback}是否已添加
     */
    protected boolean speedListenerAppend = false;
    private ScheduledExecutorService measureUploadRateScheduler = ThreadPoolManager.getClientAcquireUploadSpeedScheduler();
    private Future showUploadSpeedFuture;
    /**
     * 打印文件上传进度
     */
    protected ShowUploadProgressSpeedProgressCallback showUploadProgressSpeedProgressCallback = new ShowUploadProgressSpeedProgressCallback();
    /**
     * 重试策略
     */
    protected RetryStrategy retryStrategy = new FileUploadExceptionRetryStrategy();
    /**
     * 一些hook调用，文件上传完成或者失败时候一些资源的释放，比如io的释放
     */
    protected Map<String, Runnable> hookMap = Maps.newConcurrentMap();

    public AbstractClientWorker(String terminalType) {
        this(terminalType, null);
    }

    public <B, T> AbstractClientWorker(String terminalType, RetryStrategy<B, T> retryStrategy) {
        super(terminalType);
        if (shouldShowUploadSpeed) {
            showUploadSpeedFuture = measureUploadRateScheduler.scheduleAtFixedRate(showUploadProgressSpeedProgressCallback::showUploadSpeedInfo, 1, 1, TimeUnit.SECONDS);
        }
        if (retryStrategy != null) {
            this.retryStrategy = retryStrategy;
        }
    }

    /**
     * 客户端处理完成之后，释放资源
     */
    public void shutdown() {
        if (showUploadSpeedFuture != null) {
            showUploadSpeedFuture.cancel(true);
        }
        ExecutorUtil.gracefulShutdown(measureUploadRateScheduler, 1000);
        ExecutorUtil.gracefulShutdown(parallelUploadExecutor, 1000);
    }

    protected FileUploadRequest constructFileUploadRequest(String saveParentPath, String relativePath, File file, long startPos, RandomAccessFile accessFile) throws IOException {
        FileUploadRequest request = new FileUploadRequest();
        request.setFileName(file.getName());
        request.setSaveParentFolder(saveParentPath);
        request.setRelativePath(relativePath);
        String wholeFilePath = FileHandlerHelper.generateWholePath(saveParentPath, relativePath, file.getName());
        //目录和文件的标识不同，目录只需要完全路径即可，文件需要添加上文件字节大小
        if (file.isDirectory()) {
            request.setIdentifier(FileHandlerHelper.generateUniqueIdentifier(wholeFilePath));
            request.setFileType(FileTypeEnum.DIR_TYPE);
            request.setCheckSum("0L");
        } else {
            request.setIdentifier(FileHandlerHelper.generateUniqueIdentifier(wholeFilePath + CommonConstant.UNDERLINE + file.length()));
            request.setFileType(FileTypeEnum.FILE_TYPE);
            request.setStartPos(startPos);
            request.setTotalFileLength(file.length());
            int perUploadSegmentLength = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH));
            byte[] segmentContents = new byte[perUploadSegmentLength];
            accessFile.seek(startPos);
            int readBytesLength = accessFile.read(segmentContents);
            request.setContents(segmentContents);
            request.setBytesLength(readBytesLength);
            request.setCheckSum(FileHandlerHelper.generateContentsCheckSum(segmentContents, readBytesLength));
        }
        return request;
    }

    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    /**
     * 上传单个文件或者文件夹
     *
     * @param saveParentPath
     * @param relativePath
     * @param uploadSingleFileOrDir
     * @param client
     * @return
     */
    public abstract ClientUploadStatus doUploadSingleFile(String saveParentPath,
                                                          String relativePath, File uploadSingleFileOrDir,
                                                          FileTransferWorker.Client client, FileUploadRequest[] fileUploadRequests) throws Exception;

    /**
     * 连接探活
     *
     * @param remoteHost
     * @param remotePort
     * @param connectionTimeout
     * @return
     */

    public boolean detectConnection(String remoteHost, int remotePort, int connectionTimeout) {
        RemoteRpcNode remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
        boolean createConn = remoteRpcNode.createRemoteConnectionIfNotExists();
        if (!createConn) {
            LOGGER.warn("failed to create remote connection||host={}||port={}", remoteHost, remotePort);
            return false;
        }
        remoteRpcNode.destroyConnection();
        return true;
    }

    @Override
    public void onFileUploadFinish(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        super.onFileUploadFinish(fileIdentifier, filePath, fileType);
        hookMap.getOrDefault(fileIdentifier, () -> {
        }).run();
    }

    @Override
    public void onFileUploadFail(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        super.onFileUploadFail(fileIdentifier, filePath, fileType);
        hookMap.getOrDefault(fileIdentifier, () -> {
        }).run();
    }

    /**
     * 文件或者文件夹上传上传
     *
     * @param saveParentPath 服务端保存的路径，绝对路径
     * @param file
     * @param remoteHost
     * @param remotePort
     * @return
     */
    public final void clientUploadFile(String saveParentPath, File file, String remoteHost, int remotePort, int connectionTimeout) {

        if (!file.exists() || !file.canExecute()) {
            throw new RuntimeException("file path not exists or no execute permission");
        }
        if (!detectConnection(remoteHost, remotePort, connectionTimeout)) {
            return;
        }
        if (shouldShowUploadSpeed && !speedListenerAppend) {
            synchronized (this) {
                if (!speedListenerAppend) {
                    addUploadProgressFileCallback(showUploadProgressSpeedProgressCallback);
                    speedListenerAppend = true;
                }
            }
        }
        Collection<File> fileLists = new ArrayList<>();
        if (file.isFile()) {
            fileLists.add(file);
        } else {
            fileLists = FileUtils.listFilesAndDirs(file, TrueFileFilter.TRUE, DirectoryFileFilter.DIRECTORY);
        }
        //并发上传文件数量
        int maxParallelUploadFileNum = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM));
        String rootPath = file.getAbsolutePath();
        //根目录或者文件路径
        String rootFileName = file.getName();
        String parentFilePath = StringUtils.isBlank(file.getParent()) ? "" : file.getParent();
        List<List<File>> splitFileArrayList = AllocateSourcesUtils.averageSource(new ArrayList(fileLists), maxParallelUploadFileNum);
        CountDownLatch countDownLatch = new CountDownLatch(maxParallelUploadFileNum);
        for (List<File> subFileList : splitFileArrayList) {
            parallelUploadExecutor.execute(() -> {
                RemoteRpcNode remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
                try {
                    for (int i = 0; i < subFileList.size(); i++) {
                        if (!remoteRpcNode.createRemoteConnectionIfNotExists()) {
                            return;
                        }
                        File uploadFile = subFileList.get(i);
                        if (uploadFile.isFile() && uploadFile.length() <= 0L) {
                            LOGGER.warn("file lack content,ignore||filePath={}", uploadFile.getAbsolutePath());
                            continue;
                        }
                        FileTransferWorker.Client client = remoteRpcNode.getRemoteClient();
                        String fileAbsolutePath = uploadFile.getAbsolutePath();
                        //获取相对于根目录的相对路径,比如当前根目录是d:/test，对应子目录是d:/test/nice/mv.mp4，那么相对路径就是test/nice
                        String relativePath = null;
                        if (fileAbsolutePath.length() > rootPath.length()) {
                            //相对路径
                            int rootFilePathSplit = rootFileName.indexOf(parentFilePath) + parentFilePath.length() + 1;
                            relativePath = uploadFile.getParent().substring(rootFilePathSplit).replaceAll(Pattern.quote(CommonConstant.WINDOWS_FILE_SEPARATOR), CommonConstant.LINUX_SHELL_SEPARATOR);
                        }
                        ClientUploadStatus clientUploadStatus = ClientUploadStatus.FAIL;
                        FileUploadRequest[] fileUploadRequests = new FileUploadRequest[1];
                        RetryStrategyEnum retryStrategyEnum = RetryStrategyEnum.ABORT;
                        Function<FileUploadRequest, String> function = FileUploadRequest::getIdentifier;
                        try {
                            clientUploadStatus = doUploadSingleFile(saveParentPath, relativePath, uploadFile, client, fileUploadRequests);
                            //正常上传失败，走正常重试策略
                            if (clientUploadStatus != ClientUploadStatus.UPLOAD_FINISH) {
                                retryStrategyEnum = retryStrategy.doRetryOrNot(fileUploadRequests[0], function, clientUploadStatus);
                            }
                        } catch (Exception e) {
                            LOGGER.error("transport exception", e);
                            clientUploadStatus = ClientUploadStatus.FAIL;
                            retryStrategyEnum = retryStrategy.doRetryOrNot(fileUploadRequests, function, e);
                        }
                        boolean goOn = true;
                        boolean terminalAll = false;
                        switch (retryStrategyEnum) {
                            case SIMPLE_RETRY:
                                i--;
                                goOn = true;
                                break;
                            case TERMINATE_ALL:
                                terminalAll = true;
                                break;
                            case RECREATE_CONNECTION_THEN_ABORT:
                                remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
                                goOn = false;
                                break;
                            case RECREATE_CONNECTION_THEN_RETRY:
                                remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
                                i--;
                                goOn = true;
                                break;
                            case ABORT:
                                break;
                        }

                        String fileIdentifier = fileUploadRequests[0].getIdentifier();
                        FileTypeEnum fileTypeEnum = uploadFile.isFile() ? FileTypeEnum.FILE_TYPE : FileTypeEnum.DIR_TYPE;
                        if (terminalAll) {
                            LOGGER.warn("terminate all uploading file");
                            onFileUploadFail(fileIdentifier, fileAbsolutePath, fileTypeEnum);
                            break;
                        }
                        if (goOn) {
                            continue;
                        }
                        if (clientUploadStatus != ClientUploadStatus.UPLOAD_FINISH) {
                            LOGGER.warn("file {} upload failed||fileIndex={}||totalFileSize={}", fileAbsolutePath, i + 1, subFileList.size());
                            onFileUploadFail(fileIdentifier, fileAbsolutePath, fileTypeEnum);
                        } else {
                            LOGGER.info("file {} upload success||fileIndex={}||totalFileSize={}", fileAbsolutePath, i + 1, subFileList.size());
                            onFileUploadFinish(fileIdentifier, fileAbsolutePath, fileTypeEnum);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("exception when upload", e);
                } finally {
                    if (remoteRpcNode != null) {
                        remoteRpcNode.destroyConnection();
                    }
                    LOGGER.info("release countdown lock");
                    countDownLatch.countDown();
                }
            });
        }
        try {
            LOGGER.info("main thread is wait upload finish");
            countDownLatch.await();
            LOGGER.info("upload finish");
        } catch (InterruptedException e) {
            LOGGER.error("interrupted exception", e);
        } finally {
            shutdown();
        }
    }

}
