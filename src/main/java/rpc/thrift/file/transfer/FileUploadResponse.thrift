namespace java rpc.thrift.file.transfer
//请求结果状态枚举
enum ResResult{
    FILE_START,
    FILE_PARAM_VALIDATION_FAIL,
    SUCCESS,
    TOKEN_FAILED,
    FILE_BROKEN,
    UNKNOWN_ERROR,
    FILE_END,
    FIX_UPLOAD_OFFSET
}
struct FileUploadResponse{
    1:required ResResult uploadStatusResult;
    //如果是文件，表示下一步需要传输的文件起始字节索引
    2:optional i64 nextPos;
    //文件传输失败详情
    3:optional string errorMsg;
}