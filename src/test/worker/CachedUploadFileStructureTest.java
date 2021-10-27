package worker;

import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import rpc.thrift.file.transfer.FileTypeEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class CachedUploadFileStructureTest {
    CachedUploadFileStructure cachedUploadFileStructure;

    @Before
    public void init() {
        cachedUploadFileStructure = new CachedUploadFileStructure();
        cachedUploadFileStructure.setFileBytesLength(20L);
        cachedUploadFileStructure.setFileTypeEnum(FileTypeEnum.FILE_TYPE);
        try {
            cachedUploadFileStructure.setRandomAccessFile(new RandomAccessFile(new File("d:/test"), "r"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void serialTest() {
        String serialTest = JSON.toJSONString(cachedUploadFileStructure);
        System.out.println("serialTest:" + serialTest);
        CachedUploadFileStructure restore = JSON.parseObject(serialTest, CachedUploadFileStructure.class);
        System.out.println(restore.getFileTypeEnum());
    }
}