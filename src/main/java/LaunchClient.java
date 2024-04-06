import cn.hutool.core.thread.ThreadUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.service.FileTransferClient;
import rpc.thrift.file.service.FileTransferServer;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.*;

public class LaunchClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchClient.class);
    private static BlockingDeque<File> clientReadyToSendFileFolderQueue = new LinkedBlockingDeque<>();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    private static void waitForInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            LOGGER.info("wait to input send path");
            String line = scanner.nextLine();
            if (!new File(line).exists()) {
                LOGGER.warn("path={} non exists", line);
            } else {
                clientReadyToSendFileFolderQueue.offer(new File(line));
            }
            continue;
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(new Option("help", false, "client help message"));
        options.addOption(new Option("remote_port", false, "remove server port"));
        options.addOption(new Option("remote_ip", true, "remote server ip"));
        options.addOption(new Option("timeout", false, "remote connect timeout seconds,default 5s"));
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LaunchClient", options);
        String remoteServerIp;
        int port = FileTransferServer.FILE_HANDLER_SERVER_PORT;
        int timeout = FileTransferClient.CONNECTION_TIME_OUT;
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("remote_port")) {
                port = Integer.parseInt(commandLine.getOptionValue("remote_port"));
            }
            if (commandLine.hasOption("timeout")) {
                timeout = Integer.parseInt(commandLine.getOptionValue("timeout"));
            }
            if (!commandLine.hasOption("remote_ip")) {
                LOGGER.error("you have to specify remote server ip");
                return;
            }
            remoteServerIp = commandLine.getOptionValue("remote_ip");
        } catch (ParseException e) {
            LOGGER.error("parser exception", e);
            LOGGER.error("start failed");
            return;
        }
        FileTransferClient fileTransferClient = new FileTransferClient();
        LOGGER.info("detect connection||ip={}||port={}||timeout={}", remoteServerIp, port, timeout);
        if (!fileTransferClient.createConnection(remoteServerIp, port, timeout)) {
            LOGGER.error("failed to connect file server||ip={}||port={}||timeout={}", remoteServerIp, port, timeout);
            System.exit(-1);
        }
        LOGGER.info("connect to {} success", remoteServerIp);
        executor.execute(LaunchClient::waitForInput);
        while (true) {
            File toSendFile = clientReadyToSendFileFolderQueue.poll();
            if (toSendFile == null) {
                LOGGER.info("client no send file dir");
                ThreadUtil.sleep(10, TimeUnit.SECONDS);
                continue;
            }
            LOGGER.info("start to upload file={}||remainDirSize={}", toSendFile.getAbsolutePath(), clientReadyToSendFileFolderQueue.size());
            fileTransferClient.uploadFile(toSendFile.getAbsolutePath(), remoteServerIp);
            LOGGER.info("finish upload file={}", toSendFile.getAbsolutePath());
        }
    }


}
