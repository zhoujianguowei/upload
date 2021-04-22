package rpc.thrift;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;

public class NonblockHellocClient {
    public static void main(String[] args) throws Exception {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket("localhost", NonblockHelloServer.NON_BLOCK_SERVER_PORT));
            // 协议要和服务端一致
            TProtocol protocol = new TCompactProtocol(transport);
            Hello.Client client = new Hello.Client(
                    protocol);
            transport.open();
            SimpleHelloClient.clientServe(client);
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
}
