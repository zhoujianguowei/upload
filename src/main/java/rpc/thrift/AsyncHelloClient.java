package rpc.thrift;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;

public class AsyncHelloClient {

    public static void main(String[] args) {
        System.out.println("async client start");
        TTransport transport = null;
        try {
            TSocket tSocket = new TSocket(new TConfiguration(), "localhost", AsyncHelloServer.ASYNC_HELLO_SERVER_PORT, 5000);
            transport = new TFramedTransport(tSocket);
            // 协议要和服务端一致
            TProtocol protocol = new TCompactProtocol(transport);
            Hello.Client client = new Hello.Client(protocol);
            transport.open();
            SimpleHelloClient.clientServe(client);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != transport && transport.isOpen()) {
                transport.close();
            }
        }
    }
}
