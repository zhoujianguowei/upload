package worker;

import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTransferWorker;

/**
 * 远程rpc连接
 */
public class RemoteRpcNode {
    private String host;
    private int port;
    private TSocket tSocket;
    private TTransport transport;
    private FileTransferWorker.Client client;
    private int timeout;
    /**
     * 当前连接是否关闭
     */
    private volatile boolean connectionAlive = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteRpcNode.class);

    public RemoteRpcNode(String host, int port) {
        this(host, port, 5000);
    }

    public RemoteRpcNode(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * 如果远程连接不存在，那么简历连接。
     *
     * @return 表示远程连接是否建立成功
     */
    protected boolean createRemoteConnectionIfNotExists() {
        if (connectionAlive) {
            return true;
        }
        synchronized (this) {
            try {
                tSocket = new TSocket(new TConfiguration(), host, port, timeout);
                transport = new TFramedTransport(tSocket);
                TProtocol protocol = new TCompactProtocol(transport);
                client = new FileTransferWorker.Client(protocol);
                transport.open();
                connectionAlive = true;
            } catch (Exception e) {
                LOGGER.error("failed to create remote connection", e);
            }
        }
        return connectionAlive;
    }

    protected boolean createRemoteConnectionWithMaxTryCount(int maxTryCount) {
        return createRemoteConnectionWithMaxTryCount(maxTryCount, 100);
    }

    /**
     * 建立rpc连接，有最大重试次数
     *
     * @param maxTryCount
     * @return
     */
    protected boolean createRemoteConnectionWithMaxTryCount(int maxTryCount, int interval) {
        if (maxTryCount <= 0) {
            throw new IllegalArgumentException("maxTryCount can't be smaller than zero");
        }
        for (int i = 0; i < maxTryCount; i++) {
            if (createRemoteConnectionIfNotExists()) {
                return true;
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                LOGGER.warn("interrupted exception when create rpc connection", e);
            }
        }
        return false;
    }

    public FileTransferWorker.Client getRemoteClient() {
        return client;
    }

    /**
     * 关闭连接
     */
    protected synchronized void releaseConnectionIfNecessary() {
        if (!connectionAlive) {
            return;
        }
        try {
            connectionAlive = false;
            if (transport != null && transport.isOpen()) {
                transport.close();
                tSocket = null;
            }
            if (tSocket != null && tSocket.isOpen()) {
                tSocket.close();
                tSocket = null;
            }
        } catch (Exception e) {
            LOGGER.error("exception when to release connection", e);
        }
    }

}