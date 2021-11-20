package common;

/**
 * 客户端文件上传状态
 */
public enum ClientUploadStatus {
    ONGOING("上传中"), PAUSE("上传暂停"), ABORT("终止上传"), FAIL("上传失败"), UPLOAD_FINISH("上传完成"), TERMINATE_ALL("终止所有上传任务");
    private String status;

    ClientUploadStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
