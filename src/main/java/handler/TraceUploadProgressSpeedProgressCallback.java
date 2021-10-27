package handler;

import com.google.common.collect.Maps;
import common.SimpleUploadProgress;
import org.apache.thrift.util.StorageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * 客户端打印文件上传速度回调工具类
 */
public class TraceUploadProgressSpeedProgressCallback implements UploadFileProgressCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceUploadProgressSpeedProgressCallback.class);
    private Map<String, Float> preUploadProcess = Maps.newConcurrentMap();
    /**
     * 用来评估文件上传速度
     */
    private Map<String, SimpleUploadProgress> previousUploadFileBytesLengthMap = Maps.newConcurrentMap();
    private Map<String, SimpleUploadProgress> updateUploadFileBytesLengthMap = Maps.newConcurrentMap();
    /**
     * 上传文件速度格式，只保留一位整数，比如13.5kb，25.8kb,3.2mb
     */
    private static final String UPLOAD_SPEED_FORMAT = "0.0";
    /**
     * 上传文件进度格式，保留2位小数
     */
    private static final DecimalFormat UPLOAD_PROGRESS_NUMBER_FORMAT = new DecimalFormat("0.00%");

    public TraceUploadProgressSpeedProgressCallback() {
    }

    public void showUploadSpeedInfo() {
        long totalUploadBytesPerSecond = 0L;
        for (Map.Entry<String, SimpleUploadProgress> entry : updateUploadFileBytesLengthMap.entrySet()) {
            String fileIdentifier = entry.getKey();
            SimpleUploadProgress updateUploadProgress = entry.getValue();
            SimpleUploadProgress preUploadProgress = previousUploadFileBytesLengthMap.computeIfAbsent(fileIdentifier, (k) -> {
                SimpleUploadProgress simpleUploadProgress = new SimpleUploadProgress();
                simpleUploadProgress.setFilePath(updateUploadProgress.getFilePath());
                simpleUploadProgress.setTotalFileBytes(updateUploadProgress.getTotalFileBytes());
                simpleUploadProgress.setFileIdentifier(updateUploadProgress.getFileIdentifier());
                simpleUploadProgress.setUploadBytesLength(0L);
                return simpleUploadProgress;
            });
            long diffUploadBytesLength = updateUploadProgress.getUploadBytesLength() - preUploadProgress.getUploadBytesLength();
            LOGGER.info("upload rate||rate={}||filePath={}", StorageFormat.formatStorageSize(String.valueOf(diffUploadBytesLength) + "byte", UPLOAD_SPEED_FORMAT), updateUploadProgress.getFilePath());
            totalUploadBytesPerSecond += diffUploadBytesLength;
            preUploadProgress.setUploadBytesLength(updateUploadProgress.getUploadBytesLength());
        }
        LOGGER.info("total upload rate||rate={}", StorageFormat.formatStorageSize(String.valueOf(totalUploadBytesPerSecond) + "byte", UPLOAD_SPEED_FORMAT));
    }

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
    public void onFileUploadProgress(String fileIdentifier, String filePath, long fileBytesLength, long uploadFileBytesLength) {
        updateUploadFileBytesLengthMap.computeIfAbsent(fileIdentifier, (k) -> {
            SimpleUploadProgress simpleUploadProgress = new SimpleUploadProgress();
            simpleUploadProgress.setFileIdentifier(fileIdentifier);
            simpleUploadProgress.setTotalFileBytes(fileBytesLength);
            simpleUploadProgress.setFilePath(filePath);
            simpleUploadProgress.setUploadBytesLength(uploadFileBytesLength);
            return simpleUploadProgress;
        }).setUploadBytesLength(uploadFileBytesLength);
        float currentProgress = uploadFileBytesLength * 1.0f / fileBytesLength;
        if (showUpdateUploadProgress(fileIdentifier, currentProgress)) {
            preUploadProcess.put(fileIdentifier, currentProgress);
            LOGGER.info("upload progress||progress={}||filePath={}", UPLOAD_PROGRESS_NUMBER_FORMAT.format(currentProgress), filePath);
        }
    }
}
