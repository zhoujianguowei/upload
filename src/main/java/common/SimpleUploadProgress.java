package common;

/**
 * 文件上传进度
 */
public class SimpleUploadProgress {
    private String fileIdentifier;
    private String filePath;
    private Long uploadBytesLength;
    private Long totalFileBytes;

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getUploadBytesLength() {
        return uploadBytesLength;
    }

    public void setUploadBytesLength(Long uploadBytesLength) {
        this.uploadBytesLength = uploadBytesLength;
    }

    public Long getTotalFileBytes() {
        return totalFileBytes;
    }

    public void setTotalFileBytes(Long totalFileBytes) {
        this.totalFileBytes = totalFileBytes;
    }
}
