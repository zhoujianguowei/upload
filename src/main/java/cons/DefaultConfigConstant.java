package cons;

import org.apache.thrift.util.StorageFormat;

public class DefaultConfigConstant {
    public static final long UPLOAD_BATCH_BYTES_SIZE = 102400;
    public static final int FILE_BROKER_MAX_RETRY_TIMES = 3;
    public static final int MAX_UPLOAD_PARALLEL_NUM = 5;
    public static final int UPLOAD_SERVER_PORT = 10033;
    public static final int CREATE_CONNECTION_RETRY_TIMES = 5;
    public static final double MAX_UPLOAD_SPEED_BYTES_THRESHOLD = StorageFormat.transformSize("100mb", "byte");
    public static final double FILE_UPLOAD_MAX_RETRY_TIMES = 3;
}
