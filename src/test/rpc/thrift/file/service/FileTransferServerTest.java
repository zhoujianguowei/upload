package rpc.thrift.file.service;

import org.junit.Test;

public class FileTransferServerTest {
    @Test
    public void asyncLaunchFileHandlerService() throws Exception {
        FileTransferServer fileTransferServer = FileTransferServer.getSingleTon();
        fileTransferServer.asyncLaunchFileHandlerService();
    }

}