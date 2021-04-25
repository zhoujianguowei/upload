package rpc.thrift;

import com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import rpc.thrift.file.transfer.FileSegment;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;
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
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            while (!Objects.equals("exit", line)) {
                FileSegment segment = null;
                switch (line) {
                    case "large":
                        segment = new FileSegment();
                        segment.setContents(new byte[1024]);
                        segment.setFileName(line);
                        break;
                    case "empty":
                        segment = new FileSegment();
                        segment.setContents(new byte[10]);
                        segment.setFileName(line);
                        break;
                    case "init":
                        segment = new FileSegment();
                        segment.setContents(new byte[100]);
                        segment.setFileName(line);
                        break;
                    default:
                        segment=new FileSegment();
                        segment.setContents(new byte[0]);
                        break;
                }
                List<FileSegment> segmentList = client.transferData(segment);
                if (segmentList == null) {
                    System.out.println("ret result is null");
                } else if (segmentList.isEmpty()) {
                    System.out.println("ret result is empty");
                } else {
                    System.out.println("ret content size=" + segmentList.get(0).getContents().length);
                }
                reqTotal.incrementAndGet();
                line=scanner.nextLine();
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
        clientServe(new AtomicLong());
    }
}
