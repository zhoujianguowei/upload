package rpc.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

public class SimpleHelloServer {
    public static final int SIMPLE_SERVER_PORT=9900;
    public static void main(String[] args) throws TTransportException {
        System.out.println("simple server start");
        TProcessor tprocessor = new Hello.Processor<Hello.Iface>(
                new SimpleHelloServerImpl());

        // 简单的单线程服务模型，一般用于测试
        TServerSocket serverTransport = new TServerSocket(SIMPLE_SERVER_PORT);
        TServer.Args tArgs = new TServer.Args(serverTransport);
        tArgs.processor(tprocessor);
        tArgs.protocolFactory(new TBinaryProtocol.Factory());
        // tArgs.protocolFactory(new TCompactProtocol.Factory());
        // tArgs.protocolFactory(new TJSONProtocol.Factory());
        TServer server = new TSimpleServer(tArgs);
        server.serve();
    }
}
