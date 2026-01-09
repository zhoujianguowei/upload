package config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigOperation {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.upload_manager";
    private static final String CLIENT_JSON = CONFIG_DIR + "/client.json";
    private static final String SERVER_JSON = CONFIG_DIR + "/server.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigOperation.class);

    private static void ensureConfigDir() throws IOException {
        Path path = Paths.get(CONFIG_DIR);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public static ClientConfig getClientConfig() {
        File file = new File(CLIENT_JSON);
        if (!file.exists() || !file.isFile()) {
            return new ClientConfig();
        }
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            return JSON.parseObject(content, ClientConfig.class);
        } catch (IOException e) {
            LOGGER.error("failed to read client config||path={}", CLIENT_JSON, e);
            return new ClientConfig();
        } catch (JSONException e) {
            LOGGER.error("failed to parse client config||path={}", CLIENT_JSON, e);
            return new ClientConfig();
        }
    }

    public static ServerConfig getServerConfig() {
        File file = new File(SERVER_JSON);
        if (!file.exists() || !file.isFile()) {
            return new ServerConfig();
        }
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            return JSON.parseObject(content, ServerConfig.class);
        } catch (IOException e) {
            LOGGER.error("failed to read server config||path={}", SERVER_JSON, e);
            return new ServerConfig();
        } catch (JSONException e) {
            LOGGER.error("failed to parse server config||path={}", SERVER_JSON, e);
            return new ServerConfig();
        }
    }

    public static void saveClientConfig(ClientConfig config) {
        try {
            ensureConfigDir();
            String content = JSON.toJSONString(config, true);
            Files.write(new File(CLIENT_JSON).toPath(), content.getBytes());
        } catch (IOException e) {
            LOGGER.error("failed to save client config||path={}", CLIENT_JSON, e);
        }
    }

    public static void saveServerConfig(ServerConfig config) {
        try {
            ensureConfigDir();
            String content = JSON.toJSONString(config, true);
            Files.write(new File(SERVER_JSON).toPath(), content.getBytes());
        } catch (IOException e) {
            LOGGER.error("failed to save server config||path={}", SERVER_JSON, e);
        }
    }
}
