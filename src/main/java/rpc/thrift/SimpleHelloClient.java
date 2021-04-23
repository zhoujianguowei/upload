package rpc.thrift;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleHelloClient {
    public static void clientServe(Hello.Client client) throws TException {
        Scanner scanner = new Scanner(System.in);
        String line = null;
        while (!Objects.equals(line, "exit")) {
            line = scanner.nextLine();
            String ret = client.helloString(line);
            System.out.println("from simple server result:" + ret);
        }
    }

    private static void simpleClientServe(AtomicLong reqTotal) {
        TSocket transport = null;
        try {
            System.out.println("simple client start");
            transport = new TSocket(new TConfiguration(), "localhost", SimpleHelloServer.SIMPLE_SERVER_PORT, 3000);
            // 协议要和服务端一致
            TProtocol protocol = new TBinaryProtocol(transport);
            // TProtocol protocol = new TCompactProtocol(transport);
            // TProtocol protocol = new TJSONProtocol(transport);
            Hello.Client client = new Hello.Client(
                    protocol);
            transport.open();
            while (true) {
                client.helloString("nice");
                reqTotal.incrementAndGet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        AsyncHelloClient.measurePressureQPS(SimpleHelloClient::simpleClientServe,100);
    }
}
