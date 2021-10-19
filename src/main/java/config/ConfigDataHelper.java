package config;

import cons.BusinessConstant;
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
            saveStoreConfigData(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH, System.getProperty(BusinessConstant.ConfigData.PER_UPLOAD_BYTES_LENGTH, String.valueOf(102400)));
            saveStoreConfigData(BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES, System.getProperty(BusinessConstant.ConfigData.FILE_CONTENT_BROKER_MAX_RETRY_TIMES, String.valueOf(3)));
            saveStoreConfigData(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM, System.getProperty(BusinessConstant.ConfigData.MAX_PARALLEL_UPDATE_FILE_NUM, String.valueOf(5)));
        } catch (IOException e) {
            throw new RuntimeException("failed to load config");
        }
    }

    public static synchronized void saveStoreConfigData(String key, String configData) throws IOException {
        File storeFile = new File(STORE_CONFIG_DATA_PATH);
        if (!storeFile.exists() || !storeFile.isFile()) {
            if (!storeFile.createNewFile()) {
                throw new RuntimeException("failed to create config file||path=" + storeFile.getAbsolutePath());
            }
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(storeFile));
        properties.setProperty(key, configData);
        properties.store(new FileOutputStream(storeFile), "config data");
    }

    public static String getStoreConfigData(String configKey) {
        File storeFile = new File(STORE_CONFIG_DATA_PATH);
        if (!storeFile.exists() || !storeFile.isFile()) {
            return null;
        }
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(storeFile));
            return properties.getProperty(configKey);
        } catch (IOException e) {
            LOGGER.error("file io exception||filePath=" + storeFile.getAbsolutePath(), e);
        }
        return null;
    }
}
