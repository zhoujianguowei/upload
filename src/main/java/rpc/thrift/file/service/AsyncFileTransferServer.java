package rpc.thrift.file.service;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class AsyncFileTransferServer {
    public static int ASYNC_HELLO_SERVER_PORT = 10033;
    public static int BACK_LOG = 100;
    private static int SELECTOR_THREADS = 16;
    private static int ACCEPT_QUEUE_PER_THREAD = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFileTransferServer.class);


    public static void main(String[] args) throws TTransportException {
        TProcessor processor = new FileTransferWorker.AsyncProcessor<>(FileTransferServiceImpl.getInstance());
        TNonblockingServerSocket transport = new TNonblockingServerSocket(
                new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs()
                        .port(ASYNC_HELLO_SERVER_PORT)
                        .backlog(BACK_LOG)
        );
        LOGGER.info("async server start");
        TThreadedSelectorServer.Args serverArgs = new TThreadedSelectorServer.Args(transport);
        serverArgs.processor(processor)
                .protocolFactory(new TCompactProtocol.Factory())
                .transportFactory(new TFramedTransport.Factory())
                .selectorThreads(SELECTOR_THREADS)
                .acceptQueueSizePerThread(ACCEPT_QUEUE_PER_THREAD)
                .executorService(getExecutorService());
        serverArgs.maxReadBufferBytes = 256 * 1024 * 1024;

        TServer server = new TThreadedSelectorServer(serverArgs);
        server.serve();
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
