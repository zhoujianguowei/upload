package handler;

import com.google.common.collect.Lists;
import groovy.io.FileType;
import rpc.thrift.file.transfer.FileTypeEnum;
import sun.plugin2.jvm.RemoteJVMLauncher;

import java.util.List;

/**
 * 封装了文件上传的聚合方法，对应的客户端和服务端都可以继承改方法
 */
public abstract class AbstractUploadFileProgressCallback implements UploadFileProgressCallback {
    protected List<UploadFileProgressCallback> uploadFileProgressCallbackList = Lists.newCopyOnWriteArrayList();
    /**
     * 终端类型，客户端还是服务端
     */
    protected String terminalType;

    public AbstractUploadFileProgressCallback(String terminalType) {
        this.terminalType = terminalType;
    }

    public void addUploadProgressFileCallback(UploadFileProgressCallback callback) {
        uploadFileProgressCallbackList.add(callback);
    }

    public void removeUploadProgressFileCallback(UploadFileProgressCallback callback) {
        uploadFileProgressCallbackList.remove(callback);
    }

    @Override
    public void onFileUploadFinish(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        for (UploadFileProgressCallback callback : uploadFileProgressCallbackList) {
            callback.onFileUploadFinish(fileIdentifier, filePath, fileType);
        }
    }

    @Override
    public void onFileUploadFail(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        for (UploadFileProgressCallback callback : uploadFileProgressCallbackList) {
            callback.onFileUploadFinish(fileIdentifier, filePath, fileType);
        }
    }

    @Override
    public void onFileCancel(String fileIdentifier, String filePath, FileTypeEnum fileType) {
        for (UploadFileProgressCallback callback : uploadFileProgressCallbackList) {
            callback.onFileCancel(fileIdentifier, filePath, fileType);
        }
    }

    @Override
    public void onFileUploadProgress(String fileIdentifier, String filePath, long fileBytesLength, long uploadFileBytesLength) {
        for (UploadFileProgressCallback callback : uploadFileProgressCallbackList) {
            callback.onFileUploadProgress(fileIdentifier, filePath, fileBytesLength, uploadFileBytesLength);
        }
    }
}
