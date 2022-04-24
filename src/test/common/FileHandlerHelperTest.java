package common;

import org.apache.thrift.util.AllocateSourcesUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class FileHandlerHelperTest {

    @Test
    public void splitList() {
        List<Integer> sourceList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> splitList = AllocateSourcesUtils.averageSource(sourceList, 3);
        assertTrue(splitList.get(0).get(0) == 1);
        assertTrue(splitList.get(1).get(0) == 5);
        assertTrue(splitList.get(2).get(0) == 8);
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