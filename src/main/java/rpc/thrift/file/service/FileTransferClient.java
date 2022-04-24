package rpc.thrift.file.service;

import com.google.common.util.concurrent.Service;
import handler.UploadFileProgressCallback;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worker.AbstractClientWorker;
import worker.DefaultClientWorker;

import java.io.File;

public class FileTransferClient {


    private static final int CONNECTION_TIME_OUT = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferClient.class);
    private UploadFileProgressCallback uploadFileProgressCallback;
    /**
     * 客户端上传状态，只有两种状态，开始和结束标示
     */
    private Service.State state = Service.State.NEW;

    public void uploadFile(String uploadFileOrDirPath, String host) {
        this.uploadFile(null, uploadFileOrDirPath, host);
    }

    public void uploadFile(String uploadFileOrDirPath, String host, String[] nameFilters) {
        this.uploadFile(null, uploadFileOrDirPath, host, FileTransferServer.FILE_HANDLER_SERVER_PORT, CONNECTION_TIME_OUT, nameFilters);
    }

    public void uploadFile(String saveParentPath, String uploadFileOrDirPath, String host) {
        this.uploadFile(saveParentPath, uploadFileOrDirPath, host, CONNECTION_TIME_OUT);
    }

    public void uploadFile(String saveParentPath, String uploadFileOrDirPath, String host, int connectionTimeOut) {
        this.uploadFile(saveParentPath, uploadFileOrDirPath, host, FileTransferServer.FILE_HANDLER_SERVER_PORT, connectionTimeOut, null);
    }

    /**
     * client begin transfer file
     *
     * @param saveParentPath      文件上传保存到服务端的父目录
     * @param uploadFileOrDirPath 上传文件或者文件夹绝对路径
     * @param host                服务端ip地址
     * @param port                服务端端口
     */
    public synchronized void uploadFile(String saveParentPath, String uploadFileOrDirPath, String host,
                                        int port, int connectionTimeOut, String[] nameFilters) {
        File file = new File(uploadFileOrDirPath);
        if (Service.State.NEW != state) {
            LOGGER.error("client has stopped");
            throw new RuntimeException("client has been stopped");
        }
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
        clientWorker.clientUploadFile(saveParentPath, file, host, port, connectionTimeOut);
    }

    public UploadFileProgressCallback getUploadFileProgressCallback() {
        return uploadFileProgressCallback;
    }

    public void setUploadFileProgressCallback(UploadFileProgressCallback uploadFileProgressCallback) {
        this.uploadFileProgressCallback = uploadFileProgressCallback;
    }

    /**
     * 结束客户端上传，客户上传完成后，上传进程终止
     */
    public synchronized void shutdown() {
        if (state == Service.State.TERMINATED) {
            LOGGER.warn("upload client has been stopped");
            return;
        }
        DefaultClientWorker.getSingleTon().shutdown();
        LOGGER.info("upload client shutdown success");
    }
}
