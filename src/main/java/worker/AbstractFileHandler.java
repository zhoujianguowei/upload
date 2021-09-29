package worker;

import common.ErrorMeta;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import static cons.BusinessConstant.FileUploadErrorMsg;

/**
 * 处理文件上传、下载的抽象父类
 */
public abstract class AbstractFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileHandler.class);

    /**
     * 是否允许上传
     *
     * @param token token内容
     * @return
     */
    abstract boolean authorized(String token);

    protected ErrorMeta<String> validateRequestIdentifier(String identifier) {
        ErrorMeta<String> errorMeta = new ErrorMeta<>();
        if (StringUtils.isBlank(identifier) || identifier.length() != 48) {
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
        if (fileTypeEnum == FileTypeEnum.DIR_TYPE) {
            return errorMeta;
        }
        long startPos = request.getStartPos();
        if (startPos < 0) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_START_POS_ERROR);
        }
        //首次上传，需要设置文件总大小
        if (startPos == 0) {
            if (request.getTotalFileLength() <= 0) {
                errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_TOTAL_LENGTH_ERROR);
            }
        }
        errorMeta.combine(validateRequestIdentifier(request.getIdentifier()));
        byte[] contents = request.getContents();
        if (ArrayUtils.isEmpty(contents)) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_CONTENTS_EMPTY);
        }
        int contentLength = request.getBytesLength();
        if (contentLength > contents.length) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_BYTES_LENGTH_PARAM_ERROR);
        }
        String checkSum = request.getCheckSum();
        if (StringUtils.isBlank(checkSum) || checkSum.trim().length() < 4) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_CHECK_SUM_VALIDATE_FAILED);
        }
        return errorMeta;

    }

    public abstract FileUploadResponse doHandleUploadFile(FileUploadRequest request);

    public final FileUploadResponse handleUploadFile(FileUploadRequest request, String token) {
        ErrorMeta<String> errorMeta = new ErrorMeta<>();
        if (StringUtils.isNotBlank(token)) {
            if (!authorized(token)) {
                errorMeta.addErrorMsg(FileUploadErrorMsg.TOKEN_VALIDATION_FAIL);
            }
        }
        FileUploadResponse response = new FileUploadResponse();
        errorMeta.combine(validateRequestParam(request));
        if (!errorMeta.isLegal()) {
            response.setUploadStatusResult(ResResult.FILE_PARAM_VALIDATION_FAIL);
            response.setErrorMsg(errorMeta.getDefaultErrorMsg());
            return response;
        }
        return doHandleUploadFile(request);
    }
}
