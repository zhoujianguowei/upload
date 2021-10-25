package handler;

import common.RetryStrategyEnum;

import java.util.function.Function;

/**
 * 客户端上传或者下载内部充实策略
 *
 * @param <B> b代表输入的参数，对于文件上传，就是上传的请求参数或者是从请求参数
 * @param <T> 异常码或者状态码
 */
public interface RetryStrategy<B, T> {
    /**
     * @param b
     * @param function 从输入参数b能够提取对应标识
     * @param t
     * @return
     */
    RetryStrategyEnum doRetryOrNot(B b, Function<B, String> function, T t);
}
