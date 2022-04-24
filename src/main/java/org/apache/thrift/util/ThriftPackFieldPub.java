package org.apache.thrift.util;

import org.apache.thrift.server.AbstractNonblockingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ThriftPackFieldPub {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftPackFieldPub.class);
    public static Field FRAME_BUFFER_FIELD;
    public static Field TRANS_FIELD;

    static {
        try {
            Class invocationClass = Class.forName("org.apache.thrift.server.Invocation");
            FRAME_BUFFER_FIELD = invocationClass.getDeclaredField("frameBuffer");
            FRAME_BUFFER_FIELD.setAccessible(true);
            TRANS_FIELD = AbstractNonblockingServer.FrameBuffer.class.getDeclaredField("trans_");
            TRANS_FIELD.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("exception when get field", e);
            throw new RuntimeException(e);
        }
    }
}
