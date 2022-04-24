package worker;

import com.google.common.collect.Maps;
import common.*;
import config.ConfigDataHelper;
import cons.BusinessConstant;
import cons.CommonConstant;
import handler.AbstractUploadFileProgressCallback;
import handler.FileUploadExceptionRetryStrategy;
import handler.RetryStrategy;
import handler.TraceUploadProgressSpeedProgressCallback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 客户端处理文件上传、下载服务类
 */
public abstract class AbstractClientWorker extends AbstractUploadFileProgressCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientWorker.class);
    protected ExecutorService parallelUploadExecutor = ThreadPoolManager.getClientParallelUploadFileNumExecutorService();
    protected boolean shouldTraceUploadSpeed = Boolean.parseBoolean(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.TRACE_CLIENT_UPLOAD_SPEED_SWITCH));
    /**
     * 标志位，标记{@link #traceUploadProgressSpeedProgressCallback}是否已添加
     */
    protected boolean speedListenerAppend = false;
    /**
     * 打印文件上传进度
     */
    protected TraceUploadProgressSpeedProgressCallback traceUploadProgressSpeedProgressCallback = new TraceUploadProgressSpeedProgressCallback();
    /**
     * 重试策略
     */
    protected RetryStrategy retryStrategy = new FileUploadExceptionRetryStrategy();
    /**
     * 一些hook调用，文件上传完成或者失败时候一些资源的释放，比如io的释放
     */
    protected Map<String, Runnable> hookMap = Maps.newConcurrentMap();
    //并发上传文件数量
    protected int maxParallelUploadFileNum = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM));
    /**
     * 建立rpc连接最大重试次数
     */
    protected int maxCreateConnectionTryTimes = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.CLIENT_CREATE_CONNECTION_MAX_TRY_TIMES));
    private ScheduledExecutorService measureUploadRateScheduler = ThreadPoolManager.getClientAcquireUploadSpeedScheduler();
    private Future traceUploadSpeedFuture;
    /**
     * 如果上传的是文件夹，过滤上传的文件类型
     */
    private String[] nameFilters;

    public AbstractClientWorker(String terminalType) {
        this(terminalType, null);
    }

    public <B, T> AbstractClientWorker(String terminalType, RetryStrategy<B, T> retryStrategy) {
        super(terminalType);
        if (shouldTraceUploadSpeed) {
            traceUploadSpeedFuture = measureUploadRateScheduler.scheduleAtFixedRate(traceUploadProgressSpeedProgressCallback::showUploadSpeedInfo, 5, 1, TimeUnit.SECONDS);
        }
        if (retryStrategy != null) {
            this.retryStrategy = retryStrategy;
        }
    }

    public String[] getNameFilters() {
        return nameFilters;
    }

    public void setNameFilters(String[] nameFilters) {
        this.nameFilters = nameFilters;
    }

    /**
     * 客户端处理完成之后，释放资源
     */
    public void shutdown() {
        if (traceUploadSpeedFuture != null) {
            traceUploadSpeedFuture.cancel(true);
        }
        ExecutorUtil.gracefulShutdown(measureUploadRateScheduler, 1000);
        ExecutorUtil.gracefulShutdown(parallelUploadExecutor, 1000);
    }

    protected final String generateFileIdentifier(String saveParentPath, String relativePath, File file) {
        String path = FileHandlerHelper.generateWholePath(saveParentPath, relativePath, file.getName());
        if (file.isFile()) {
            path = path + CommonConstant.UNDERLINE + file.length();
        }
        return FileHandlerHelper.generateUniqueIdentifier(path);
    }

    protected FileUploadRequest constructFileUploadRequest(String saveParentPath, String relativePath, File file, long startPos, RandomAccessFile accessFile) throws IOException {
        FileUploadRequest request = new FileUploadRequest();
        request.setFileName(file.getName());
        request.setSaveParentFolder(saveParentPath);
        request.setRelativePath(relativePath);
        request.setIdentifier(generateFileIdentifier(saveParentPath, relativePath, file));
        //目录和文件的标识不同，目录只需要完全路径即可，文件需要添加上文件字节大小
        if (file.isDirectory()) {
            request.setFileType(FileTypeEnum.DIR_TYPE);
            request.setCheckSum("0L");
        } else {
            request.setFileType(FileTypeEnum.FILE_TYPE);
            request.setStartPos(startPos);
            request.setTotalFileLength(file.length());
            int readBytesLength = 0;
            int perUploadSegmentLength = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH));
            byte[] segmentContents = new byte[perUploadSegmentLength];
            /**
             * 如果上传的是空文件，显示设置空文件标识{@link FileUploadRequest#setEmptyFile(boolean)}
             */
            if (file.length() > 0) {
                accessFile.seek(startPos);
                readBytesLength = accessFile.read(segmentContents);
                request.setContents(segmentContents);
                request.setBytesLength(readBytesLength);
            } else {
                request.setEmptyFile(true);
            }
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
        remoteRpcNode.releaseConnectionIfNecessary();
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

    protected String getRelativePath(File rootFile, File uploadSingleFile) {
        //根目录或者文件路径
        String rootFileAbsolutePath = rootFile.getAbsolutePath();
        String rootFileName = rootFile.getName();
        String parentFilePath = StringUtils.isBlank(rootFile.getParent()) ? "" : rootFile.getParent();
        String uploadSingleFileAbsolutePath = uploadSingleFile.getAbsolutePath();

        //获取相对于根目录的相对路径,比如当前根目录是d:/test，对应子目录是d:/test/nice/mv.mp4，那么相对路径就是test/nice
        String relativePath = null;
        if (!uploadSingleFileAbsolutePath.startsWith(rootFileAbsolutePath)) {
            return relativePath;
        }
        //相对路径
        int rootFilePathSplit = rootFileName.indexOf(parentFilePath) + parentFilePath.length() + 1;
        relativePath = uploadSingleFile.getParent().substring(rootFilePathSplit).replaceAll(Pattern.quote(CommonConstant.WINDOWS_FILE_SEPARATOR), CommonConstant.LINUX_SHELL_SEPARATOR);
        return relativePath;
    }

    /**
     * 单个文件上传，支持内部重试
     *
     * @param uploadSingleFile
     * @param saveParentPath
     * @param rootFile
     * @param remoteHost
     * @param remotePort
     * @param connectionTimeout
     * @return
     * @throws Exception
     */
    protected ClientUploadStatus uploadSingleFile(File uploadSingleFile, String saveParentPath, File rootFile, String remoteHost, int remotePort, int connectionTimeout) {
        RemoteRpcNode remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
        ClientUploadStatus clientUploadStatus;
        String relativePath = getRelativePath(rootFile, uploadSingleFile);
        while (true) {
            if (!remoteRpcNode.createRemoteConnectionWithMaxTryCount(maxCreateConnectionTryTimes)) {
                clientUploadStatus = ClientUploadStatus.FAIL;
                break;
            }
            FileTransferWorker.Client client = remoteRpcNode.getRemoteClient();
            FileUploadRequest[] fileUploadRequests = new FileUploadRequest[1];
            RetryStrategyEnum retryStrategyEnum = RetryStrategyEnum.ABORT;
            Function<FileUploadRequest, String> function = FileUploadRequest::getIdentifier;
            try {
                clientUploadStatus = doUploadSingleFile(saveParentPath, relativePath, uploadSingleFile, client, fileUploadRequests);
                //正常上传失败，走正常重试策略
                if (clientUploadStatus != ClientUploadStatus.UPLOAD_FINISH) {
                    retryStrategyEnum = retryStrategy.doRetryOrNot(fileUploadRequests[0], function, clientUploadStatus);
                }
            } catch (Exception e) {
                LOGGER.error("transport exception", e);
                clientUploadStatus = ClientUploadStatus.FAIL;
                retryStrategyEnum = retryStrategy.doRetryOrNot(fileUploadRequests[0], function, e);
            }
            boolean goOn = false;
            boolean terminalAll = false;
            switch (retryStrategyEnum) {
                case SIMPLE_RETRY:
                    goOn = true;
                    break;
                case TERMINATE_ALL:
                    terminalAll = true;
                    break;
                case RECREATE_CONNECTION_THEN_ABORT:
                    remoteRpcNode.releaseConnectionIfNecessary();
                    remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
                    goOn = false;
                    break;
                case RECREATE_CONNECTION_THEN_RETRY:
                    remoteRpcNode.releaseConnectionIfNecessary();
                    remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
                    goOn = true;
                    break;
                case ABORT:
                    break;
            }

            if (terminalAll) {
                LOGGER.warn("terminate all uploading file");
                clientUploadStatus = ClientUploadStatus.TERMINATE_ALL;
                break;
            }
            if (goOn) {
                continue;
            }
            break;
        }
        remoteRpcNode.releaseConnectionIfNecessary();
        return clientUploadStatus;
    }

    /**
     * 文件或者文件夹上传上传
     *
     * @param saveParentPath 服务端保存的路径，绝对路径
     * @param rootFile
     * @param remoteHost
     * @param remotePort
     * @return
     */
    public final void clientUploadFile(String saveParentPath, File rootFile, String remoteHost, int remotePort, int connectionTimeout) {
        if (!rootFile.exists()) {
            throw new RuntimeException("file path not exists or no read permission");
        }
        if (!detectConnection(remoteHost, remotePort, connectionTimeout)) {
            return;
        }
        if (shouldTraceUploadSpeed && !speedListenerAppend) {
            synchronized (this) {
                if (!speedListenerAppend) {
                    addUploadProgressFileCallback(traceUploadProgressSpeedProgressCallback);
                    speedListenerAppend = true;
                }
            }
        }
        List<File> fileLists = new ArrayList<>();
        if (rootFile.isFile()) {
            fileLists.add(rootFile);
        } else {
            IOFileFilter fileFilter = FileFileFilter.FILE;
            if (ArrayUtils.isNotEmpty(nameFilters)) {
                fileFilter = FileFilterUtils.and(new SuffixFileFilter(nameFilters, IOCase.INSENSITIVE), FileFileFilter.FILE);
            }
            fileLists = new ArrayList<>(FileUtils.listFilesAndDirs(rootFile, fileFilter, DirectoryFileFilter.DIRECTORY));
        }
        //排序
        fileLists.sort(Comparator.comparing(File::getAbsolutePath));
        ResetCountDownLatch countDownLatch = new ResetCountDownLatch(fileLists.size());
        AtomicInteger uploadSuccessFileCount = new AtomicInteger();
        AtomicInteger uploadFailFileCount = new AtomicInteger();
        Semaphore semaphore = new Semaphore(maxParallelUploadFileNum);
        AtomicBoolean terminateUploading = new AtomicBoolean(false);

        for (int index = 0; index < fileLists.size(); index++) {
            try {
                File uploadSingleFileOrDir = fileLists.get(index);
                semaphore.acquire();
                parallelUploadExecutor.execute(() -> {
                    ClientUploadStatus clientUploadStatus = ClientUploadStatus.FAIL;
                    String uploadFileAbsolutePath = uploadSingleFileOrDir.getAbsolutePath();
                    String relativePath = getRelativePath(rootFile, uploadSingleFileOrDir);
                    String fileIdentifier = generateFileIdentifier(saveParentPath, relativePath, uploadSingleFileOrDir);
                    FileTypeEnum fileTypeEnum = uploadSingleFileOrDir.isFile() ? FileTypeEnum.FILE_TYPE : FileTypeEnum.DIR_TYPE;
                    try {
                        clientUploadStatus = uploadSingleFile(uploadSingleFileOrDir, saveParentPath, rootFile, remoteHost, remotePort, connectionTimeout);
                    } catch (Exception e) {
                        LOGGER.error("exception when upload||filePath", e);
                    } finally {
                        LOGGER.info("upload result||filePath={}||result={}", uploadFileAbsolutePath, clientUploadStatus.getStatus());
                        LOGGER.info("release countdown lock");
                        countDownLatch.countDown();
                        semaphore.release();
                        if (clientUploadStatus != ClientUploadStatus.UPLOAD_FINISH) {
                            uploadSuccessFileCount.incrementAndGet();
                            onFileUploadFail(fileIdentifier, uploadFileAbsolutePath, fileTypeEnum);
                        } else {
                            uploadFailFileCount.incrementAndGet();
                            onFileUploadFinish(fileIdentifier, uploadFileAbsolutePath, fileTypeEnum);
                        }
                        if (clientUploadStatus == ClientUploadStatus.TERMINATE_ALL) {
                            terminateUploading.set(true);
                        }
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.warn("interrupted exception||exceptionMsg={}", e.getMessage());
            }
            if (terminateUploading.get()) {
                LOGGER.warn("terminate all upload task");
                uploadFailFileCount.addAndGet(fileLists.size() - index + 1);
                countDownLatch.releaseAll();
                break;
            }
        }
        try {
            LOGGER.info("main thread is wait upload finish");
            countDownLatch.await();
            LOGGER.info("upload result||uploadSuccessFileCount={}||uploadFailFileCount={}",
                    uploadSuccessFileCount.get(), uploadFailFileCount.get());
            LOGGER.info("upload finish");
        } catch (InterruptedException e) {
            LOGGER.error("upload interrupted exception", e);
        } finally {
            //资源释放由于客户端手动指定
//            shutdown();
        }
    }

}
