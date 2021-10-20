package common;

import org.apache.thrift.util.ExecutorUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 线程池管理、创建以及销毁
 */
public class ThreadPoolManager {
    /**
     * 同步文件上传进度
     */
    private static final ScheduledExecutorService serverSyncUploadProgressScheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * 打印文件上传速率
     */
    private static final ScheduledExecutorService clientAcquireUploadSpeedScheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * 控制文件并发上传
     */
    private static final ExecutorService clientParallelUploadFileNumExecutorService = Executors.newFixedThreadPool(50);

    public static ScheduledExecutorService getServerSyncUploadProgressScheduler() {
        return serverSyncUploadProgressScheduler;
    }

    public static ScheduledExecutorService getClientAcquireUploadSpeedScheduler() {
        return clientAcquireUploadSpeedScheduler;
    }

    public static ExecutorService getClientParallelUploadFileNumExecutorService() {
        return clientParallelUploadFileNumExecutorService;
    }

}
