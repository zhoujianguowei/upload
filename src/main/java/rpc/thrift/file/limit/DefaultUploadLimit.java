package rpc.thrift.file.limit;

import com.google.common.util.concurrent.RateLimiter;
import config.ConfigDataHelper;
import cons.BusinessConstant;
import rpc.thrift.file.transfer.FileUploadRequest;

public class DefaultUploadLimit implements UploadLimit {
    /**
     * 客户端全局限速
     */
    protected RateLimiter globalRateLimiter = RateLimiter.create(Double.parseDouble(ConfigDataHelper.getStoreConfigData(BusinessConstant.ConfigData.CLIENT_UPLOAD_LIMIT_SPEED_THRESHOLD)));

    @Override
    public void blockLimit(FileUploadRequest uploadRequest) {
        //最低处理限流
        int minLimit = 10;
        globalRateLimiter.acquire(Math.max(minLimit, uploadRequest.getBytesLength()));
    }
}
