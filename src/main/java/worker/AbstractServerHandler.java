package worker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.ErrorMeta;
import common.FileHandlerHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import java.util.concurrent.TimeUnit;

import static cons.BusinessConstant.FileUploadErrorMsg;

/**
 * 处理文件上传、下载的抽象父类
 */
public abstract class AbstractServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServerHandler.class);
    /**
     * 单个文件上传最大允许上传间隔时长，其中{@link FileUploadRequest#identifier}唯一标识
     */
    protected Cache<String, FileUploadRequest> uploadCacheLoader = CacheBuilder.newBuilder().
            expireAfterAccess(7, TimeUnit.DAYS).build();

    /**
     * 是否允许上传
     *
     * @param token token内容
     * @return
     */
    abstract boolean authorized(String token, String fileName);

    protected ErrorMeta<String> validateRequestIdentifier(FileUploadRequest request) {
        String identifier = request.getIdentifier();
        ErrorMeta<String> errorMeta = new ErrorMeta<>();
        if (StringUtils.isBlank(identifier) || !FileHandlerHelper.validateFileIdentifier(request)) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.IDENTIFIER_VALIDATE_FAILED);
        }
        return errorMeta;
    }

    /**
     * 检测请求是否合法，上传如果是文件，需要配置一系列参数
     *
     * @param request
     * @return
     */
    protected ErrorMeta<String> validateRequestParam(FileUploadRequest request) {
        FileTypeEnum fileTypeEnum = request.getFileType();
        ErrorMeta<String> errorMeta = new ErrorMeta<>();
        errorMeta.combine(validateRequestIdentifier(request));
        if (fileTypeEnum == FileTypeEnum.DIR_TYPE) {
            if (!errorMeta.isLegal()) {
                return errorMeta;
            }
            return errorMeta;
        }
        long startPos = request.getStartPos();
        if (startPos < 0) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_START_POS_ERROR);
        }
        byte[] contents = request.getContents();
        if (ArrayUtils.isEmpty(contents)) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_CONTENTS_EMPTY);
        }
        int contentLength = request.getBytesLength();
        if (contentLength > contents.length) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_BYTES_LENGTH_PARAM_ERROR);
        }
        String checkSum = request.getCheckSum();
        if (StringUtils.isBlank(checkSum) || !checkSum.equals(FileHandlerHelper.generateContentsCheckSum(contents))) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_CHECK_SUM_VALIDATE_FAILED);
        }
        //首次上传，需要设置文件总大小
        if (startPos == 0) {
            if (request.getTotalFileLength() <= 0) {
                errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_TOTAL_LENGTH_ERROR);
            }
        }
        return errorMeta;

    }

    public abstract FileUploadResponse doHandleUploadFile(FileUploadRequest request);

    public final FileUploadResponse handleUploadFile(FileUploadRequest request, String token) {
        LOGGER.debug("upload file param||request={}||token={}", request, token);
        ErrorMeta<String> errorMeta = new ErrorMeta<>();
        if (!authorized(token, request.getFileName())) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.TOKEN_VALIDATION_FAIL);
        }
        FileUploadResponse response = new FileUploadResponse();
        errorMeta.combine(validateRequestParam(request));
        if (!errorMeta.isLegal()) {
            response.setUploadStatusResult(ResResult.FILE_PARAM_VALIDATION_FAIL);
            response.setErrorMsg(errorMeta.getDefaultErrorMsg());
            LOGGER.error("param or token failed to validate||request={}||token={}||errorMsgInfo={}", request, token, errorMeta.getDefaultErrorMsg());
            return response;
        }
        synchronized (this) {
            //首次传输，记录文件信息，包括文件名、文件大小、文件类型等
            if (uploadCacheLoader.getIfPresent(request.getIdentifier()) == null) {
                uploadCacheLoader.put(request.getIdentifier(), request);
            }
        }

        try {
            response = doHandleUploadFile(request);
        } catch (Exception e) {
            LOGGER.error("transport exception when upload||requestParam=" + request.toString(), e);
            response.setUploadStatusResult(ResResult.UNKNOWN_ERROR);
        }
        return response;
    }
}
