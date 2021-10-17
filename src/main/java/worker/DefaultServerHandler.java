package worker;

import common.FileHandlerHelper;
import cons.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

/**
 * 默认实现的thrift文件上传服务类
 */
public class DefaultServerHandler extends AbstractServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerHandler.class);

    private static class InnerInstance {
        static DefaultServerHandler instance = new DefaultServerHandler();
    }

    private DefaultServerHandler() {
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
            parentFile = new File(parentPath.replaceAll(Pattern.quote(File.separator), CommonConstant.LINUX_SHELL_SEPARATOR));
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                LOGGER.error("failed to create parentPath||parentFile={}", parentFile.getAbsolutePath());
                return parentFile;
            }
        }
        if (StringUtils.isNotBlank(relativePath)) {
            parentFile = new File(parentPath, relativePath);
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                LOGGER.error("failed to create relative path||relativePath={}", parentFile.getAbsolutePath());
                return parentFile;
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
        CachedUploadFileStructure cachedUploadFileStructure = uploadCacheLoader.getIfPresent(request.getIdentifier());
        RandomAccessFile tmpAccessFile = cachedUploadFileStructure.getRandomAccessFile();
        File tmpFile;
        if (parentFileFolder == null) {
            tmpFile = new File(cachedUploadFileStructure.getTmpFileName());
        } else {
            tmpFile = new File(parentFileFolder, cachedUploadFileStructure.getTmpFileName());
        }
        //首次上传
        if (tmpAccessFile == null) {
            synchronized (this) {
                try {
                    if (tmpAccessFile == null) {
                        tmpAccessFile = new RandomAccessFile(tmpFile, "rwd");
                    }
                    cachedUploadFileStructure.setRandomAccessFile(tmpAccessFile);
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
            tmpAccessFile.seek(request.startPos);
            byte[] segmentContents = request.getContents();
            int segmentBytesLength = request.getBytesLength();
            tmpAccessFile.write(segmentContents, 0, segmentBytesLength);
            long nextPos = request.getStartPos() + segmentBytesLength;
            //更新上传进度
            cachedUploadFileStructure.setCachedFileOffset(nextPos);
            response.setNextPos(nextPos);
            //文件上传完成
            if (request.getStartPos() + segmentBytesLength == cachedUploadFileStructure.getFileBytesLength()) {
                tmpAccessFile.close();
                //文件重命名
                File realFile = new File(tmpFile.getParent(), cachedUploadFileStructure.getFileName());
                if (!tmpFile.renameTo(realFile)) {
                    LOGGER.warn("failed to rename file||tmpFilePath={}||realFilePath={}", tmpFile.getAbsolutePath(), realFile.getAbsoluteFile());
                }
                response.setUploadStatusResult(ResResult.FILE_END);
            } else {
                response.setUploadStatusResult(ResResult.SUCCESS);
            }
        } catch (IOException e) {
            LOGGER.error("failed to write file", e);
            if (tmpAccessFile != null) {
                try {
                    tmpAccessFile.close();
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
