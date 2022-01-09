package rpc.thrift.file.limit;

import rpc.thrift.file.transfer.FileUploadRequest;

/***
 * 通用限流接口
 * @date 2022-01-09
 */
public interface UploadLimit {
    /**
     * 阻塞式限流
     *
     * @param uploadRequest
     */
    void blockLimit(FileUploadRequest uploadRequest);

    /**
     * 非阻塞限流查询
     *
     * @param fileUploadRequest
     * @return
     */
    default boolean tryAcquire(FileUploadRequest fileUploadRequest) {
        return true;
    }
}
