package common;

/**
 * 客户端文件上传状态
 */
public enum ClientUploadStatus {
    ONGOING("上传中"), 
    PAUSE("上传暂停"), 
    ABORT("终止上传"), 
    FAIL("上传失败"), 
    UPLOAD_FINISH("上传完成"),
    TERMINATE_ALL("终止所有上传任务"),
    PENDING("等待上传");
    
    private String status;

    ClientUploadStatus(String status) {
        this.status = status;
    }
    
    public String getStatus(){
        return status;
    }
    
    /**
     * 根据状态字符串获取对应的枚举值
     */
    public static ClientUploadStatus fromStatus(String status) {
        for (ClientUploadStatus value : ClientUploadStatus.values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
