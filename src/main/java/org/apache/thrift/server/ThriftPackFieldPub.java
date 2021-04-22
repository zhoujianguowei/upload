package org.apache.thrift.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ThriftPackFieldPub {
    public static Field FRAME_BUFFER_FIELD;
    public static Field TRANS_FIELD;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftPackFieldPub.class);

    static {
        try {
            FRAME_BUFFER_FIELD = Invocation.class.getDeclaredField("frameBuffer");
            FRAME_BUFFER_FIELD.setAccessible(true);
            TRANS_FIELD = AbstractNonblockingServer.FrameBuffer.class.getDeclaredField("trans_");
            TRANS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOGGER.error("exception when get field", e);
            throw new RuntimeException(e);
        }
    }
}
