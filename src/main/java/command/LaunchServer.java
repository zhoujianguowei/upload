package command;

import config.ConfigDataHelper;
import cons.BusinessConstant;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.service.FileTransferServer;

public class LaunchServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchServer.class);

    public static void main(String[] args) {
        Options options = new Options();
        String portOptionKey = "port";
        Option portOption = Option.builder("p")
                .longOpt(portOptionKey)
                .desc("server port")
                .hasArg()
                .build();
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .desc("显示帮助信息")
                .hasArg(false)
                .build();
        options.addOption(portOption).addOption(helpOption);
        Integer port = BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT_VALUE;
        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar app [选项] [参数]", options);
                System.exit(0);
            }
            if (cmd.hasOption(portOptionKey)) {
                port = Integer.valueOf(cmd.getOptionValue(portOptionKey));
                LOGGER.info("specify port {}", port);
            }
            ConfigDataHelper.saveStoreConfigData(BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT, String.valueOf(port));
        } catch (Exception e) {
            LOGGER.error("parse command line args error", e);
            System.exit(1);
        }
        options.addOption(portOption);
        FileTransferServer fileTransferServer = FileTransferServer.getSingleTon();
        LOGGER.info("launch server start");
        fileTransferServer.asyncLaunchFileHandlerService(port);
    }
}
