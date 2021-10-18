package worker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.ErrorMeta;
import common.FileHandlerHelper;
import cons.BusinessConstant;
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
import static rpc.thrift.file.transfer.FileTypeEnum.FILE_TYPE;

/**
 * 处理文件上传、下载的抽象父类
 */
public abstract class AbstractServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServerHandler.class);
    /**
     * 单个文件上传最大允许上传间隔时长，其中key表示{@link FileUploadRequest#identifier}唯一标识，value表示上传文件结构
     */
    protected Cache<String, CachedUploadFileStructure> uploadProgressCacheLoader = CacheBuilder.newBuilder().
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
        long totalFileLength = request.getTotalFileLength();
        if (totalFileLength <= 0) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_TOTAL_LENGTH_ERROR);
        }
        long startPos = request.getStartPos();
        if (startPos < 0) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_START_POS_ERROR);
        }
        byte[] contents = request.getContents();
        if (ArrayUtils.isEmpty(contents)) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_CONTENTS_EMPTY);
        }
        //上传有效字节大小
        int contentsBytesLength = request.getBytesLength();
        if (contentsBytesLength > contents.length) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_BYTES_LENGTH_PARAM_ERROR);
        }
        if (startPos + contentsBytesLength > request.getTotalFileLength()) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_WRITE_BYTES_LENGTH_OVER_FLOW);
        }
        String checkSum = request.getCheckSum();
        if (StringUtils.isBlank(checkSum) || !checkSum.equals(FileHandlerHelper.generateContentsCheckSum(contents, contentsBytesLength))) {
            errorMeta.addErrorMsg(FileUploadErrorMsg.FILE_CHECK_SUM_VALIDATE_FAILED);
        }
        return errorMeta;

    }

    public abstract FileUploadResponse doHandleUploadFile(FileUploadRequest request);

    /***
     * 构造当前上传的文件基本内容，首次上传的时候设置
     * @param request
     * @return
     */
    public CachedUploadFileStructure extractCachedFileStructure(FileUploadRequest request) {
        CachedUploadFileStructure cachedUploadFileStructure = new CachedUploadFileStructure();
        cachedUploadFileStructure.setSaveParentFolder(request.getSaveParentFolder());
        cachedUploadFileStructure.setRelativePath(request.getRelativePath());
        cachedUploadFileStructure.setFileName(request.getFileName());
        cachedUploadFileStructure.setFileIdentifier(request.getIdentifier());
        cachedUploadFileStructure.setFileTypeEnum(request.getFileType());
        long currentTimeInMillis = System.currentTimeMillis();
        cachedUploadFileStructure.setUploadStartDate(currentTimeInMillis);
        cachedUploadFileStructure.setCachedFileOffset(0L);
        //当前上传的是文件，需要设置文件总大小
        if (request.getFileType() == FILE_TYPE) {
            cachedUploadFileStructure.setFileBytesLength(request.totalFileLength);
            cachedUploadFileStructure.setTmpFileName(request.getFileName() + BusinessConstant.TMP_UPLOAD_FILE_NAME_SUFFIX);
            if (request.isEncrypted() && request.getEncryptedType() != null) {
                cachedUploadFileStructure.setEncryptTypeEnum(request.getEncryptedType());
            }
        }
        return cachedUploadFileStructure;
    }

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
            if (errorMeta.getAllErrorMsgList().contains(FileUploadErrorMsg.FILE_CHECK_SUM_VALIDATE_FAILED)) {
                LOGGER.warn("file upload content changed");
                response.setUploadStatusResult(ResResult.FILE_BROKEN);
            }
            response.setErrorMsg(errorMeta.getDefaultErrorMsg());
            LOGGER.error("param or token failed to validate||request={}||token={}||errorMsgInfo={}", request, token, errorMeta.getDefaultErrorMsg());
            return response;
        }
        if (uploadProgressCacheLoader.getIfPresent(request.getIdentifier()) == null) {
            synchronized (this) {
                //首次传输，记录文件信息，包括文件名、文件大小、文件类型等
                if (uploadProgressCacheLoader.getIfPresent(request.getIdentifier()) == null) {
                    uploadProgressCacheLoader.put(request.getIdentifier(), extractCachedFileStructure(request));
                }
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
