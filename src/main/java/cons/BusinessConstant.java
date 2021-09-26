package cons;

/**
 * 业务类常量
 */
public class BusinessConstant {
    /**
     * 文件上传失败错误信息
     */
    public interface FileUploadErrorMsg{
        String FILE_BYTES_LENGTH_PARAM_ERROR="写入字节大小不正确";
        String FILE_CONTENTS_EMPTY="文件内容为空";
        String FILE_START_POS_ERROR="上传字段位置不合法";
        String FILE_TOTAL_LENGTH_ERROR="文件总大小配置错误";
        String TOKEN_VALIDATION_FAIL="token校验失败";
        String IDENTIFIER_VALIDATE_FAILED="文件身份校验失败";
        String FILE_CHECK_SUM_VALIDATE_FAILED="校验码错误";

    }
}
