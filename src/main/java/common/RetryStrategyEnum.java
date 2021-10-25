package common;

/**
 * 重试策略返回枚举值
 */
public enum RetryStrategyEnum {
    SIMPLE_RETRY("重试"), ABORT("继续流程，不做处理"), RECREATE_CONNECTION_THEN_RETRY("重新建立链接，然后再重试"),
    RECREATE_CONNECTION_THEN_ABORT("重新建立连接，然后取消"), TERMINATE_ALL("终止剩余所有文件上传");
    private String action;

    RetryStrategyEnum(String action) {
        this.action = action;
    }
}
