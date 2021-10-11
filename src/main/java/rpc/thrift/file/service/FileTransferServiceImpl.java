package rpc.thrift.file.service;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import rpc.thrift.file.transfer.FileTransferWorker;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import worker.AbstractFileHandler;
import worker.DefaultFileHandler;

public class FileTransferServiceImpl implements FileTransferWorker.AsyncIface {
    private static FileTransferServiceImpl instance;


    public synchronized static FileTransferServiceImpl getInstance() {
        if (instance == null) {
            instance = new FileTransferServiceImpl();
        }
        return instance;
    }

    @Override
    public void uploadFile(FileUploadRequest request, String token, AsyncMethodCallback<FileUploadResponse> resultHandler) throws TException {
        AbstractFileHandler abstractFileHandler = new DefaultFileHandler();
        FileUploadResponse uploadResponse = abstractFileHandler.handleUploadFile(request, token);
        resultHandler.onComplete(uploadResponse);
    }
}
