package worker;

import common.ClientUploadStatus;
import common.FileHandlerHelper;
import config.ConfigDataHelper;
import cons.BusinessConstant;
import cons.CommonConstant;
import handler.ClientUploadManager;
import handler.UploadFileCallBack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.Collection;

import static rpc.thrift.file.transfer.ResResult.FILE_END;

/**
 * 客户端处理文件上传、下载服务类
 */
public abstract class AbstractClientWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientWorker.class);
    protected ClientUploadManager clientUploadManager;
    protected UploadFileCallBack uploadFileCallBack;


    public UploadFileCallBack getUploadFileCallBack() {
        return uploadFileCallBack;
    }

    public void setUploadFileCallBack(UploadFileCallBack uploadFileCallBack) {
        this.uploadFileCallBack = uploadFileCallBack;
    }

    protected FileUploadRequest constructFileUploadRequest(String saveParentPath, String relativePath, File file, long startPos, RandomAccessFile accessFile) throws IOException {
        FileUploadRequest request = new FileUploadRequest();
        request.setFileName(file.getName());
        request.setSaveParentFolder(saveParentPath);
        request.setRelativePath(relativePath);
        String wholeFilePath = FileHandlerHelper.generateWholePath(saveParentPath, relativePath, file.getName());
        //目录和文件的标识不同，目录只需要完全路径即可，文件需要添加上文件字节大小
        if (file.isDirectory()) {
            request.setIdentifier(FileHandlerHelper.generateUniqueIdentifier(wholeFilePath));
            request.setFileType(FileTypeEnum.DIR_TYPE);
            request.setCheckSum("0L");
        } else {
            request.setIdentifier(FileHandlerHelper.generateUniqueIdentifier(wholeFilePath + CommonConstant.UNDERLINE + file.length()));
            request.setFileType(FileTypeEnum.FILE_TYPE);
            request.setStartPos(startPos);
            request.setTotalFileLength(file.length());
            int perUploadSegmentLength = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH));
            byte[] segmentContents = new byte[perUploadSegmentLength];
            accessFile.seek(startPos);
            int readBytesLength = accessFile.read(segmentContents);
            request.setContents(segmentContents);
            request.setBytesLength(readBytesLength);
            request.setCheckSum(FileHandlerHelper.generateContentsCheckSum(segmentContents, readBytesLength));
        }
        return request;
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
    public abstract ClientUploadStatus doUploadSingleFile(String saveParentPath, String relativePath, File uploadSingleFileOrDir, FileTransferWorker.Client client);


    /**
     * 文件或者文件夹上传上传
     *
     * @param saveParentPath 服务端保存的路径，绝对路径
     * @param file
     * @param remoteHost
     * @param remotePort
     * @return
     */
    public final void clientUploadFile(String saveParentPath, File file, String remoteHost, int remotePort, int connectionTimeout) {
        if (!file.exists() || !file.canExecute()) {
            throw new RuntimeException("file path not exists or no execute permission");
        }
        RemoteRpcNode remoteRpcNode = new RemoteRpcNode(remoteHost, remotePort, connectionTimeout);
        boolean createConn = remoteRpcNode.createRemoteConnectionIfNotExists();
        if (!createConn) {
            LOGGER.warn("failed to create remote connection||host={}||port={}", remoteHost, remotePort);
            return;
        }
        Collection<File> fileLists = new ArrayList<>();
        if (!file.isDirectory()) {
            fileLists.add(file);
        } else {
            fileLists = FileUtils.listFilesAndDirs(file, TrueFileFilter.TRUE, DirectoryFileFilter.DIRECTORY);
        }
        String rootPath = file.getAbsolutePath();
        //根目录或者文件路径
        String rootFileName = file.getName();
        String parentFilePath = StringUtils.isBlank(file.getParent()) ? "" : file.getParent();
        for (File uploadFile : fileLists) {
            if (uploadFile.isFile() && uploadFile.length() <= 0L) {
                LOGGER.warn("file lack content,ignore||filePath={}", uploadFile.getAbsolutePath());
                continue;
            }
            FileTransferWorker.Client client = remoteRpcNode.getRemoteClient();
            String fileAbsolutePath = uploadFile.getAbsolutePath();
            //获取相对于根目录的相对路径,比如当前根目录是d:/test，对应子目录是d:/test/nice/mv.mp4，那么相对路径就是test/nice
            String relativePath = null;
            if (fileAbsolutePath.length() > rootPath.length()) {
                //相对路径
                int rootFilePathSplit = rootFileName.indexOf(parentFilePath) + parentFilePath.length() + 1;
                relativePath = uploadFile.getParent().substring(rootFilePathSplit);
            }
            ClientUploadStatus clientUploadStatus = doUploadSingleFile(saveParentPath, relativePath, uploadFile, client);
            if (clientUploadStatus != ClientUploadStatus.UPLOAD_FINISH) {
                LOGGER.warn("file {} upload failed", fileAbsolutePath);
            } else {
                LOGGER.info("file {} upload success", fileAbsolutePath);
            }
        }
        remoteRpcNode.destroyConnection();
    }

}
