package cons;

import handler.ShowUploadProgressSpeedProgressCallback;

/**
 * 业务类常量
 */
public class BusinessConstant {
    /**
     * 文件上传失败错误信息
     */
    public interface FileUploadErrorMsg {
        String FILE_BYTES_LENGTH_PARAM_ERROR = "写入字节大小不正确";
        String FILE_CONTENTS_EMPTY = "文件内容为空";
        String FILE_START_POS_ERROR = "上传字段位置不合法";
        String FILE_TOTAL_LENGTH_ERROR = "文件总大小配置错误";
        String TOKEN_VALIDATION_FAIL = "token校验失败";
        String IDENTIFIER_VALIDATE_FAILED = "文件身份校验失败";
        String FILE_CHECK_SUM_VALIDATE_FAILED = "校验码错误";
        String FILE_WRITE_BYTES_LENGTH_OVER_FLOW = "写入文件超过文件总大小";

    }

    public static final String SERVER_TERMINAL_TYPE = "server_terminal";
    public static final String CLIENT_TERMINAL_TYPE = "client_terminal";
    /**
     * 上传的文件临时名称
     */
    public static final String TMP_UPLOAD_FILE_NAME_SUFFIX = ".tmp";

    /**
     * 配置文件内容
     */
    public interface ConfigData {
        /**
         * 当前主机唯一标识
         */
        String HOST_IDENTIFIER_KEY = "host_identifier";
        /**
         * 一次上传的字节数,默认100kb
         */
        String PER_UPLOAD_BYTES_LENGTH = "per_upload_bytes_length";
        /**
         * 文件上传过程中，由于网络原因导致字节错误的最大重试次数，默认3次
         */
        String FILE_CONTENT_BROKER_MAX_RETRY_TIMES = "file_content_broker_max_retry_times";
        /**
         * 批量上传的最大文件个数，默认5个
         */
        String MAX_PARALLEL_UPDATE_FILE_NUM = "max_parallel_upload_file_num";
        /**
         * 文件上传rpc server端的默认端口
         */
        String TRANSFER_FILE_SERVER_PORT = "upload_file_server_port";
        /**
         * 是否展示客户端上传速度，如果设置为true的话，那么默认会添加{@link ShowUploadProgressSpeedProgressCallback}监听类
         */
        String SHOW_CLIENT_UPLOAD_SPEED_SWITCH = "show_client_upload_speed";
        /**
         * 文件上传最大重试次数，这个重试次数限制的是文件重试的类型。包括异常重试、非异常重试等
         */
        String FILE_UPLOAD_MAX_RETRY_COUNT="file_upload_max_retry_count";
    }
}
