package common;

import org.apache.thrift.util.ExecutorUtil;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResetCountDownLatchTest {

    @Test
    public void reset() {
        ResetCountDownLatch resetCountDownLatch = new ResetCountDownLatch(10);
        ExecutorService service = Executors.newFixedThreadPool(10);
        int i = 0;
        for (; i < 10; i++) {
            int finalI = i;
            service.execute(() -> {
                System.out.println("test" + finalI);
                resetCountDownLatch.countDown();
            });
        }
        try {
            System.out.println("main thread is waiting");
            resetCountDownLatch.await();
            System.out.println("main thread finish round1");
            resetCountDownLatch.reset();
            System.out.println("reset countdown");
            System.out.println("main thread is waiting round2");
            for (; i < 20; i++) {
                int finalI = i;
                service.execute(() -> {
                    System.out.println("test" + finalI);
                    resetCountDownLatch.countDown();
                });
            }
            resetCountDownLatch.await();
            System.out.println("main thread finish round2");
            ExecutorUtil.gracefulShutdown(service,1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void releaseAll() {
        ResetCountDownLatch resetCountDownLatch = new ResetCountDownLatch(1000);
        try {
            ExecutorService service = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                service.execute(() -> {
                    System.out.println("test" + finalI);
                    if (finalI == 2) {
                        System.out.println("begin to release all");
                        resetCountDownLatch.releaseAll();
                        System.out.println("release all finish");
                    }
                });
            }
            System.out.println("main thread is waiting");
            resetCountDownLatch.await();
            System.out.println("main thread finish");
            ExecutorUtil.gracefulShutdown(service,1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}