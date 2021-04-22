package rpc.thrift;

import org.apache.thrift.TException;
import rpc.thrift.Hello;

public class NonblockHelloServerImpl implements Hello.Iface {
    @Override
    public String helloString(String param) throws TException {
        return "nonBlock||param=" + param;
    }
}
