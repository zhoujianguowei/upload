package config;

public class ClientConfig {
    private String hostIdentifier;
    private int perUploadBytesLength = 102400;
    private int fileContentBrokerMaxRetryTimes = 3;
    private int maxParallelUploadFileNum = 5;
    private boolean traceClientUploadSpeedSwitch = true;
    private int fileUploadMaxRetryCount = 5;
    private int clientCreateConnectionMaxTryTimes = 5;
    private String fileUploadSaveParentPath = System.getProperty("user.home") + "/Download";

    public String getHostIdentifier() {
        return hostIdentifier;
    }

    public void setHostIdentifier(String hostIdentifier) {
        this.hostIdentifier = hostIdentifier;
    }

    public int getPerUploadBytesLength() {
        return perUploadBytesLength;
    }

    public void setPerUploadBytesLength(int perUploadBytesLength) {
        this.perUploadBytesLength = perUploadBytesLength;
    }

    public int getFileContentBrokerMaxRetryTimes() {
        return fileContentBrokerMaxRetryTimes;
    }

    public void setFileContentBrokerMaxRetryTimes(int fileContentBrokerMaxRetryTimes) {
        this.fileContentBrokerMaxRetryTimes = fileContentBrokerMaxRetryTimes;
    }

    public int getMaxParallelUploadFileNum() {
        return maxParallelUploadFileNum;
    }

    public void setMaxParallelUploadFileNum(int maxParallelUploadFileNum) {
        this.maxParallelUploadFileNum = maxParallelUploadFileNum;
    }

    public boolean isTraceClientUploadSpeedSwitch() {
        return traceClientUploadSpeedSwitch;
    }

    public void setTraceClientUploadSpeedSwitch(boolean traceClientUploadSpeedSwitch) {
        this.traceClientUploadSpeedSwitch = traceClientUploadSpeedSwitch;
    }

    public int getFileUploadMaxRetryCount() {
        return fileUploadMaxRetryCount;
    }

    public void setFileUploadMaxRetryCount(int fileUploadMaxRetryCount) {
        this.fileUploadMaxRetryCount = fileUploadMaxRetryCount;
    }

    public int getClientCreateConnectionMaxTryTimes() {
        return clientCreateConnectionMaxTryTimes;
    }

    public void setClientCreateConnectionMaxTryTimes(int clientCreateConnectionMaxTryTimes) {
        this.clientCreateConnectionMaxTryTimes = clientCreateConnectionMaxTryTimes;
    }

    public String getFileUploadSaveParentPath() {
        return fileUploadSaveParentPath;
    }

    public void setFileUploadSaveParentPath(String fileUploadSaveParentPath) {
        this.fileUploadSaveParentPath = fileUploadSaveParentPath;
    }
}
