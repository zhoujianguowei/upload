package rpc.thrift;

import org.apache.thrift.TException;
import rpc.thrift.Hello;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleHelloServerImpl implements Hello.Iface {
    private static AtomicLong requestCnt=new AtomicLong();
    @Override
    public String helloString(String param) throws TException {
        requestCnt.incrementAndGet();
        return "simple||params=" + param;
    }
}
