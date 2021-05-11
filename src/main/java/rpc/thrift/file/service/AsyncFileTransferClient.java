package rpc.thrift.file.service;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import rpc.thrift.file.transfer.FileSegment;
import rpc.thrift.file.transfer.FileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class AsyncFileTransferClient {

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

    private static void clientServe(String filePath) {
        TTransport transport = null;
        TSocket tSocket = null;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("path %s not exits", filePath));
        }
        try {
            tSocket = new TSocket(new TConfiguration(), "localhost", AsyncFileTransferServer.ASYNC_HELLO_SERVER_PORT, 5000);
            transport = new TFramedTransport(tSocket);
            // 协议要和服务端一致
            TProtocol protocol = new TCompactProtocol(transport);
            FileTransfer.Client client = new FileTransfer.Client(protocol);
            transport.open();
            String fileName = file.getName();
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int readBytes = -1;
            long pos = 0;
            long fileLength = file.length();
            float ratio = 0;
            NumberFormat numberFormat = new DecimalFormat("##.00%");
            while ((readBytes = fileInputStream.read(buffer)) >= -1) {
                FileSegment fileSegment = new FileSegment();
                fileSegment.setFileName(fileName);
                fileSegment.setIdentifier(fileName);
                fileSegment.setBytesLength(readBytes);
                //indicate transfer over
                if (readBytes == -1) {
                    fileSegment.setContents(new byte[0]);
                } else {
                    fileSegment.setContents(buffer);
                }
                fileSegment.setPos(pos);
                fileSegment.setCheckSum("123");
                pos = client.transferFile(fileSegment, null);
                if (pos == -1) {
                    break;
                }
                float updateRatio = pos * 1.0f / fileLength;
                if (updateRatio - ratio > 0.005) {
                    System.out.println(String.format("transfer ratio:%s", numberFormat.format(updateRatio)));
                    ratio = updateRatio;
                }
            }
        } catch (Exception e) {
            System.out.println(String.format("transfer file exception||filePath=%s", filePath));
            e.printStackTrace();
        } finally {
            if (transport != null && transport.isOpen()) {
                transport.close();
            }
            System.out.println("transfer finished");
        }


    }

    public static void main(String[] args) {
        System.out.println("async client start");
        clientServe("d:/test3.pdf");
    }
}
