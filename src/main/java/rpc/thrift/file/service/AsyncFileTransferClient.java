package rpc.thrift.file.service;

import handler.DefaultUploadProgressCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worker.AbstractClientWorker;
import worker.DefaultClientWorker;

import java.io.File;

public class AsyncFileTransferClient {


    private static final int CONNECTION_TIME_OUT = 500000;
    private volatile boolean connectionOpen = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFileTransferClient.class);


    /**
     * client begin transfer file
     *
     * @param filePath file absolute path
     * @param host     file server host
     */
    private static void uploadFile(String filePath, String host) {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead() || !file.canExecute()) {
            throw new IllegalArgumentException(String.format("path %s not exits or can't execute", filePath));
        }
        AbstractClientWorker clientWorker = DefaultClientWorker.getSingleTon();
        clientWorker.setUploadFileCallBack(new DefaultUploadProgressCallBack());
        clientWorker.clientUploadFile("d:/cpTest", file, host, AsyncFileTransferServer.FILE_HANDLER_SERVER_PORT, CONNECTION_TIME_OUT);
    }

    public static void main(String[] args) {
        System.out.println("async client start");
        uploadFile("d:/test", "localhost");
    }
}
