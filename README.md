# 局域网文件上传

底层是使用thrift实现，能够实现局域网内文件（包括文件夹）的批量上传。

## 基本功能

1. 文件文件夹上传功能，支持原始文件夹目录结构，并支持断点续传功能，支持多文件批量上传
2. 文件数据校验：上传文件文件名以及文件内容校验
3. 文件加密：支持上传文件加密传输（目前功能还未开发完成）
4. 多种配置方式：批量上传文件数量、一次上传字节块大小、重试策略、最大重试次数等配置
5. 客户端支持上传进度回调

## 基本用法

整个项目采用maven方式，通过git下载完成之后，直接通过ide编译即可。

### 首先启动文件上传服务端

启动服务端，默认端口是10033。

~~~java
 FileTransferServer fileTransferServer=FileTransferServer.getSingleTon();
        fileTransferServer.asyncLaunchFileHandlerService();
~~~

### 启动客户端

客户端是**FileTransferClient**类，客户端支持指定上传文件保存到指定的目录，支持按照文件名方式进行过滤上传。支持上传进度回调。（目前默认实现类是**
TraceUploadProgressSpeedProgressCallback**）

~~~
String serverHost = "192.168.0.110";
FileTransferClient fileTransferClient=new FiletransferClient();
fileTransferClient.uploadFile("E:/test/aabbbcc", "E:/课件/面试", serverHost);
~~~

**FileTransferClient**

~~~java
public class FileTransferClient {


    private static final int CONNECTION_TIME_OUT = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferClient.class);
    private UploadFileProgressCallback uploadFileProgressCallback;

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
    public void uploadFile(String saveParentPath, String uploadFileOrDirPath, String host,
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
        clientWorker.clientUploadFile(saveParentPath, file, host, port, connectionTimeOut);
    }

    public UploadFileProgressCallback getUploadFileProgressCallback() {
        return uploadFileProgressCallback;
    }


    public void setUploadFileProgressCallback(UploadFileProgressCallback uploadFileProgressCallback) {
        this.uploadFileProgressCallback = uploadFileProgressCallback;
    }
}
~~~

## 运行截图

客户端的上传进度截图

![client_upload_progress_2021111101](https://github.com/zhoujianguowei/upload/raw/master/img/client_upload_progress_2021111101.png)

## 版本变更

版本以为分支名称作为标准。

- v1.0
    - 文件、文件夹批量上传功能开发完成，支持文件数据内容校验、支持断点上传
    - 支持文件上传进度回调
- v1.1
    - 支持自定义异常重试
    - 增加多种自定义配置



