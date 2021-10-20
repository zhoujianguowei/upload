package handler;

import rpc.thrift.file.transfer.FileTypeEnum;

/**
 * 文件上传进度回调接口，客户端实现
 *
 * @date 2021-10-10
 */
public interface UploadFileProgressCallback {
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
     * @param fileIdentifier        上传文件标识
     * @param filePath              上传文件路径所在本地路径，对于服务端来说是文件保存路径，对于客户端口是文件上传路径
     * @param fileBytesLength       当前上传文件大小
     * @param uploadFileBytesLength 上传文件大小
     */
    void onFileUploadProgress(String fileIdentifier, String filePath, long fileBytesLength, long uploadFileBytesLength);

}
