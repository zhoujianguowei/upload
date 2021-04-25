package rpc.thrift;

import org.apache.thrift.TException;
import rpc.thrift.Hello;
import rpc.thrift.file.transfer.FileSegment;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleHelloServerImpl implements Hello.Iface {
    private static AtomicLong requestCnt=new AtomicLong();
    @Override
    public String helloString(String param) throws TException {
        requestCnt.incrementAndGet();
        return "simple||params=" + param;
    }

    @Override
    public List<FileSegment> transferData(FileSegment segment) throws TException {
        return null;
    }
}
