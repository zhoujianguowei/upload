package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
     * 控制文件并发上传,最大线程数50
     */
    private static final ExecutorService clientParallelUploadFileNumExecutorService =new ThreadPoolExecutor(50,50,60,
            TimeUnit.SECONDS,new LinkedBlockingDeque<>());

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
