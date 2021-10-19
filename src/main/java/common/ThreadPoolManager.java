package common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 线程池管理、创建以及销毁
 */
public class ThreadPoolManager {
    /**
     * 同步文件上传进度
     */
    private static final ScheduledExecutorService syncClientUploadProgressScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final ScheduledExecutorService measureUploadRateScheduler=Executors.newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService getSyncClientUploadProgressScheduler() {
        return syncClientUploadProgressScheduler;
    }
    public static ScheduledExecutorService getMeasureUploadRateScheduler(){
        return measureUploadRateScheduler;
    }


}
