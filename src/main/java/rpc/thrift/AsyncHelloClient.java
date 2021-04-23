package rpc.thrift;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class AsyncHelloClient {

    public static void measurePressureQPS(Consumer<AtomicLong> consumer, int workerThreads) {
        ScheduledExecutorService singleSchedulePool = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorService = Executors.newFixedThreadPool(workerThreads);
        AtomicLong reqTotal = new AtomicLong();
        AtomicLong preReqTotal = new AtomicLong();
        singleSchedulePool.scheduleAtFixedRate(() -> {
            long currentReq = reqTotal.get();
            long preReq = preReqTotal.get();
            System.out.println("request qps=" + (currentReq - preReq));
            preReqTotal.set(currentReq);
        }, 1, 1, TimeUnit.SECONDS);
        for (int i = 0; i < workerThreads; i++) {
            executorService.submit(() -> {
                consumer.accept(reqTotal);
            });
        }

    }

    private static void clientServe(AtomicLong reqTotal) {
        TTransport transport = null;
        try {
            TSocket tSocket = new TSocket(new TConfiguration(), "localhost", AsyncHelloServer.ASYNC_HELLO_SERVER_PORT, 5000);
            transport = new TFramedTransport(tSocket);
            // 协议要和服务端一致
            TProtocol protocol = new TCompactProtocol(transport);
            Hello.Client client = new Hello.Client(protocol);
            transport.open();
            while (true) {
                String result = client.helloString("nice");
                reqTotal.incrementAndGet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != transport && transport.isOpen()) {
                transport.close();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("async client start");
        measurePressureQPS(AsyncHelloClient::clientServe, 1);
    }
}
