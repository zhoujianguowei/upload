package common;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * countdown latch with reset and clear metho
 */
public class ResetCountDownLatch extends CountDownLatch {
    private static Field SYNC_FIELD = null;
    private static Field STATE_FIELD = null;
    protected AbstractQueuedSynchronizer sync = null;

    static {
        try {
            SYNC_FIELD = CountDownLatch.class.getDeclaredField("sync");
            STATE_FIELD = AbstractQueuedSynchronizer.class.getDeclaredField("state");
            SYNC_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
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
        try {
            sync = (AbstractQueuedSynchronizer) SYNC_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void reset() {
        try {
            STATE_FIELD.set(sync, getCount());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void releaseAll() {
        sync.releaseShared((int) getCount());
    }
}
