package worker;

import rpc.thrift.file.transfer.EncryptTypeEnum;
import rpc.thrift.file.transfer.FileTypeEnum;

import java.io.RandomAccessFile;

public class CachedUploadFileStructure {
    private String fileName;
    private String saveParentFolder;
    private FileTypeEnum fileTypeEnum;
    private String relativePath;
    private long cachedFileOffset;
    private Long uploadStartDate;
    private String fileIdentifier;
    private Long fileBytesLength;
    private EncryptTypeEnum encryptTypeEnum;
    private transient RandomAccessFile randomAccessFile;
    /**
     * 临时保存的文件名
     */
    private String tmpFileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSaveParentFolder() {
        return saveParentFolder;
    }

    public void setSaveParentFolder(String saveParentFolder) {
        this.saveParentFolder = saveParentFolder;
    }

    public FileTypeEnum getFileTypeEnum() {
        return fileTypeEnum;
    }

    public void setFileTypeEnum(FileTypeEnum fileTypeEnum) {
        this.fileTypeEnum = fileTypeEnum;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public long getCachedFileOffset() {
        return cachedFileOffset;
    }

    public void setCachedFileOffset(long cachedFileOffset) {
        this.cachedFileOffset = cachedFileOffset;
    }

    public Long getUploadStartDate() {
        return uploadStartDate;
    }

    public void setUploadStartDate(Long uploadStartDate) {
        this.uploadStartDate = uploadStartDate;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public Long getFileBytesLength() {
        return fileBytesLength;
    }

    public void setFileBytesLength(Long fileBytesLength) {
        this.fileBytesLength = fileBytesLength;
    }

    public EncryptTypeEnum getEncryptTypeEnum() {
        return encryptTypeEnum;
    }

    public void setEncryptTypeEnum(EncryptTypeEnum encryptTypeEnum) {
        this.encryptTypeEnum = encryptTypeEnum;
    }

    public String getTmpFileName() {
        return tmpFileName;
    }

    public void setTmpFileName(String tmpFileName) {
        this.tmpFileName = tmpFileName;
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    public void setRandomAccessFile(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public String toString() {
        return "CachedUploadFileStructure{" +
                "fileName='" + fileName + '\'' +
                ", saveParentFolder='" + saveParentFolder + '\'' +
                ", fileTypeEnum=" + fileTypeEnum +
                ", relativePath='" + relativePath + '\'' +
                ", cachedFileOffset=" + cachedFileOffset +
                ", uploadStartDate=" + uploadStartDate +
                ", fileIdentifier='" + fileIdentifier + '\'' +
                ", fileBytesLength=" + fileBytesLength +
                ", encryptTypeEnum=" + encryptTypeEnum +
                ", tmpFileName='" + tmpFileName + '\'' +
                '}';
    }
}
