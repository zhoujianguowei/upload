package worker;

import common.ClientUploadStatus;
import common.FileHandlerHelper;
import config.ConfigDataHelper;
import cons.BusinessConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTransferWorker;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static rpc.thrift.file.transfer.ResResult.FILE_END;

public class DefaultClientWorker extends AbstractClientWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientWorker.class);


    private DefaultClientWorker(String terminalType) {
        super(terminalType);
    }

    private static class InnerInstance {
        static DefaultClientWorker outerInstance = new DefaultClientWorker(BusinessConstant.CLIENT_TERMINAL_TYPE);
    }

    public static DefaultClientWorker getSingleTon() {
        return InnerInstance.outerInstance;
    }

    protected ClientUploadStatus handleUploadDir(FileUploadRequest request, File uploadSingleFileOrDir, FileTransferWorker.Client client) throws Exception {
        FileUploadResponse fileUploadResponse = client.uploadFile(request, FileHandlerHelper.generateFileToken(uploadSingleFileOrDir.getName()));
        if (fileUploadResponse.getUploadStatusResult() != FILE_END) {
            LOGGER.warn("failed to upload file||response={}", fileUploadResponse);
            return ClientUploadStatus.FAIL;
        }
        return ClientUploadStatus.UPLOAD_FINISH;
    }

    protected ClientUploadStatus handleUploadFile(FileUploadRequest fileUploadRequest, String saveParentPath, String relativePath, File uploadSingleFileOrDir,
                                                  RandomAccessFile randomAccessFile, FileTransferWorker.Client client) throws Exception {
        long startPos = 0L;
        //文件传输失败内容错误最大重试次数
        int brokerRetryTimes = 3;
        long preBrokerOffset = -1L;
        String absoluteFilePath = uploadSingleFileOrDir.getAbsolutePath();
        int maxBrokerRetryTimes = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES));
        while (true) {
            FileUploadResponse uploadResult = client.uploadFile(fileUploadRequest, FileHandlerHelper.generateFileToken(uploadSingleFileOrDir.getName()));
            ResResult resResult = uploadResult.getUploadStatusResult();
            switch (resResult) {
                case FILE_END:
                    return ClientUploadStatus.UPLOAD_FINISH;
                case SUCCESS:
                    startPos = uploadResult.nextPos;
                    fileUploadRequest = constructFileUploadRequest(saveParentPath, relativePath, uploadSingleFileOrDir, startPos, randomAccessFile);
                    onFileUploadProgress(fileUploadRequest.getIdentifier(),
                            absoluteFilePath, fileUploadRequest.getTotalFileLength(), uploadResult.nextPos);
                    break;
                case FILE_BROKEN:
                    LOGGER.warn("file upload content broker,retry again");
                    if (startPos == preBrokerOffset) {
                        brokerRetryTimes++;
                    } else {
                        brokerRetryTimes = 1;
                        preBrokerOffset = startPos;
                    }
                    if (preBrokerOffset > maxBrokerRetryTimes) {
                        LOGGER.error("file broker try times exceed default max try times||currentRetryTimes={}||maxDefaultRetryTimes={}||brokerOffset={}",
                                brokerRetryTimes, maxBrokerRetryTimes, preBrokerOffset);
                        return ClientUploadStatus.FAIL;
                    }
                    fileUploadRequest = constructFileUploadRequest(saveParentPath, relativePath, uploadSingleFileOrDir, startPos, randomAccessFile);
                    break;
                default:
                    LOGGER.error("upload result fail,retMsg={}", uploadResult.errorMsg);
                    throw new RuntimeException("upload fail");
            }
        }
    }

    /**
     * 上传单个文件或者文件夹
     *
     * @param saveParentPath
     * @param relativePath
     * @param uploadSingleFileOrDir
     * @param client
     * @return
     */
    @Override
    public ClientUploadStatus doUploadSingleFile(String saveParentPath, String relativePath,
                                                 File uploadSingleFileOrDir, FileTransferWorker.Client client, FileUploadRequest[] fileUploadRequests) throws Exception {
        RandomAccessFile randomAccessFile = null;
        long nextUploadPos = 0L;
        FileUploadRequest fileUploadRequest = null;

        ClientUploadStatus uploadStatus = ClientUploadStatus.UPLOAD_FINISH;
        if (uploadSingleFileOrDir.isFile()) {
            randomAccessFile = new RandomAccessFile(uploadSingleFileOrDir, "r");
        }
        fileUploadRequest = constructFileUploadRequest(saveParentPath, relativePath, uploadSingleFileOrDir, nextUploadPos, randomAccessFile);
        RandomAccessFile finalRandomAccessFile = randomAccessFile;
        hookMap.put(fileUploadRequest.getIdentifier(), () -> {
            try {
                finalRandomAccessFile.close();
            } catch (IOException e) {
                LOGGER.error("random io exception||filePath=" + uploadSingleFileOrDir.getAbsolutePath(), e);
            }
        });
        fileUploadRequests[0] = fileUploadRequest;
        switch (fileUploadRequest.getFileType()) {
            case DIR_TYPE:
                uploadStatus = handleUploadDir(fileUploadRequest, uploadSingleFileOrDir, client);
                break;
            case FILE_TYPE:
                uploadStatus = handleUploadFile(fileUploadRequest, saveParentPath, relativePath, uploadSingleFileOrDir, randomAccessFile, client);
                break;
        }
        return uploadStatus;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
