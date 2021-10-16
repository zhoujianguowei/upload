package worker;

import common.FileHandlerHelper;
import cons.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 默认实现的thrift文件上传服务类
 */
public class DefaultServerHandler extends AbstractServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerHandler.class);

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
     * @param parentFileFolder 上传的父目录，如果在当前路径下
     * @param request
     * @return
     */
    protected FileUploadResponse handleUploadFile(File parentFileFolder, FileUploadRequest request) {
        return null;
    }

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
