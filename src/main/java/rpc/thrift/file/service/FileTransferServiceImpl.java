package rpc.thrift.file.service;

import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileSegmentRequest;
import rpc.thrift.file.transfer.FileSegmentResponse;
import rpc.thrift.file.transfer.FileTransferWorker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class FileTransferServiceImpl implements FileTransferWorker.AsyncIface {
    private static FileTransferServiceImpl instance;
    private Map<String, File> cacheFileMap = Maps.newConcurrentMap();
    private Map<String, FileOutputStream> fileOutputStreamMap = Maps.newConcurrentMap();
    private Map<String, Long> fileFilePosMap = Maps.newConcurrentMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferServiceImpl.class);

    @Override
    public void transferFile(FileSegmentRequest segment, String token, AsyncMethodCallback<FileSegmentResponse> resultHandler) throws TException {
        String identifier = segment.getIdentifier();
        String fileName = segment.getFileName();
        File file = cacheFileMap.computeIfAbsent(identifier, k -> new File(fileName));
        FileOutputStream outputStream = null;
        long currentPos = fileFilePosMap.computeIfAbsent(identifier, k -> 0L);
        if (!file.exists()) {
            try {
                file.createNewFile();
                fileOutputStreamMap.computeIfAbsent(identifier, k -> {
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return fileOutputStream;
                });
                LOGGER.info("file start transfer||fileName={}||identifier={}", fileName, identifier);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputStream = fileOutputStreamMap.get(identifier);
        int length = segment.getBytesLength();
        if (length <= 0) {
            try {
                outputStream.close();
                cacheFileMap.remove(identifier);
                fileOutputStreamMap.remove(identifier);
                fileFilePosMap.remove(identifier);
                LOGGER.info("file transfer finished||fileName={}||identifier={}", fileName, identifier);
            } catch (IOException e) {
                LOGGER.error("exception", e);
            } finally {
//                resultHandler.onComplete(-1L);
            }
        } else {
            byte[] contents = segment.getContents();
            try {
                outputStream.write(contents, 0, length);
            } catch (IOException e) {
                LOGGER.error("exception occurred", e);
            } finally {
                fileFilePosMap.put(identifier, currentPos + length);
//                resultHandler.onComplete(currentPos + length);
            }
        }
    }

    public synchronized static FileTransferServiceImpl getInstance() {
        if (instance == null) {
            instance = new FileTransferServiceImpl();
        }
        return instance;
    }
}
