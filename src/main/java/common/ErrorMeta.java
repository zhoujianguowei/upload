package common;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

/**
 * 错误校验类
 */
public class ErrorMeta<T> {
    private List<T> errorMsgList = Lists.newArrayList();
    private List<T> infoMsgList = Lists.newArrayList();

    public void addErrorMsg(T error) {
        errorMsgList.add(error);
    }

    public void addInfoMsg(T info) {
        infoMsgList.add(info);
    }

    public boolean isLegal() {
        return errorMsgList.isEmpty();
    }

    public String getErrorMsg(Function<List<T>, String> function) {
        if (isLegal()) {
            return StringUtils.EMPTY;
        }
        return function.apply(errorMsgList);
    }

    public List<T> getAllErrorMsgList() {
        return errorMsgList;
    }

    public List<T> getAllInfoMsgList() {
        return infoMsgList;
    }

    /**
     * 获取当前错误信息详情
     *
     * @return
     */
    public String getDefaultErrorMsg() {
        Function<List<T>, String> function = errorInfo -> StringUtils.join(errorInfo, StringUtils.LF);
        return getErrorMsg(function);
    }

    public ErrorMeta combine(ErrorMeta<T> errorMeta) {
        this.errorMsgList.addAll(errorMeta.getAllErrorMsgList());
        this.infoMsgList.addAll(errorMeta.getAllInfoMsgList());
        return this;
    }
}
