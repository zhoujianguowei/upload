package rpc.thrift.file.service;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import rpc.thrift.file.transfer.FileTransferWorker;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import worker.AbstractServerHandler;
import worker.DefaultServerHandler;

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
        AbstractServerHandler abstractServerHandler = DefaultServerHandler.getSingleTon();
        FileUploadResponse uploadResponse = abstractServerHandler.handleUploadFile(request, token);
        resultHandler.onComplete(uploadResponse);
    }
}
