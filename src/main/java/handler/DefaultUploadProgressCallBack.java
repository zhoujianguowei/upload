package handler;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;

import java.text.DecimalFormat;
import java.util.Map;

public class DefaultUploadProgressCallBack implements UploadFileCallBack {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUploadProgressCallBack.class);
    private Map<String, Float> preUploadProcess = Maps.newConcurrentMap();
    private static final DecimalFormat PROGRESS_DECIMAL_FORMAT = new DecimalFormat("0.##%");

    protected boolean showUpdateUploadProgress(String fileIdentifier, float currentUpdateProgress) {
        float preUploadProgress = preUploadProcess.getOrDefault(fileIdentifier, 0F);
        return currentUpdateProgress - preUploadProgress > 0.0001;
    }

    @Override
    public void onFileUploadFinish(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        LOGGER.info("file upload success||filePath={}", filePath);
    }

    @Override
    public void onFileUploadFail(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        LOGGER.error("file upload fail||filePath={}", filePath);
    }

    @Override
    public void onFileCancel(String fileIdentifier, String filePath, FileTypeEnum fileType) {

    }

    @Override
    public void onFileUploadProgress(String fileIdentifier, long totalFileBytesLength, long totalUploadFileBytes, String filePath, long fileBytesLength, long uploadFileBytesLength) {
        float currentProgress = uploadFileBytesLength * 1.0f / fileBytesLength;
        if (showUpdateUploadProgress(fileIdentifier, currentProgress)) {
            preUploadProcess.put(fileIdentifier, currentProgress);
            LOGGER.info("upload progress||progress={}||filePath={}", PROGRESS_DECIMAL_FORMAT.format(currentProgress), filePath);
        }
    }
}
