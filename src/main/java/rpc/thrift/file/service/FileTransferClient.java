package rpc.thrift.file.service;

import handler.DefaultUploadProgressProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worker.AbstractClientWorker;
import worker.DefaultClientWorker;

import java.io.File;

public class FileTransferClient {


    private static final int CONNECTION_TIME_OUT = 50000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferClient.class);


    public void uploadFile(String saveParentPath, String uploadFileOrDirPath, String host, int connectionTimeOut) {
        this.uploadFile(saveParentPath, uploadFileOrDirPath, host, FileTransferServer.FILE_HANDLER_SERVER_PORT, connectionTimeOut);
    }

    /**
     * client begin transfer file
     *
     * @param saveParentPath      文件上传保存到服务端的父目录
     * @param uploadFileOrDirPath 上传文件或者文件夹绝对路径
     * @param host                服务端ip地址
     * @param port                服务端端口
     */
    public void uploadFile(String saveParentPath, String uploadFileOrDirPath, String host, int port, int connectionTimeOut) {
        File file = new File(uploadFileOrDirPath);
        if (!file.exists() || !file.canRead() || !file.canExecute()) {
            throw new IllegalArgumentException(String.format("path %s not exits or can't execute", uploadFileOrDirPath));
        }
        AbstractClientWorker clientWorker = DefaultClientWorker.getSingleTon();
        clientWorker.addUploadProgressFileCallback(new DefaultUploadProgressProgressCallback());
        clientWorker.clientUploadFile(saveParentPath, file, host, FileTransferServer.FILE_HANDLER_SERVER_PORT, CONNECTION_TIME_OUT);
    }

}
