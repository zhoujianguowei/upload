package common;


import com.google.common.collect.Maps;
import config.ConfigDataHelper;
import cons.BusinessConstant;
import handler.RetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 重试策略，提供了一套默认的模板方法，模板方法内部处理了非异常的重试机智
 *
 * @param <B>
 * @param <T>
 */
public abstract class AbstractRetryStrategy<B, T> implements RetryStrategy<B, T> {

    /**
     * 记录内部重试信息,key表示重试类型，val表示目前为止重试次数
     */
    protected Map<String, AtomicInteger> retryRecordInfoMap = Maps.newConcurrentMap();
    /**
     * 最大重试次数
     */
    protected static final int MAX_RETRY_COUNT = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.FILE_UPLOAD_MAX_RETRY_COUNT));
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRetryStrategy.class);

    @Override
    public RetryStrategyEnum doRetryOrNot(B o, Function<B, String> function, T t) {
        Objects.requireNonNull(o);
        Objects.requireNonNull(function);
        Objects.requireNonNull(t);
        if (t instanceof Exception) {
            return handleExceptionRetryStrategy(o, function, (Exception) t);
        }
        String retryType = function.apply(o);
        int retryCount = retryRecordInfoMap.getOrDefault(retryType, new AtomicInteger()).incrementAndGet();
        LOGGER.warn("retry ||retryType={}||retryCount={}||maxRetryCount={}", retryType, retryCount, MAX_RETRY_COUNT);
        if (retryCount > MAX_RETRY_COUNT) {
            LOGGER.error("retry times exceed max retry times,abort");
            return RetryStrategyEnum.ABORT;
        }
        return RetryStrategyEnum.SIMPLE_RETRY;
    }

    /**
     * 子类需要处理异常类重试
     *
     * @param o
     * @param e
     * @return
     */
    public abstract RetryStrategyEnum handleExceptionRetryStrategy(B o, Function<B, String> function, Exception e);
}
