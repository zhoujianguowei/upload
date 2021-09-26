package worker;

import common.ErrorMeta;
import cons.BusinessConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileSegmentRequest;
import rpc.thrift.file.transfer.FileSegmentResponse;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.ResResult;

import java.util.Arrays;

import static cons.BusinessConstant.*;

/**
 * 处理文件上传的抽象父类
 */
public abstract class AbsFileUploadWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbsFileUploadWorker.class);

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
    protected ErrorMeta<String> validateRequestParam(FileSegmentRequest request) {
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

    public abstract FileSegmentResponse doHandleUploadFile(FileSegmentRequest request);

    public final FileSegmentResponse handleUploadFile(FileSegmentRequest request, String token) {
        ErrorMeta<String> errorMeta = new ErrorMeta<>();
        if (StringUtils.isNotBlank(token)) {
            if (!authorized(token)) {
                errorMeta.addErrorMsg(FileUploadErrorMsg.TOKEN_VALIDATION_FAIL);
            }
        }
        FileSegmentResponse response = new FileSegmentResponse();
        errorMeta.combine(validateRequestParam(request));
        if (!errorMeta.isLegal()) {
            response.setUploadStatusResult(ResResult.FILE_PARAM_VALIDATION_FAIL);
            response.setErrorMsg(errorMeta.getDefaultErrorMsg());
            return response;
        }
        return doHandleUploadFile(request);
    }
}
