package rpc.thrift.file.service;

import handler.UploadFileProgressCallback;
import java.io.File;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worker.AbstractClientWorker;
import worker.DefaultClientWorker;

public class FileTransferClient {


    private static final int CONNECTION_TIME_OUT = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferClient.class);
    private UploadFileProgressCallback uploadFileProgressCallback;

    public void uploadFile(String uploadFileOrDirPath, String host) {
        this.uploadFile(uploadFileOrDirPath, host, null);
    }

    public void uploadFile(String uploadFileOrDirPath, String host, String[] nameFilters) {
        this.uploadFile(uploadFileOrDirPath, host, FileTransferServer.FILE_HANDLER_SERVER_PORT, CONNECTION_TIME_OUT, nameFilters);
    }

    public void uploadFile(String uploadFileOrDirPath, String host, int connectionTimeOut) {
        this.uploadFile(uploadFileOrDirPath, host, FileTransferServer.FILE_HANDLER_SERVER_PORT, connectionTimeOut, null);
    }

    /**
     * client begin transfer file
     *
     * @param uploadFileOrDirPath 上传文件或者文件夹绝对路径
     * @param host                服务端ip地址
     * @param port                服务端端口
     */
    public void uploadFile(String uploadFileOrDirPath, String host,
                           int port, int connectionTimeOut, String[] nameFilters) {
        File file = new File(uploadFileOrDirPath);
        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(String.format("path %s not exits or can't execute", uploadFileOrDirPath));
        }
        AbstractClientWorker clientWorker = DefaultClientWorker.getSingleTon();
        if (uploadFileProgressCallback != null) {
            clientWorker.addUploadProgressFileCallback(uploadFileProgressCallback);
        }
        if (ArrayUtils.isNotEmpty(nameFilters)) {
            clientWorker.setNameFilters(nameFilters);
        }
        clientWorker.clientUploadFile(file, host, port, connectionTimeOut);
    }

    public UploadFileProgressCallback getUploadFileProgressCallback() {
        return uploadFileProgressCallback;
    }


    public void setUploadFileProgressCallback(UploadFileProgressCallback uploadFileProgressCallback) {
        this.uploadFileProgressCallback = uploadFileProgressCallback;
    }
}
