package rpc.thrift;

import org.apache.thrift.TException;
import rpc.thrift.Hello;
import rpc.thrift.file.transfer.FileSegment;

import java.util.List;

public class NonblockHelloServerImpl implements Hello.Iface {
    @Override
    public String helloString(String param) throws TException {
        return "nonBlock||param=" + param;
    }

    @Override
    public List<FileSegment> transferData(FileSegment segment) throws TException {
        return null;
    }
}
