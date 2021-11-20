namespace java rpc.thrift.file.transfer
//请求结果状态枚举
enum ResResult{
    //文件首次传输
    FILE_START,
    //传输参数错误
    FILE_PARAM_VALIDATION_FAIL,
    //文件传输成功，还没有传输完成
    SUCCESS,
    //文件token鉴权失败
    TOKEN_FAILED,
    //文件传输损坏
    FILE_BROKEN,
    //其它未知错误
    UNKNOWN_ERROR,
    //文件传输完成
    FILE_END,
    //修正offset，上传文件的时候，断点续传首次传输时候，返回
    FIX_UPLOAD_OFFSET;
}
struct FileUploadResponse{
    1:required ResResult uploadStatusResult;
    //如果是文件，表示下一步需要传输的文件起始字节索引
    2:optional i64 nextPos;
    //文件传输失败详情
    3:optional string errorMsg;
}