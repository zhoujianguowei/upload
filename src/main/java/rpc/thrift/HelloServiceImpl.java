package rpc.thrift;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.Hello.AsyncIface;

public class HelloServiceImpl implements AsyncIface {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public void helloString(String param, AsyncMethodCallback<String> resultHandler) throws TException {
        if (StringUtils.isBlank(param)) {
            LOGGER.warn("param is empty");
            resultHandler.onComplete("error");
        } else if (param.equals("zbj")) {
            resultHandler.onComplete("welcome to thrift world," + param);
        } else {
            resultHandler.onComplete("good morning," + param);
        }
    }
}
