package rpc.thrift.file.service;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import rpc.thrift.file.transfer.FileTransferWorker;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AsyncFileTransferClient {


    /**
     * client begin transfer file
     * @param filePath  file absolute path
     * @param host file server host
     */
    private static void clientServe(String filePath,String host) {
        TTransport transport = null;
        TSocket tSocket = null;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("path %s not exits", filePath));
        }
        try {
            tSocket = new TSocket(new TConfiguration(), host, AsyncFileTransferServer.ASYNC_HELLO_SERVER_PORT, 5000);
            transport = new TFramedTransport(tSocket);
            // 协议要和服务端一致
            TProtocol protocol = new TCompactProtocol(transport);
            FileTransferWorker.Client client = new FileTransferWorker.Client(protocol);
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
//                FileSegment fileSegment = new FileSegment();
//                fileSegment.setFileName(fileName);
//                fileSegment.setIdentifier(fileName);
//                fileSegment.setBytesLength(readBytes);
//                //indicate transfer over
//                if (readBytes == -1) {
//                    fileSegment.setContents(new byte[0]);
//                } else {
//                    fileSegment.setContents(buffer);
//                }
//                fileSegment.setPos(pos);
//                fileSegment.setCheckSum("123");
//                pos = client.transferFile(fileSegment, null);
//                if (pos == -1) {
//                    break;
//                }
//                float updateRatio = pos * 1.0f / fileLength;
//                if (updateRatio - ratio > 0.005) {
//                    System.out.println(String.format("transfer ratio:%s", numberFormat.format(updateRatio)));
//                    ratio = updateRatio;
//                }
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
        clientServe("d:/test3.pdf","localhost");
    }
}
