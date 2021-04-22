package rpc.thrift;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AsyncHelloServerImpl implements Hello.AsyncIface {
    private static volatile AsyncHelloServerImpl instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHelloServerImpl.class);

    private AsyncHelloServerImpl() {

    }

    @Override
    public void helloString(String param, AsyncMethodCallback<String> resultHandler) throws TException {
        LOGGER.info("async helloString||params={}", param);
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
}
