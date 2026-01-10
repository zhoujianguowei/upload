package command;

import cons.BusinessConstant;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.service.FileTransferClient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LaunchClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchClient.class);

    public static void main(String[] args) {
        Options options = createOptions();
        CommandLine cmd;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                printHelp(options);
                return;
            }
            Integer port = getPort(cmd);
            String uploadPath = getUploadPath(cmd);
            String ip = getIP(cmd);

            if (port == null || uploadPath == null || ip == null) {
                LOGGER.error("参数校验失败，请检查输入");
                printHelp(options);
                return;
            }
            LOGGER.info("成功解析参数：端口=" + port + "，上传路径=" + uploadPath + "，IP=" + ip);
            FileTransferClient fileTransferClient = new FileTransferClient();
            fileTransferClient.uploadFile(uploadPath, ip, port, 5000, null);
        } catch (ParseException e) {
            LOGGER.error("参数解析失败", e);
            printHelp(options);
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("显示帮助信息")
                .build();
        Option port = Option.builder("p")
                .longOpt("port")
                .desc("映射的端口（1-65535）")
                .hasArg()
                .type(Integer.class)
                .build();
        Option upload = Option.builder("up")
                .longOpt("upload")
                .desc("要上传的本地文件或目录路径")
                .hasArg()
                .required()
                .build();
        Option ip = Option.builder("i")
                .longOpt("ip")
                .desc("指定服务器 IP 地址（如：192.168.1.1）")
                .hasArg()
                .required()
                .build();
        options.addOption(help);
        options.addOption(port);
        options.addOption(upload);
        options.addOption(ip);
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("myapp [选项]", options);
        System.exit(0);
    }

    private static Integer getPort(CommandLine cmd) {
        if (!cmd.hasOption("p")) {
            return BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT_VALUE;
        }
        try {
            Integer port = Integer.valueOf(cmd.getOptionValue("p"));
            if (port < 1 || port > 65535) {
                LOGGER.error("错误: 端口必须在 1~65535 之间");
                return null;
            }
            return port;
        } catch (NumberFormatException e) {
            LOGGER.error("错误: 端口必须是数字");
            return null;
        }
    }

    private static String getUploadPath(CommandLine cmd) {
        String path = cmd.getOptionValue("up");
        if (path == null) {
            LOGGER.error("错误: 未指定上传路径");
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            LOGGER.error("错误: 路径不存在: " + path);
            return null;
        }
        if (!file.canRead()) {
            LOGGER.error("错误: 无法读取路径: " + path);
            return null;
        }
        return path;
    }

    private static String getIP(CommandLine cmd) {
        String ip = cmd.getOptionValue("i");
        if (ip == null) {
            LOGGER.error("错误: 未指定 IP 地址");
            return null;
        }
        try {
            InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            LOGGER.error("错误: IP 地址格式不正确");
            return null;
        }
        return ip;
    }
}
