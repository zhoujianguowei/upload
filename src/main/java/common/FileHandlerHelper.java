package common;

import java.util.zip.CRC32;

/**
 * 辅助工具类
 */
public class FileHandlerHelper {
    /**
     * 如果上传或者下载的是文件，文件片段的校验码。采用crc32作为校验
     *
     * @param contents
     * @return
     */
    public static String generateContentsCheckSum(byte[] contents) {
        CRC32 crc32 = new CRC32();
        crc32.update(contents);
        return Long.toHexString(crc32.getValue());
    }
}
