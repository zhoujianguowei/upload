package common;

import cons.BusinessConstant;
import cons.CommonConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;
import rpc.thrift.file.transfer.FileUploadRequest;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import static config.ConfigDataHelper.getStoreConfigData;
import static config.ConfigDataHelper.saveStoreConfigData;

/**
 * 辅助工具类
 */
public class FileHandlerHelper {
    /**
     * 大小写字母、数字
     */
    private static final char digitsAlpha[] = new char[62];
    /**
     * 文件上传产生的唯一identifier前缀
     */
    private static final String IDENTIFIER_PREFIX = "KJGD8787532lagsgd@!IUIUG@~JKKKSLD";
    /**
     * 文件上传需要的token的后缀
     */
    private static final String TOKEN_SUFFIX = "788532LKLKSG@KJKLGS71sd8u52352";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerHelper.class);
    private static String deviceIdentifier;
    /**
     * 兜底的文件设备标识
     */
    private static final String BOTTOM_DEVICE_IDENTIFIER = "bottomDeviceIdentifier";

    static {
        int index = 0;
        for (char ch = '0'; ch <= '9'; ch++) {
            digitsAlpha[index++] = ch;
        }
        for (char ch = 'a'; ch <= 'z'; ch++) {
            digitsAlpha[index++] = ch;
        }
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            digitsAlpha[index++] = ch;
        }
    }

    /**
     * 如果上传或者下载的是文件，文件片段的校验码。采用crc32作为校验
     *
     * @param contents
     * @return
     */
    public static String generateContentsCheckSum(byte[] contents, int length) {
        CRC32 crc32 = new CRC32();
        crc32.update(contents, 0, length);
        return Long.toHexString(crc32.getValue());
    }


    /**
     * MD5加密，如果加密失败，返回原始字符串
     *
     * @param plainText
     * @return
     */
    public static String doMd5Digest(String plainText) {
        StringBuilder ans = new StringBuilder();
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 使用指定的字节更新摘要
            md.update(plainText.getBytes());

            // 获得密文
            byte[] mdResult = md.digest();
            // 把密文转换成十六进制的字符串形式
            for (byte mdByte : mdResult) {
                ans.append(digitsAlpha[(128 + mdByte) % digitsAlpha.length]);
            }

        } catch (Exception e) {
            LOGGER.error("failed to do md5 digest", e);
            ans.append(plainText);
        }
        return ans.toString();
    }

    /**
     * 产生文件对应的token
     *
     * @param fileName
     * @return
     */
    public static String generateFileToken(String fileName) {
        return fileName + CommonConstant.UNDERLINE + TOKEN_SUFFIX;
    }

    public static boolean validateFileToken(String token, String fileName) {
        return token.equals(generateFileToken(fileName));
    }

    /**
     * 获取设备唯一标识
     *
     * @return
     */
    public static String getDeviceUniqueIdentifier() {
        if (StringUtils.isBlank(deviceIdentifier)) {
            synchronized (FileHandlerHelper.class) {
                if (StringUtils.isBlank(deviceIdentifier)) {
                    deviceIdentifier = getStoreConfigData(BusinessConstant.ConfigData.HOST_IDENTIFIER_KEY);
                    if (StringUtils.isBlank(deviceIdentifier)) {
                        deviceIdentifier = UUID.randomUUID().toString();
                        try {
                            saveStoreConfigData(BusinessConstant.ConfigData.HOST_IDENTIFIER_KEY, deviceIdentifier);
                        } catch (IOException e) {
                            LOGGER.error("failed to store config data,use bottom strategy||configKey=" + BusinessConstant.ConfigData.HOST_IDENTIFIER_KEY, e);
                            deviceIdentifier = BOTTOM_DEVICE_IDENTIFIER;
                        }
                    }
                }
            }
        }
        return deviceIdentifier;
    }

    /**
     * 产生上传完成所在目录的全路径名，使用linux文件分隔符
     *
     * @param saveParentPath
     * @param relativePath
     * @param fileName
     * @return
     */
    public static String generateWholePath(String saveParentPath, String relativePath, String fileName) {
        StringBuilder concatPath = new StringBuilder();
        if (StringUtils.isNotBlank(saveParentPath)) {
            concatPath.append(saveParentPath).append(CommonConstant.LINUX_SHELL_SEPARATOR);
        }
        if (StringUtils.isNotBlank(relativePath)) {
            concatPath.append(relativePath).append(CommonConstant.LINUX_SHELL_SEPARATOR);
        }
        if (StringUtils.isNotBlank(fileName)) {
            concatPath.append(fileName);
        }
        return concatPath.toString().replaceAll(Pattern.quote(CommonConstant.WINDOWS_FILE_SEPARATOR), CommonConstant.LINUX_SHELL_SEPARATOR).replaceAll("(" + CommonConstant.LINUX_SHELL_SEPARATOR + ")\\1+", "$1");
    }

    /**
     * 获取当前文件的唯一标识
     *
     * @param fileIdentifier
     * @return
     */
    public static String generateUniqueIdentifier(String fileIdentifier) {
        return generateUniqueIdentifier(fileIdentifier, getDeviceUniqueIdentifier());
    }

    /**
     * 校验文件上传文件的{@link FileUploadRequest#identifier}是否正确
     *
     * @param request
     * @return
     */
    public static boolean validateFileIdentifier(FileUploadRequest request) {
        String identifier = request.getIdentifier();
        if (!identifier.startsWith(IDENTIFIER_PREFIX)) {
            return false;
        }
        String fileWholePath = generateWholePath(request.getSaveParentFolder(), request.getRelativePath(), request.getFileName());
        if (request.getFileType() == FileTypeEnum.DIR_TYPE) {
            return identifier.startsWith(IDENTIFIER_PREFIX + CommonConstant.UNDERLINE + doMd5Digest(fileWholePath));
        } else {
            return identifier.startsWith(IDENTIFIER_PREFIX + CommonConstant.UNDERLINE + doMd5Digest(fileWholePath + CommonConstant.UNDERLINE + request.getTotalFileLength()));
        }
    }

    /**
     * 产生上传文件的唯一token
     *
     * @param absoluteFilePath
     * @param hostIdentifier
     * @return
     */
    public static String generateUniqueIdentifier(String absoluteFilePath, String hostIdentifier) {
        return IDENTIFIER_PREFIX + CommonConstant.UNDERLINE + doMd5Digest(absoluteFilePath) + CommonConstant.UNDERLINE + doMd5Digest(hostIdentifier);
    }

    /**
     * 将资源平均划分到splitNumber份
     *
     * @param source
     * @param splitNumber
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> source, int splitNumber) {

        if (CollectionUtils.isEmpty(source) || splitNumber <= 0) {
            return new ArrayList<>();
        }
        int totalSize = source.size();
        //每个子列表分到的资源个数
        int perSubListSize = totalSize / splitNumber;
        if (totalSize % splitNumber != 0) {
            perSubListSize++;
        }
        List<List<T>> result = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < splitNumber; i++) {
            ArrayList<T> subList = new ArrayList<>();
            for (int j = index; j < index + perSubListSize && j < totalSize; j++) {
                subList.add(source.get(j));
            }
            index += perSubListSize;
            result.add(subList);
        }
        return result;
    }

}
