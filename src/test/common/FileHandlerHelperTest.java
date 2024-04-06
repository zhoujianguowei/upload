package common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.thrift.util.AllocateSourcesUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class FileHandlerHelperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerHelperTest.class);

    @Test
    public void splitList() {
        List<Integer> sourceList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> splitList = AllocateSourcesUtils.averageSource(sourceList, 3);
        assertEquals(1, (int) splitList.get(0).get(0));
        assertEquals(5, (int) splitList.get(1).get(0));
        assertEquals(8, (int) splitList.get(2).get(0));
    }

    @Test
    public void batchRenameTest() {
        File rootDir = new File("H:\\电视剧");
        Collection<File> fileList = FileUtils.listFiles(rootDir, FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY);
        Map<File, List<File>> parentFile2ListFile = fileList.stream().filter(File::isFile).collect(Collectors.groupingBy(File::getParentFile
        ));
        for (Map.Entry<File, List<File>> entry : parentFile2ListFile.entrySet()) {
            LOGGER.info("parentDir={}||fileSize={}", entry.getKey().getAbsoluteFile(), entry.getValue().size());
        }

    }

    @Test
    public void testFinally() {
        for (int i = 0; i < 10; i++) {
            try {
                if (i < 10) {
                    return;
                }
            } finally {
                System.out.println("index=" + i);
            }
        }
    }
}