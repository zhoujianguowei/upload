package rpc.thrift;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileSegment;

import java.util.List;
import java.util.Objects;

public class AsyncHelloServerImpl implements Hello.AsyncIface {
    private static volatile AsyncHelloServerImpl instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHelloServerImpl.class);

    private AsyncHelloServerImpl() {

    }

    @Override
    public void helloString(String param, AsyncMethodCallback resultHandler) throws TException {
        LOGGER.debug("async helloString||params={}", param);
        if (StringUtils.isBlank(param)) {
            LOGGER.warn("param is empty");
            resultHandler.onComplete("param is empty");
        } else if (Objects.equals(param, "zbj")) {
            resultHandler.onComplete("welcome to thrift world!!!zbj");
        } else {
            resultHandler.onComplete("welcome||param=" + param);
        }

    }

    public synchronized static AsyncHelloServerImpl getInstance() {
        if (instance == null) {
            instance = new AsyncHelloServerImpl();
        }
        return instance;
    }

    @Override
    public void transferData(FileSegment segment, AsyncMethodCallback<List<FileSegment>> resultHandler) throws TException {
        if (segment == null) {
            LOGGER.warn("content is null");
            resultHandler.onComplete(null);
            return;
        }
        String fileName = segment.getFileName();
        LOGGER.info("filename={}||content size={}", fileName,segment.getContents().length);
        List<FileSegment> segmentList = Lists.newArrayList();
        if (StringUtils.isBlank(fileName) || Objects.equals("empty", fileName)) {
            resultHandler.onComplete(segmentList);
            return;
        }
        if (fileName.contains("large")) {
            FileSegment largeSegment = new FileSegment();
            largeSegment.setContents(new byte[10]);
            largeSegment.setFileName("large_ret");
            segmentList.add(largeSegment);
        } else if (fileName.contains("init")) {
            segmentList.add(segment);
        }
        resultHandler.onComplete(segmentList);
    }
}
