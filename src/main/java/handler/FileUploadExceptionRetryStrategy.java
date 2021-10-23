package handler;

import common.AbstractRetryStrategy;
import common.RetryStrategyEnum;
import cons.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileUploadRequest;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 文件上传异常重试策略
 */
public class FileUploadExceptionRetryStrategy extends AbstractRetryStrategy<FileUploadRequest, Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadExceptionRetryStrategy.class);

    public RetryStrategyEnum handleExceptionRetryStrategy(FileUploadRequest fileUploadRequest, Function<FileUploadRequest, String> function, Exception e) {
        String retryType = StringUtils.join(new String[]{fileUploadRequest.getIdentifier(), e.getClass().getSimpleName()}, CommonConstant.UNDERLINE);
        int retryCount = retryRecordInfoMap.getOrDefault(retryType, new AtomicInteger(0)).incrementAndGet();
        LOGGER.info("upload file exception||retryType={}||retryCount={}", retryType, retryCount);
        if (e instanceof TException) {
            if (retryCount > MAX_RETRY_COUNT) {
                LOGGER.error("thrift retry times exceed limit,create new connection and abort||retryType={}||currentRetryCount={}||maxRetryCount={}",
                        retryType, retryCount, MAX_RETRY_COUNT);
                return RetryStrategyEnum.RECREATE_CONNECTION_THEN_ABORT;
            } else {
                LOGGER.warn("thrift exception ,create new connection and do retry");
                return RetryStrategyEnum.RECREATE_CONNECTION_THEN_RETRY;
            }
        } else {
            if (retryCount > MAX_RETRY_COUNT) {
                LOGGER.error("retry times exceed limit,abort||retryType={}||currentRetryCount={}||maxRetryCount={}",
                        retryType, retryCount, MAX_RETRY_COUNT);
                return RetryStrategyEnum.ABORT;
            }
            LOGGER.warn("just simple retry");
            return RetryStrategyEnum.SIMPLE_RETRY;
        }
    }
}
