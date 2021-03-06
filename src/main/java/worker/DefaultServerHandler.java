package worker;

import common.FileHandlerHelper;
import common.ThreadPoolManager;
import config.UploadProgressHelper;
import cons.CommonConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 默认实现的thrift文件上传服务类
 */
public class DefaultServerHandler extends AbstractServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerHandler.class);
    protected ScheduledExecutorService syncUploadProgressScheduler = ThreadPoolManager.getServerSyncUploadProgressScheduler();
    protected Future syncUploadProgressFuture;

    private static class InnerInstance {
        static DefaultServerHandler instance = new DefaultServerHandler();
    }

    private DefaultServerHandler() {
        loadUploadProgress();
        syncUploadProgressFuture = syncUploadProgressScheduler.scheduleAtFixedRate(() -> syncUploadProgress(), 5, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        if (syncUploadProgressFuture != null) {
            syncUploadProgressFuture.cancel(true);
        }
        ExecutorUtil.gracefulShutdown(syncUploadProgressScheduler, 1000);
    }

    /**
     * 加载文件上传进度
     */
    protected synchronized void loadUploadProgress() {
        List<CachedUploadFileStructure> cachedUploadFileStructureList = UploadProgressHelper.loadUploadProgressData();
        if (CollectionUtils.isEmpty(cachedUploadFileStructureList)) {
            LOGGER.info("no upload progress,don't need to load");
        }
        for (CachedUploadFileStructure cachedUploadFileStructure : cachedUploadFileStructureList) {
            String absoluteFilePath = FileHandlerHelper.generateWholePath(cachedUploadFileStructure.getSaveParentFolder(), cachedUploadFileStructure.getRelativePath(), cachedUploadFileStructure.getTmpFileName());
            File uploadTmpFile = new File(absoluteFilePath);
            if (!uploadTmpFile.exists() || !uploadTmpFile.isFile() || uploadTmpFile.length() < cachedUploadFileStructure.getCachedFileOffset()) {
                LOGGER.warn("cached file not exists,remove||cacheFileStructure={}", cachedUploadFileStructure);
                continue;
            }
            uploadProgressCacheLoader.put(cachedUploadFileStructure.getFileIdentifier(), cachedUploadFileStructure);
        }
        LOGGER.info("load upload progress success");
    }

    protected synchronized void syncUploadProgress() {
        List<CachedUploadFileStructure> cachedUploadFileStructures = new ArrayList<>(uploadProgressCacheLoader.asMap().values());
        UploadProgressHelper.persistUploadProgressData(cachedUploadFileStructures);
        LOGGER.info("sync upload progress success");
    }

    public static DefaultServerHandler getSingleTon() {
        return InnerInstance.instance;
    }

    @Override
    boolean authorized(String token, String fileName) {
        return FileHandlerHelper.validateFileToken(token, fileName);
    }

    /**
     * 创建父目录，如果父目录不存在
     *
     * @param request
     * @return
     */
    protected File createParentFileIfNotExists(FileUploadRequest request) {
        //保存的父目录绝度路径
        String parentPath = request.getSaveParentFolder();
        //相对路径
        String relativePath = request.getRelativePath();
        File parentFile = null;
        if (StringUtils.isBlank(parentPath) && StringUtils.isBlank(relativePath)) {
            return parentFile;
        }
        if (StringUtils.isNotBlank(parentPath)) {
            parentFile = new File(parentPath.replaceAll(Pattern.quote(CommonConstant.WINDOWS_FILE_SEPARATOR), CommonConstant.LINUX_SHELL_SEPARATOR));
            if (!parentFile.exists()) {
                synchronized (this) {
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        LOGGER.error("failed to create parentPath||parentFile={}", parentFile.getAbsolutePath());
                        return parentFile;
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(relativePath)) {
            parentFile = new File(parentPath, relativePath);
            if (!parentFile.exists()) {
                synchronized (this) {
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        LOGGER.error("failed to create relative path||relativePath={}", parentFile.getAbsolutePath());
                        return parentFile;
                    }
                }
            }
        }
        return parentFile;
    }

    /**
     * 当前上传的是目录
     *
     * @param request
     * @return
     */
    protected FileUploadResponse handleUploadDir(File parentFileFolder, FileUploadRequest request) {
        File targetDir = null;
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        if (parentFileFolder != null) {
            targetDir = new File(parentFileFolder, request.getFileName());
        } else {
            targetDir = new File(request.getFileName());
        }
        synchronized (this) {
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                boolean createNewFolderFlag = targetDir.mkdirs();
                if (!createNewFolderFlag) {
                    LOGGER.error("failed to create folder||path={}", targetDir.getAbsolutePath());
                    fileUploadResponse.setErrorMsg(String.format("failed to create folder||path=%s", targetDir.getAbsolutePath()));
                    fileUploadResponse.setUploadStatusResult(ResResult.UNKNOWN_ERROR);
                    return fileUploadResponse;
                }
            }
        }
        fileUploadResponse.setUploadStatusResult(ResResult.FILE_END);
        uploadProgressCacheLoader.invalidate(request.getIdentifier());
        return fileUploadResponse;
    }

    /**
     * 处理上传的是文件
     *
     * @param parentFileFolder 上传的父目录
     * @param request
     * @return
     */
    protected FileUploadResponse handleUploadFile(File parentFileFolder, FileUploadRequest request) {
        FileUploadResponse response = new FileUploadResponse();
        CachedUploadFileStructure cachedUploadFileStructure = uploadProgressCacheLoader.getIfPresent(request.getIdentifier());
        RandomAccessFile randomAccessFile = cachedUploadFileStructure.getRandomAccessFile();
        File tmpFile;
        if (parentFileFolder == null) {
            tmpFile = new File(cachedUploadFileStructure.getTmpFileName());
        } else {
            tmpFile = new File(parentFileFolder, cachedUploadFileStructure.getTmpFileName());
        }
        //首次上传或者是从离线日志中同步
        if (randomAccessFile == null) {
            synchronized (this) {
                try {
                    if (randomAccessFile == null) {
                        randomAccessFile = new RandomAccessFile(tmpFile, "rwd");
                    }
                    cachedUploadFileStructure.setRandomAccessFile(randomAccessFile);
                } catch (Exception e) {
                    LOGGER.error("failed to create tmp file", e);
                    throw new RuntimeException("failed to create tmp file");
                }
            }
        }
        //调整上传分段offset值
        if (request.getStartPos() != cachedUploadFileStructure.getCachedFileOffset()) {
            response.setUploadStatusResult(ResResult.SUCCESS);
            response.setNextPos(cachedUploadFileStructure.getCachedFileOffset());
            return response;
        }
        try {
            randomAccessFile.seek(request.startPos);
            byte[] segmentContents = request.getContents();
            int segmentBytesLength = request.getBytesLength();
            randomAccessFile.write(segmentContents, 0, segmentBytesLength);
            long nextPos = request.getStartPos() + segmentBytesLength;
            //更新上传进度
            cachedUploadFileStructure.setCachedFileOffset(nextPos);
            response.setNextPos(nextPos);
            //文件上传完成
            if (request.getStartPos() + segmentBytesLength == cachedUploadFileStructure.getFileBytesLength()) {
                randomAccessFile.close();
                //文件重命名
                File realFile = new File(tmpFile.getParent(), cachedUploadFileStructure.getFileName());
                if (!tmpFile.renameTo(realFile)) {
                    LOGGER.warn("failed to rename file||tmpFilePath={}||realFilePath={}", tmpFile.getAbsolutePath(), realFile.getAbsoluteFile());
                }
                response.setUploadStatusResult(ResResult.FILE_END);
                //清除缓存的上传文件进度信息
                uploadProgressCacheLoader.invalidate(request.getIdentifier());
            } else {
                response.setUploadStatusResult(ResResult.SUCCESS);
            }
        } catch (IOException e) {
            LOGGER.error("failed to write file", e);
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e1) {
                    LOGGER.error("io exception", e);
                }
            }
            throw new RuntimeException("failed to write file");
        }
        return response;
    }

    /**
     * 检查上传的文件是否已存在
     *
     * @param parentFile
     * @param request
     * @return
     */
    public boolean targetFileExists(String parentFile, FileUploadRequest request) {
        File targetFile;
        if (StringUtils.isNotBlank(parentFile)) {
            targetFile = new File(parentFile, request.getFileName());
        } else {
            targetFile = new File(request.getFileName());
        }
        FileTypeEnum typeEnum = request.getFileType();
        boolean exists = false;
        if (targetFile.exists()) {
            switch (typeEnum) {
                case DIR_TYPE:
                    if (targetFile.isDirectory()) {
                        exists = true;
                    }
                    break;
                case FILE_TYPE:
                    if (targetFile.isFile()) {
                        exists = true;
                    }
                    break;
            }
        }
        if (exists) {
            LOGGER.warn("target upload file exists,ignore||targetFilePath={}", targetFile.getAbsolutePath());
        }
        return exists;
    }

    /**
     * 上传单个文件，如果文件已存在，直接返回上传成功
     *
     * @param request
     * @return
     */
    @Override
    public FileUploadResponse doHandleUploadFile(FileUploadRequest request) {
        FileUploadResponse response = new FileUploadResponse();
        String relativePath = request.getRelativePath();
        String saveParentPath = request.getSaveParentFolder();
        File parentFile = null;
        if (StringUtils.isNotBlank(relativePath) || StringUtils.isNotBlank(saveParentPath)) {
            parentFile = createParentFileIfNotExists(request);
            if (parentFile != null && !parentFile.exists()) {
                response.setErrorMsg(String.format("failed to create parent dir folder||path=%s", parentFile.getAbsolutePath()));
                response.setUploadStatusResult(ResResult.UNKNOWN_ERROR);
                return response;
            }
        }
        if (targetFileExists(parentFile == null ? null : parentFile.getAbsolutePath(), request)) {
            response.setUploadStatusResult(ResResult.FILE_END);
            uploadProgressCacheLoader.invalidate(request.getIdentifier());
            return response;
        }

        switch (request.getFileType()) {
            case FILE_TYPE:
                response = handleUploadFile(parentFile, request);
                break;
            case DIR_TYPE:
                response = handleUploadDir(parentFile, request);
                break;
        }
        return response;
    }
}
