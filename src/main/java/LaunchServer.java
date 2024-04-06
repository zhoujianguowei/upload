import config.ConfigDataHelper;
import cons.BusinessConstant;
import org.apache.commons.cli.*;
import org.apache.thrift.util.StorageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.service.FileTransferServer;

public class LaunchServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchServer.class);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(new Option("help", false, "server help message"));
        options.addOption(new Option("receive_limit_mb", false, "receive limit speed,default 2mb"));
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("LaunchServer", options);
        int limit = (int) StorageFormat.transformSize("2mb", "byte");
        try {
            CommandLine commandLine = commandLineParser.parse(options, args);
            if (commandLine.hasOption("receive_limit_mb")) {
                limit = Integer.parseInt("receive_limit_mb");
            }
            ConfigDataHelper.saveStoreConfigData(BusinessConstant.ConfigData.CLIENT_UPLOAD_LIMIT_SPEED_THRESHOLD, String.valueOf(StorageFormat.transformSize(limit + "mb", "byte")));
        } catch (Exception e) {
            LOGGER.error("parse exception", e);
            return;
        }
        FileTransferServer fileTransferServer = FileTransferServer.getSingleTon();
        fileTransferServer.asyncLaunchFileHandlerService();
        LOGGER.info("launch server success");

    }
}
