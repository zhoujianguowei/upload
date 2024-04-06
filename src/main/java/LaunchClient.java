import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.service.FileTransferClient;
import rpc.thrift.file.service.FileTransferServer;

import java.io.File;

public class LaunchClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchClient.class);
    private static final String PATH_OPT = "path";
    private static final String SUFFIX_OPT = "suffix";


    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(new Option("help", false, "client help message"));
        options.addOption(new Option("remote_port", false, "remove server port"));
        options.addOption(new Option("remote_ip", true, "remote server ip"));
        options.addOption(new Option(PATH_OPT, true, "upload file path"));
        options.addOption(new Option(SUFFIX_OPT, false, "file name suffix,multi split by comma"));
        options.addOption(new Option("timeout", false, "remote connect timeout seconds,default 5s"));
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LaunchClient", options);
        String remoteServerIp;
        int port = FileTransferServer.FILE_HANDLER_SERVER_PORT;
        int timeout = FileTransferClient.CONNECTION_TIME_OUT;
        String uploadPath;
        String[] nameFilters = null;
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
            if (!commandLine.hasOption(PATH_OPT)) {
                LOGGER.error("you have to specify {} value", PATH_OPT);
                return;
            }
            if (commandLine.hasOption(SUFFIX_OPT)) {
                nameFilters = commandLine.getOptionValue(SUFFIX_OPT).split(",");
            }
            remoteServerIp = commandLine.getOptionValue("remote_ip");
            uploadPath = commandLine.getOptionValue(PATH_OPT);
            if (!new File(uploadPath).exists()) {
                LOGGER.warn("path {} non exits", uploadPath);
                return;
            }
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
        if (ArrayUtils.isNotEmpty(nameFilters)) {
            fileTransferClient.uploadFile(uploadPath, remoteServerIp, nameFilters);
        } else {
            fileTransferClient.uploadFile(uploadPath, remoteServerIp);
        }
        LOGGER.info("finish upload file={}", fileTransferClient);
        fileTransferClient.shutdown();
    }


}
