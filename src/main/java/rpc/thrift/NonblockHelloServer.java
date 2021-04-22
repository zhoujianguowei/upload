package rpc.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;

public class NonblockHelloServer {
    public static final int NON_BLOCK_SERVER_PORT=8123;
    public static void main(String[] args) throws TTransportException {
        System.out.println("Hello TNonblockingServer start ....");

        TProcessor tprocessor = new Hello.Processor<Hello.Iface>(
                new NonblockHelloServerImpl());

        TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(NON_BLOCK_SERVER_PORT);
        TNonblockingServer.Args tnbArgs = new TNonblockingServer.Args(
                tnbSocketTransport);
        tnbArgs.processor(tprocessor);
        tnbArgs.transportFactory(new TFramedTransport.Factory());
        tnbArgs.protocolFactory(new TCompactProtocol.Factory());

        // 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
        TServer server = new TNonblockingServer(tnbArgs);
        server.serve();
    }
}
