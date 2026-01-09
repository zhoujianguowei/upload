package config;

import cons.BusinessConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConfigDataHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDataHelper.class);

    static {
        loadConfigData();
    }

    public static void loadConfigData() {
        ClientConfig clientConfig = ConfigOperation.getClientConfig();
        boolean clientModified = false;
        if (clientConfig.getPerUploadBytesLength() == 0) {
            clientConfig.setPerUploadBytesLength(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH_VALUE);
            clientModified = true;
        }
        if (clientConfig.getFileContentBrokerMaxRetryTimes() == 0) {
            clientConfig.setFileContentBrokerMaxRetryTimes(BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES_VALUE);
            clientModified = true;
        }
        if (clientConfig.getMaxParallelUploadFileNum() == 0) {
            clientConfig.setMaxParallelUploadFileNum(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM_VALUE);
            clientModified = true;
        }
        if (!clientConfig.isTraceClientUploadSpeedSwitch()) {
            clientConfig.setTraceClientUploadSpeedSwitch(BusinessConstant.ConfigData.TRACE_CLIENT_UPLOAD_SPEED_SWITCH_VALUE);
            clientModified = true;
        }
        if (clientConfig.getFileUploadMaxRetryCount() == 0) {
            clientConfig.setFileUploadMaxRetryCount(BusinessConstant.ConfigData.FILE_UPLOAD_MAX_RETRY_COUNT_VALUE);
            clientModified = true;
        }
        if (clientConfig.getClientCreateConnectionMaxTryTimes() == 0) {
            clientConfig.setClientCreateConnectionMaxTryTimes(BusinessConstant.ConfigData.CLIENT_CREATE_CONNECTION_MAX_TRY_TIMES_VALUE);
            clientModified = true;
        }
        if (StringUtils.isBlank(clientConfig.getFileUploadSaveParentPath())) {
            clientConfig.setFileUploadSaveParentPath(BusinessConstant.ConfigData.FILE_UPLOAD_SAVE_PARENT_PATH_VALUE);
            clientModified = true;
        }
        if (clientModified) {
            ConfigOperation.saveClientConfig(clientConfig);
        }

        ServerConfig serverConfig = ConfigOperation.getServerConfig();
        boolean serverModified = false;
        if (serverConfig.getTransferFileServerPort() == 0) {
            serverConfig.setTransferFileServerPort(BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT_VALUE);
            serverModified = true;
        }
        if (serverModified) {
            ConfigOperation.saveServerConfig(serverConfig);
        }
    }

    public static synchronized void saveStoreConfigData(String key, String configData) throws IOException {
        if (BusinessConstant.ConfigData.HOST_IDENTIFIER_KEY.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setHostIdentifier(configData);
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setPerUploadBytesLength(Integer.parseInt(configData));
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setFileContentBrokerMaxRetryTimes(Integer.parseInt(configData));
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setMaxParallelUploadFileNum(Integer.parseInt(configData));
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT.equals(key)) {
            ServerConfig serverConfig = ConfigOperation.getServerConfig();
            serverConfig.setTransferFileServerPort(Integer.parseInt(configData));
            ConfigOperation.saveServerConfig(serverConfig);
        } else if (BusinessConstant.ConfigData.TRACE_CLIENT_UPLOAD_SPEED_SWITCH.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setTraceClientUploadSpeedSwitch(Boolean.parseBoolean(configData));
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.FILE_UPLOAD_MAX_RETRY_COUNT.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setFileUploadMaxRetryCount(Integer.parseInt(configData));
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.CLIENT_CREATE_CONNECTION_MAX_TRY_TIMES.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setClientCreateConnectionMaxTryTimes(Integer.parseInt(configData));
            ConfigOperation.saveClientConfig(clientConfig);
        } else if (BusinessConstant.ConfigData.FILE_UPLOAD_SAVE_PARENT_PATH.equals(key)) {
            ClientConfig clientConfig = ConfigOperation.getClientConfig();
            clientConfig.setFileUploadSaveParentPath(configData);
            ConfigOperation.saveClientConfig(clientConfig);
        }
    }

    public static String getStoreConfigData(String configKey) {
        if (BusinessConstant.ConfigData.HOST_IDENTIFIER_KEY.equals(configKey)) {
            return ConfigOperation.getClientConfig().getHostIdentifier();
        } else if (BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH.equals(configKey)) {
            return String.valueOf(ConfigOperation.getClientConfig().getPerUploadBytesLength());
        } else if (BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES.equals(configKey)) {
            return String.valueOf(ConfigOperation.getClientConfig().getFileContentBrokerMaxRetryTimes());
        } else if (BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM.equals(configKey)) {
            return String.valueOf(ConfigOperation.getClientConfig().getMaxParallelUploadFileNum());
        } else if (BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT.equals(configKey)) {
            return String.valueOf(ConfigOperation.getServerConfig().getTransferFileServerPort());
        } else if (BusinessConstant.ConfigData.TRACE_CLIENT_UPLOAD_SPEED_SWITCH.equals(configKey)) {
            return String.valueOf(ConfigOperation.getClientConfig().isTraceClientUploadSpeedSwitch());
        } else if (BusinessConstant.ConfigData.FILE_UPLOAD_MAX_RETRY_COUNT.equals(configKey)) {
            return String.valueOf(ConfigOperation.getClientConfig().getFileUploadMaxRetryCount());
        } else if (BusinessConstant.ConfigData.CLIENT_CREATE_CONNECTION_MAX_TRY_TIMES.equals(configKey)) {
            return String.valueOf(ConfigOperation.getClientConfig().getClientCreateConnectionMaxTryTimes());
        } else if (BusinessConstant.ConfigData.FILE_UPLOAD_SAVE_PARENT_PATH.equals(configKey)) {
            return ConfigOperation.getClientConfig().getFileUploadSaveParentPath();
        }
        return null;
    }

    public static int getIntConfig(String key, int defaultValue) {
        String value = getStoreConfigData(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid integer config value for key: " + key + ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public static boolean getBooleanConfig(String key, boolean defaultValue) {
        String value = getStoreConfigData(key);
        if (value != null) {
            try {
                return Boolean.parseBoolean(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid boolean config value for key: " + key + ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public static String getFileUploadSaveParentPath() {
        String value = ConfigOperation.getClientConfig().getFileUploadSaveParentPath();
        return StringUtils.isNotBlank(value) ? value : BusinessConstant.ConfigData.FILE_UPLOAD_SAVE_PARENT_PATH_VALUE;
    }

    public static void setFileUploadSaveParentPath(String path) {
        ClientConfig clientConfig = ConfigOperation.getClientConfig();
        clientConfig.setFileUploadSaveParentPath(path);
        ConfigOperation.saveClientConfig(clientConfig);
    }
}
