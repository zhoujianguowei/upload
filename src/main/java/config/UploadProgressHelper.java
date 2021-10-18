package config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worker.CachedUploadFileStructure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传进度同步管理、数据持久化管理
 */
public class UploadProgressHelper {
    private static final String PROGRESS_FILE_PATH = System.getProperty("upload.progress.data", "upload.progress.data");
    private static final Logger LOGGER = LoggerFactory.getLogger(PROGRESS_FILE_PATH);
    private static File progressFile = new File(PROGRESS_FILE_PATH);

    static {
        if (!progressFile.exists() || !progressFile.isFile()) {
            try {
                if (!progressFile.createNewFile()) {
                    LOGGER.error("failed to create upload progress data file||configFilePath={}", progressFile.getAbsolutePath());
                    throw new RuntimeException("failed to create upload progress file");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("create upload progress data exception||filePath=" + progressFile.getAbsolutePath(), e);
            }
        }
    }

    public static List<CachedUploadFileStructure> loadUploadProgressData() {
        List<CachedUploadFileStructure> progressCachedDataList = new ArrayList<>();
        try {
            String uploadProgressData = FileUtils.readFileToString(progressFile, "UTF-8");
            if (StringUtils.isNotBlank(uploadProgressData)) {
                progressCachedDataList = JSON.parseArray(uploadProgressData, CachedUploadFileStructure.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("load config data error", e);
            throw new RuntimeException("load config data error");
        }
        return progressCachedDataList;
    }

    /**
     * 持久化文件上传进度
     *
     * @param progressCachedDataList
     */
    public static void persistUploadProgressData(List<CachedUploadFileStructure> progressCachedDataList) {
        if (CollectionUtils.isEmpty(progressCachedDataList)) {
            return;
        }
        String serialText = JSON.toJSONString(progressCachedDataList);
        try {
            FileUtils.write(progressFile, serialText, "UTF-8");
        } catch (IOException e) {
            LOGGER.warn("store upload progress exception ", e);
        }
    }
}
