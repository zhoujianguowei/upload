package rpc.thrift.file.service;

import config.ConfigDataHelper;
import cons.BusinessConstant;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.AbstractNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.apache.thrift.util.ThriftPackFieldPub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTransferWorker;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class FileTransferServer {
    public static int FILE_HANDLER_SERVER_PORT = Integer.parseInt(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT));
    public static int BACK_LOG = 100;
    private static int SELECTOR_THREADS = 16;
    private static int ACCEPT_QUEUE_PER_THREAD = 1000;
    private static long MAX_READ_BUFFER_BYTES = 256 * 1024 * 1024;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferServer.class);
    private static final FileTransferServer fileTransferService = new FileTransferServer();

    private FileTransferServer() {

    }

    public static FileTransferServer getSingleTon() {
        return fileTransferService;
    }

    public void asyncLaunchFileHandlerService(int port) {
        TProcessor processor = new FileTransferWorker.AsyncProcessor<>(FileTransferServiceImpl.getInstance());
        TNonblockingServerSocket transport = null;
        try {
            transport = new TNonblockingServerSocket(
                    new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs()
                            .port(port)
                            .backlog(BACK_LOG)
            );
        } catch (TTransportException e) {
            LOGGER.error("start handler server exception", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("async server start");
        TThreadedSelectorServer.Args serverArgs = new TThreadedSelectorServer.Args(transport);
        serverArgs.processor(processor)
                .protocolFactory(new TCompactProtocol.Factory())
                .transportFactory(new TFramedTransport.Factory())
                .selectorThreads(SELECTOR_THREADS)
                .acceptQueueSizePerThread(ACCEPT_QUEUE_PER_THREAD)
                .executorService(getExecutorService());
        serverArgs.maxReadBufferBytes = MAX_READ_BUFFER_BYTES;
        TServer server = new TThreadedSelectorServer(serverArgs);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            server.serve();
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("interrupted exception||msg={}", e.getMessage());
        }
    }

    /**
     * 启动文件上传服务端
     */
    public void asyncLaunchFileHandlerService() {
        asyncLaunchFileHandlerService(FILE_HANDLER_SERVER_PORT);
    }

    public static ExecutorService getExecutorService() {
        return new ThreadPoolExecutor(10, 100, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>(20000),
                new DefaultThreadFactory("async_thrift_executor_thread"), (r, t) -> {
            LOGGER.warn("out of queue,drop job", r);
        }) {
            @Override
            public void execute(Runnable command) {
                AbstractNonblockingServer.FrameBuffer fb = null;
                try {
                    fb = (AbstractNonblockingServer.FrameBuffer) ThriftPackFieldPub.FRAME_BUFFER_FIELD.get(command);
                    TNonblockingSocket socket = (TNonblockingSocket) ThriftPackFieldPub.TRANS_FIELD.get(fb);
                    InetSocketAddress address = (InetSocketAddress) socket.getSocketChannel().getRemoteAddress();
                    InetAddress inetAddress = address.getAddress();
                    LOGGER.debug("remoteIp={}||remotePort={}||remoteHostName={}", inetAddress.getHostAddress(), address.getPort(), inetAddress.getHostName());
                    super.execute(command);
                } catch (Exception e) {
                    LOGGER.error("failed to access remote address", e);
                }
            }
        };
    }
}
