package org.apache.thrift.util;

import java.util.ArrayList;
import java.util.List;

public class AllocateSourcesUtils
{
    /**
     * @param sources  total sources
     * @param userSize total user size ,such as thread size
     * @param little   if sources can't be allocate equally,
     *                 to decide which part get plus one resource,default the small index get more than 1
     * @param <T>      resouces type
     * @return size number of userSize competitors
     */
    public static <T> List<List<T>> averageSource(List<T> sources, int userSize, boolean... little)
    {
        assert userSize > 0;
        boolean littleFirst = false;
        if (little.length == 0 || little[0]) {
            littleFirst = true;
        }
        int sourceSize = sources.size();
        List<List<T>> res = new ArrayList<>(userSize);
        int i, j, firstIndex = 0, maxPerResSize = sourceSize % userSize == 0 ? sourceSize / userSize : (sourceSize /
                userSize + 1);
        int overFlowCnt = maxPerResSize * userSize - sourceSize;
        for (i = 0; i < userSize; i++)
        {
            List<T> iAllocRes = new ArrayList<T>();
            for (j = firstIndex; j < firstIndex + maxPerResSize - 1; j++)
            { iAllocRes.add(sources.get(j)); }
            if (littleFirst && i < userSize - overFlowCnt && j < sourceSize) {
                iAllocRes.add(sources.get(j));
            } else if (!littleFirst && i >= overFlowCnt && j < sourceSize) {
                iAllocRes.add(sources.get(j));
            }
            firstIndex += iAllocRes.size();
            res.add(iAllocRes);
        }
        return res;
    }
}
