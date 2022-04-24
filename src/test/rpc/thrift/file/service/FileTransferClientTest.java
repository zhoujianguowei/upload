package rpc.thrift.file.service;

import org.junit.Before;
import org.junit.Test;

public class FileTransferClientTest {

    private String serverHost = "192.168.43.71";
    private FileTransferClient fileTransferClient;

    @Before
    public void before() {
        fileTransferClient = new FileTransferClient();
    }

    @Test
    public void uploadFile() {
        fileTransferClient.uploadFile("/Users/zhoubenjin/Documents/video", "E:\\youtube\\武庚纪", serverHost);

    }
}