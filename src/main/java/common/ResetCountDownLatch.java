package common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * countdown latch with reset and clear metho
 */
public class ResetCountDownLatch extends CountDownLatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetCountDownLatch.class);
    private static Field SYNC_FIELD = null;
    private static Field STATE_FIELD = null;
    protected AbstractQueuedSynchronizer sync = null;
    private static Method SET_STATE_METHOD = null;
    private volatile int initCount;

    static {
        try {
            SET_STATE_METHOD = AbstractQueuedSynchronizer.class.getDeclaredMethod("setState", int.class);
            SYNC_FIELD = CountDownLatch.class.getDeclaredField("sync");
            STATE_FIELD = AbstractQueuedSynchronizer.class.getDeclaredField("state");
            SET_STATE_METHOD.setAccessible(true);
            SYNC_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *              before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public ResetCountDownLatch(int count) {
        super(count);
        initCount = count;
        try {
            sync = (AbstractQueuedSynchronizer) SYNC_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void reset() {
        try {
            SET_STATE_METHOD.invoke(sync, initCount);
        } catch (Exception e) {
            LOGGER.error("reset error", e);
        }
    }

    public void releaseAll() {
        try {
            SET_STATE_METHOD.invoke(sync, 0);
        } catch (IllegalAccessException e) {
            LOGGER.error("releaseAll error", e);
        } catch (InvocationTargetException e) {
            LOGGER.error("releaseAll error", e);
        }
    }
}
