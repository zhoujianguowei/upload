package worker;

import common.ClientUploadStatus;
import common.FileHandlerHelper;
import cons.CommonConstant;
import handler.ClientUploadManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTransferWorker;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;
import rpc.thrift.file.transfer.FileUploadResponse;
import rpc.thrift.file.transfer.ResResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户端处理文件上传、下载服务类
 */
public class AbstractClientWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientWorker.class);
    private ClientUploadManager clientUploadManager;

    public AbstractClientWorker(ClientUploadManager clientUploadManager) {
        this.clientUploadManager = clientUploadManager;
    }

    protected FileUploadRequest constructFileUploadRequest(String saveParentPath, String relativePath, File file) {
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
            //#todo
        }
        return request;
    }

    /**
     * 上传单个文件或者文件夹
     *
     * @param saveParentPath
     * @param relativePath
     * @param rootFile
     * @param client
     * @return
     */
    public ClientUploadStatus doUploadSingleFile(String saveParentPath, String relativePath, File rootFile, FileTransferWorker.Client client) {
        FileUploadRequest fileUploadRequest = constructFileUploadRequest(saveParentPath, relativePath, rootFile);
        try {
            FileUploadResponse fileUploadResponse = client.uploadFile(fileUploadRequest, FileHandlerHelper.generateFileToken(rootFile.getName()));
            if (fileUploadResponse.getUploadStatusResult() != ResResult.FILE_END) {
                LOGGER.warn("failed to upload file||response={}", fileUploadResponse);
                return ClientUploadStatus.FAIL;
            }
        } catch (TException e) {
            LOGGER.error("thrift exception", e);
            return ClientUploadStatus.FAIL;
        }
        return ClientUploadStatus.UPLOAD_FINISH;
    }

    /**
     * 当前主机产生唯一标识
     *
     * @param remoteHost
     * @return
     */
    protected String generateUniqueHostIdentifier(String remoteHost) {
        return FileHandlerHelper.getDeviceUniqueIdentifier();
    }

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
