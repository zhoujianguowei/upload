package org.apache.thrift.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * 存储容量格式化工具
 */
public class StorageFormat {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFormat.class);
    //默认采用1024作为转换单位
    public static Long unitSize = 1024L;
    private static List<String> unitList = Arrays.asList(new String[]{"byte", "kb", "mb", "gb", "tb", "pb",
            "eb", "zb"});
    //最多保留3位小数
    private static DecimalFormat REMAIN_MAX_THREE_FRACTION_FORMAT = new DecimalFormat(".###");

    /**
     * 指定的存储容量单位换算到另一个存储单位，以135.7GB形式返回
     * 最多返回5位小数
     *
     * @param format        12MB形式的数据
     * @param transformUnit 转换的单位
     * @return
     */
    public static String formatStrSize(String format, String transformUnit) {
        StorageUnitEnum.unitSize = StorageFormat.unitSize;
        if (StringUtils.isBlank(format) || StringUtils.isBlank(transformUnit)) {
            return null;
        }
        int lastDigitIndex = -1;
        format = format.toLowerCase().trim();
        transformUnit = transformUnit;
        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (!Character.isDigit(ch) && ch != '.') {
                break;
            }
            lastDigitIndex++;
        }
        double stValue = Double.parseDouble(format.substring(0, lastDigitIndex + 1));
        String originUnit = format.substring(lastDigitIndex + 1).toLowerCase();
        transformUnit = transformUnit.toLowerCase();
        if (unitList.indexOf(originUnit) == -1 || unitList.indexOf(transformUnit) == -1) {
            return null;
        }
        double unitTransformMagnitude = StorageUnitEnum.transUnitTransform(unitList.indexOf(originUnit), unitList
                .indexOf(transformUnit));
        stValue *= unitTransformMagnitude;
        return REMAIN_MAX_THREE_FRACTION_FORMAT.format(stValue) + transformUnit.toUpperCase();
    }

    public static double transformSize(String format, String transformUnit) {
        String formatSize = formatStrSize(format, transformUnit);
        if (StringUtils.isBlank(formatSize)) {
            return -1;
        }
        int transformUnitIndex = formatSize.lastIndexOf(transformUnit.toUpperCase());
        return Double.parseDouble(formatSize.substring(0, transformUnitIndex));
    }

    public static String formatStorageSize(String initFormat) {
        return formatStorageSize(initFormat, ".###");
    }

    /**
     * 格式化显示当前的存储容量，最多保留3位小数
     *
     * @param initFormat
     * @return
     */
    public static String formatStorageSize(String initFormat, String numberFormat) {
        DecimalFormat decimalFormat = new DecimalFormat(numberFormat);
        Pattern pattern = compile("(\\d*\\.?\\d*)(([a-zA-Z]){2,4})", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(initFormat);
        if (StringUtils.isBlank(initFormat)) {
            return "0byte";
        }
        initFormat = initFormat.toLowerCase();
        if (matcher.matches()) {
            double storageValue = Double.parseDouble(matcher.group(1));
            String storageUnit = matcher.group(2).toLowerCase();
            int originIndex = unitList.indexOf(storageUnit);
            if (originIndex == -1) {
                LOGGER.error("invalid storage format:{}", initFormat);
                throw new RuntimeException("not a valid storage unit");
            }
            //超过存储表示范围
            if (originIndex == unitList.size() - 1 && storageValue > StorageUnitEnum.unitSize) {
                return initFormat;
            }
            //小于存储表示范围
            if (originIndex == 0 && storageValue < 1) {
                return initFormat;
            }
            //storageValue小于1，向下转换；大于1024向上转换
            DecimalFormat dfFormat = new DecimalFormat("#");
            dfFormat.setMaximumFractionDigits(20);
            if (storageValue < 1) {
                String downStorageUnit = unitList.get(originIndex - 1);
                double downStorageValue = Math.pow(StorageUnitEnum.unitSize, 1) * storageValue;
                return formatStorageSize(dfFormat.format(downStorageValue) + downStorageUnit, numberFormat);
            } else if (storageValue > StorageUnitEnum.unitSize) {
                String upStorageUnit = unitList.get(originIndex + 1);
                double upStorageValue = Math.pow(StorageUnitEnum.unitSize, -1) * storageValue;
                return formatStorageSize(dfFormat.format(upStorageValue) + upStorageUnit, numberFormat);
            }
            return decimalFormat.format(storageValue) + storageUnit.toLowerCase();
        } else {
            LOGGER.error("not a valid storage format:{}", initFormat);
            throw new RuntimeException("not a valid storage size format");
        }
    }

    static void getClassInfo(Class c) {
        if (c == Object.class) {
            return;
        }
        Class superCls = c.getSuperclass();
        System.out.println(c + "的父类信息*********************");
        while (superCls != null) {
            System.out.println("\t" + superCls);
            getClassInfo(superCls);
            superCls = superCls.getSuperclass();
        }
        System.out.println("接口信息:");
        Class<?> interfaces[] = c.getInterfaces();
        for (Class inter : interfaces) {
            System.out.println("\t" + inter);
        }
        List<Field> attrFields = Arrays.asList(c.getDeclaredFields());
        for (Field field : attrFields) {
            String fieldName = field.getName();
            Class fieldClass = field.getType();
            System.out.println("\t" + "fieldName:" + fieldName + "\tType:" + fieldClass);
        }
    }

    private enum StorageUnitEnum {
        BYTE("byte"),
        KB("kb"), MB("mb"),
        GB("gb"), TB("tb"),
        PB("pb"), EB("eb"), ZB("zb");

        protected static long unitSize = 1024L;//默认采用1024作为单位
        private String unitName;
        StorageUnitEnum(String unitName) {
            this.unitName = unitName;
        }

        /**
         * 不同进制存储单位转换
         *
         * @param originUnitIndex
         * @param transformUnitIndex
         * @return
         */
        static double transUnitTransform(int originUnitIndex, int transformUnitIndex) {
            return Math.pow(unitSize, originUnitIndex - transformUnitIndex);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}

