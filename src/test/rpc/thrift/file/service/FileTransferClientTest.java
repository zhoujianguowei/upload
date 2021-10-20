package rpc.thrift.file.service;

import org.junit.Before;
import org.junit.Test;

public class FileTransferClientTest {

    private String serverHost = "192.168.0.110";
    private FileTransferClient fileTransferClient;
    @Before
    public void before() {
        fileTransferClient = new FileTransferClient();
    }

    @Test
    public void uploadFile() {
        fileTransferClient.uploadFile("E:/test/aabbbcc", "E:/课件/面试", serverHost);

    }
}