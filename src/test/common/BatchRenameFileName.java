package common;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BatchRenameFileName {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRenameFileName.class);
    private static final Pattern SERIES_PATTERN = Pattern.compile(
            Pattern.quote("(") + Pattern.quote("\\d") + "\\{(\\d+||\\d+,\\d+)}" + Pattern.quote(")"));

    private static String input;

    public static void batchRenameFile(Pattern filePattern, Collection<File> fileCollection,
                                       String renameFileName) {

        if (CollectionUtils.isEmpty(fileCollection)) {
            LOGGER.warn("scan dir is empty");
            return;
        }
        String suffix = null;
        Iterator<File> iterator = fileCollection.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            String fileName = file.getName();
            Matcher matcher = filePattern.matcher(fileName);
            if (!matcher.find()) {
                continue;
            }
            if (StringUtils.isBlank(suffix)) {
                //extract file name suffix
                int lastDotIndex = fileName.lastIndexOf(".");
                if (lastDotIndex == -1) {
                    suffix = ".mp4";
                } else {
                    suffix = fileName.substring(lastDotIndex);
                }
            }
            String series = matcher.group(1);
            File renameFile = new File(file.getParent(), renameFileName + " " + series + suffix);
            if (!file.renameTo(renameFile)) {
                LOGGER.error("file rename failed||initName={}||renameName={}", fileName, renameFile.getName());
            } else {
                LOGGER.info("file rename success||initName={}||renameName={}", file.getName(), renameFile.getName());
            }

        }

    }

    static boolean validateInput(Function<String, Boolean> validateFunction, String tips) {
        return validateInput(validateFunction, 3, tips);
    }

    static boolean validateInput(Function<String, Boolean> validateFunction, int maxRetryTimes, String tips) {
        int i = 0;
        Scanner scanner = new Scanner(System.in);
        while (i++ < maxRetryTimes) {
            LOGGER.info("wait for input {} ", tips);
            input = scanner.nextLine();
            if (!validateFunction.apply(input)) {
                LOGGER.warn("input is illegal,input={}", input);
            } else {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Function<String, Boolean> validateDirExistsFunction =
                dirPath -> {
                    File file = new File(dirPath);
                    return file.exists() && file.isDirectory() && file.canExecute();
                };
        Function<String, Boolean> validateSeriesFunction =
                patternStr -> SERIES_PATTERN.matcher(patternStr).find();
        if (!validateInput(validateDirExistsFunction, "重命名根目录")) {
            LOGGER.error("not legal directory path");
            return;
        }
        File fileDir = new File(input);
        if (!validateInput(validateSeriesFunction, "文件集数正则表达式")) {
            LOGGER.error("not legal file format");
            return;
        }
        Pattern filePattern = Pattern.compile(input);
        if (!validateInput(StringUtils::isNotEmpty, "修改后的名称")) {
            LOGGER.error("rename file name is empty");
            return;
        }
        String renameFileName = input;
        List<File> fileList = FileUtils.listFiles(fileDir, FileFileFilter.FILE, TrueFileFilter.TRUE)
                .stream().filter(File::isFile).collect(Collectors.toList());
        batchRenameFile(filePattern, fileList, renameFileName);

    }
}
