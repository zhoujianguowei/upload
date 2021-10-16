package handler;

import common.ClientUploadStatus;

/**
 * 控制客户端文件上传状态，管理单个文件上传的状态
 */
public abstract class ClientUploadManager {
    /**
     * 最大并发上传文件数量
     *
     * @return
     */
    protected int maxParallelUploadFileNum() {
        return Integer.MAX_VALUE;
    }

    /**
     * 外界的控制命令（手动停止或者取消上传）
     *
     * @return
     */
    protected abstract ClientUploadStatus getOuterCommand(String fileIdentifier);

    /**
     * 获取当前正在上传的文件的数量
     *
     * @return
     */
    protected abstract int getOnGoingUploadFileNum();

    /**
     * 获取当前文件上传状态
     *
     * @param identifier
     * @return
     */
    public ClientUploadStatus getUploadFileStatus(String identifier) {
        int onGoingFileNum = getOnGoingUploadFileNum();
        int maxParallelUploadFileNum = maxParallelUploadFileNum();
        if (onGoingFileNum >= maxParallelUploadFileNum) {
            return ClientUploadStatus.PAUSE;
        }
        return getOuterCommand(identifier);
    }
}
