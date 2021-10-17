package handler;

import rpc.thrift.file.transfer.FileTypeEnum;

/**
 * 文件上传进度回调接口，客户端实现
 *
 * @date 2021-10-10
 */
public interface UploadFileCallBack {
    /**
     * 单个文件上传完成
     *
     * @param filePath
     * @param fileType
     */
    void onFileUploadFinish(String fileIdentifier, String filePath, FileTypeEnum fileType);

    /**
     * 单个文件或者文件夹上传失败
     *
     * @param filePath
     * @param fileType
     */
    void onFileUploadFail(String fileIdentifier, String filePath, FileTypeEnum fileType);

    /**
     * 单个文件上传取消
     *
     * @param fileIdentifier
     * @param filePath
     * @param fileType
     */
    void onFileCancel(String fileIdentifier, String filePath, FileTypeEnum fileType);

    /**
     * 文件上传进度
     *
     * @param totalFileBytesLength  上传的是文件夹，根文件夹路径
     * @param totalUploadFileBytes  已经上传的文件总大小
     * @param filePath              上传文件路径
     * @param fileBytesLength       当前上传文件大小
     * @param uploadFileBytesLength 上传文件大小
     */
    void onFileUploadProgress(String fileIdentifier,long totalFileBytesLength, long totalUploadFileBytes, String filePath, long fileBytesLength, long uploadFileBytesLength);

}
