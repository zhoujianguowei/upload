package command;

import cons.DefaultConfigConstant;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.service.FileTransferClient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LaunchClient {
    // 初始化Log4j日志记录器
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchClient.class);

    public static void main(String[] args) {
        Options options = createOptions();
        CommandLine cmd;
        try {
            // 1. 解析命令行参数
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
            // 2. 处理帮助选项
            if (cmd.hasOption("h")) {
                printHelp(options);
                return;
            }
            // 3. 获取并校验参数
            Integer port = getPort(cmd);
            String uploadPath = getUploadPath(cmd);
            String ip = getIP(cmd); // 新增 IP 参数

            if (port == null || uploadPath == null || ip == null) {
                LOGGER.error("参数校验失败，请检查输入");
                printHelp(options);
                return;
            }
            // 4. 执行业务逻辑（示例）
            LOGGER.info("成功解析参数：端口=" + port + "，上传路径=" + uploadPath + "，IP=" + ip);
            FileTransferClient fileTransferClient = new FileTransferClient();
            fileTransferClient.uploadFile(null, uploadPath, ip, port, 5000, null);
            // 这里可以添加实际的业务处理逻辑
        } catch (ParseException e) {
            LOGGER.error("参数解析失败", e);
            printHelp(options);
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        // 1. 帮助选项 (-h, --help)
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("显示帮助信息")
                .build();
        // 2. 端口选项 (-p, --port)
        Option port = Option.builder("p")
                .longOpt("port")
                .desc("映射的端口（1-65535）")
                .hasArg()
                .type(Integer.class)
                .build();
        // 3. 上传路径选项 (-up, --upload)
        Option upload = Option.builder("up")
                .longOpt("upload")
                .desc("要上传的本地文件或目录路径")
                .hasArg()
                .required()
                .build();
        // 4. 新增 IP 选项 (-i, --ip)
        Option ip = Option.builder("i")
                .longOpt("ip")
                .desc("指定服务器 IP 地址（如：192.168.1.1）")
                .hasArg()
                .required()
                .build();
        options.addOption(help);
        options.addOption(port);
        options.addOption(upload);
        options.addOption(ip); // 添加到选项列表
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("myapp [选项]", options);
        System.exit(0);
    }

    private static Integer getPort(CommandLine cmd) {
        if (!cmd.hasOption("p")) {
            return DefaultConfigConstant.UPLOAD_SERVER_PORT;
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
            return null; // 修改：避免抛出异常，直接返回 null
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

    // 新增方法：获取并校验 IP 参数
    private static String getIP(CommandLine cmd) {
        String ip = cmd.getOptionValue("i");
        if (ip == null) {
            LOGGER.error("错误: 未指定 IP 地址");
            return null;
        }
        try {
            InetAddress.getByName(ip); // 验证 IP 格式
        } catch (UnknownHostException e) {
            LOGGER.error("错误: IP 地址格式不正确");
            return null;
        }
        return ip;
    }
}
