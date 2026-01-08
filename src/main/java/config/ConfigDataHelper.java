package config;

import cons.BusinessConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 持久化保存的内容
 */
public class ConfigDataHelper {
    /**
     * 配置文件路径
     */
    private static final String STORE_CONFIG_DATA_PATH = System.getProperty("store.properties", "store.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDataHelper.class);

    static {
        loadConfigData();
    }

    public static void loadConfigData() {
        try {
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH, 
                System.getProperty(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH, String.valueOf(102400)));
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES, 
                System.getProperty(BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES, String.valueOf(3)));
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM, 
                System.getProperty(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM, String.valueOf(5)));
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT, 
                System.getProperty(BusinessConstant.ConfigData.TRANSFER_FILE_SERVER_PORT, String.valueOf(10033)));
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.TRACE_CLIENT_UPLOAD_SPEED_SWITCH, 
                System.getProperty(BusinessConstant.ConfigData.TRACE_CLIENT_UPLOAD_SPEED_SWITCH, String.valueOf(Boolean.TRUE)));
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.FILE_UPLOAD_MAX_RETRY_COUNT, 
                System.getProperty(BusinessConstant.ConfigData.FILE_UPLOAD_MAX_RETRY_COUNT, String.valueOf(5)));
            saveStoreConfigDataIfConfigNotExists(BusinessConstant.ConfigData.CLIENT_CREATE_CONNECTION_MAX_TRY_TIMES, 
                System.getProperty(BusinessConstant.ConfigData.CLIENT_CREATE_CONNECTION_MAX_TRY_TIMES, String.valueOf(5)));
        } catch (IOException e) {
            throw new RuntimeException("failed to load config", e);
        }
    }

    /**
     * 如果对应的配置不存在的话，写入配置；否则不做操作
     *
     * @param key
     * @param configData
     * @throws IOException
     */
    public static synchronized void saveStoreConfigDataIfConfigNotExists(String key, String configData) throws IOException {
        if (StringUtils.isNotBlank(getStoreConfigData(key))) {
            return;
        }
        saveStoreConfigData(key, configData);
    }

    public static synchronized void saveStoreConfigData(String key, String configData) throws IOException {
        File storeFile = new File(STORE_CONFIG_DATA_PATH);
        if (!storeFile.exists() || !storeFile.isFile()) {
            if (!storeFile.createNewFile()) {
                throw new RuntimeException("failed to create config file||path=" + storeFile.getAbsolutePath());
            }
        }
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(storeFile)) {
            properties.load(fis);
        }
        properties.setProperty(key, configData);
        try (FileOutputStream fos = new FileOutputStream(storeFile)) {
            properties.store(fos, "config data");
        }
    }

    public static String getStoreConfigData(String configKey) {
        File storeFile = new File(STORE_CONFIG_DATA_PATH);
        if (!storeFile.exists() || !storeFile.isFile()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(storeFile)) {
            Properties properties = new Properties();
            properties.load(fis);
            return properties.getProperty(configKey);
        } catch (IOException e) {
            LOGGER.error("file io exception||filePath=" + storeFile.getAbsolutePath(), e);
        }
        return null;
    }
    
    /**
     * 获取整型配置值
     */
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
    
    /**
     * 获取布尔型配置值
     */
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
}
