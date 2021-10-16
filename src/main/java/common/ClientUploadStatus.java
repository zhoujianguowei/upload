package common;

/**
 * 客户端文件上传状态
 */
public enum ClientUploadStatus {
    ONGOING("上传中"), PAUSE("上传暂停"), ABORT("终止上传"), FAIL("上传失败"), UPLOAD_FINISH("上传完成");
    private String status;

    ClientUploadStatus(String status) {
        this.status = status;
    }
}
