package handler;

import com.google.common.collect.Maps;
import common.SimpleUploadProgress;
import common.ThreadPoolManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultUploadProgressCallBack implements UploadFileCallBack {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUploadProgressCallBack.class);
    private Map<String, Float> preUploadProcess = Maps.newConcurrentMap();
    /**
     * 用来评估文件上传速度
     */
    private Map<String, SimpleUploadProgress> previousUploadFileBytesLengthMap = Maps.newConcurrentMap();
    private Map<String, SimpleUploadProgress> updateUploadFileBytesLengthMap = Maps.newConcurrentMap();
    private ScheduledExecutorService measureUploadRateScheduler = ThreadPoolManager.getMeasureUploadRateScheduler();
    private static final DecimalFormat PROGRESS_DECIMAL_FORMAT = new DecimalFormat("0.00%");
    private static final Long PER_KB_BYTES = 1024L;

    public DefaultUploadProgressCallBack() {
        measureUploadRateScheduler.scheduleAtFixedRate(this::printUploadRateInfo, 1, 1, TimeUnit.SECONDS);
    }

    public void printUploadRateInfo() {
        long totalUploadKbPerSecond = 0L;
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
            long uploadRate = updateUploadProgress.getUploadBytesLength() - preUploadProgress.getUploadBytesLength();
            LOGGER.info("upload rate||rate={}kb||filePath={}", uploadRate / PER_KB_BYTES, updateUploadProgress.getFilePath());
            totalUploadKbPerSecond += uploadRate;
            preUploadProgress.setUploadBytesLength(updateUploadProgress.getUploadBytesLength());
        }
        LOGGER.info("total upload rate||rate={}kb", totalUploadKbPerSecond / PER_KB_BYTES);
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
    public void onFileUploadProgress(String fileIdentifier, long totalFileBytesLength, long totalUploadFileBytes, String filePath, long fileBytesLength, long uploadFileBytesLength) {
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
            LOGGER.info("upload progress||progress={}||filePath={}", PROGRESS_DECIMAL_FORMAT.format(currentProgress), filePath);
        }
    }
}
