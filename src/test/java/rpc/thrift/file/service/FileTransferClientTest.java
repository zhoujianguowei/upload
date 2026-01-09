package rpc.thrift.file.service;

import org.junit.Before;
import org.junit.Test;

public class FileTransferClientTest {

    private String serverHost = "192.168.71.44";
    private FileTransferClient fileTransferClient;
    @Before
    public void before() {
        fileTransferClient = new FileTransferClient();
    }

    @Test
    public void uploadFile() {
        fileTransferClient.uploadFile("/home/zbj/下载/model_configs_2026-01-08.json", serverHost);

    }
}