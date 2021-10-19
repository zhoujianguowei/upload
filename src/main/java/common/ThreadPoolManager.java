package common;

import org.apache.thrift.util.ExecutorUtil;
import sun.nio.ch.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池管理、创建以及销毁
 */
public class ThreadPoolManager {
    /**
     * 同步文件上传进度
     */
    private static final ScheduledExecutorService syncClientUploadProgressScheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * 打印文件上传速率
     */
    private static final ScheduledExecutorService measureUploadRateScheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * 控制文件并发上传
     */
    private static final ExecutorService parallelUploadThreadPool = Executors.newFixedThreadPool(50);

    public static ScheduledExecutorService getSyncClientUploadProgressScheduler() {
        return syncClientUploadProgressScheduler;
    }

    public static ScheduledExecutorService getMeasureUploadRateScheduler() {
        return measureUploadRateScheduler;
    }

    public static ExecutorService getParallelUploadThreadPool() {
        return parallelUploadThreadPool;
    }

    public static void shutdownCientThreadPool() {
        ExecutorUtil.gracefulShutdown(syncClientUploadProgressScheduler, 1000);
        ExecutorUtil.gracefulShutdown(measureUploadRateScheduler, 1000);
        ExecutorUtil.gracefulShutdown(parallelUploadThreadPool, 1000);
    }

}
