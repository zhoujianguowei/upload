package rpc.thrift.file.service;

import common.ClientUploadStatus;
import net.jodah.concurrentunit.Waiter;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.thrift.file.transfer.FileTypeEnum;
import worker.DefaultClientWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileUploadIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadIntegrationTest.class);
    private static final String SERVER_HOST = "127.0.0.1";
    private static final String TEST_FILE_PATH = "/home/zbj/下载/opencode.deb";
    private static final String TEST_DIR_PATH = "/home/zbj/study/visualCode";
    private static final String SAVE_PARENT_PATH = "/tmp/upload_test_output";

    private FileTransferServer server;
    private ExecutorService serverExecutor;
    private AtomicBoolean serverStarted = new AtomicBoolean(false);
    private int serverPort;

    @Before
    public void setUp() throws Exception {
        File saveDir = new File(SAVE_PARENT_PATH);
        if (saveDir.exists()) {
            FileUtils.cleanDirectory(saveDir);
        } else {
            saveDir.mkdirs();
        }

        FileTransferServer.setFileUploadSaveParentPath(SAVE_PARENT_PATH);

        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(() -> {
            try {
                FileTransferServer serverTmp = FileTransferServer.getSingleTon();
                serverPort = FileTransferServer.FILE_HANDLER_SERVER_PORT;
                serverTmp.asyncLaunchFileHandlerService();
            } catch (Exception e) {
                LOGGER.error("server start error", e);
            }
        });

        for (int i = 0; i < 30; i++) {
            Thread.sleep(500);
            if (FileTransferServer.getFileUploadSaveParentPath() != null) {
                serverStarted.set(true);
                break;
            }
        }

        if (!serverStarted.get()) {
            throw new RuntimeException("Server failed to start");
        }
    }

    @After
    public void tearDown() throws Exception {
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
            serverExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }

        File saveDir = new File(SAVE_PARENT_PATH);
        if (saveDir.exists()) {
            FileUtils.cleanDirectory(saveDir);
        }
    }

    @Test
    public void testUploadSingleFile() throws Exception {
        File testFile = new File(TEST_FILE_PATH);
        if (!testFile.exists()) {
            LOGGER.info("Test file not found, skipping: {}", TEST_FILE_PATH);
            return;
        }

        FileTransferClient client = new FileTransferClient();
        client.uploadFile(TEST_FILE_PATH, SERVER_HOST, serverPort, 10000, null);

        String expectedFileName = testFile.getName();
        File uploadedFile = new File(SAVE_PARENT_PATH, expectedFileName);

        if (!uploadedFile.exists()) {
            throw new AssertionError("Uploaded file not found: " + uploadedFile.getAbsolutePath());
        }

        if (uploadedFile.length() != testFile.length()) {
            throw new AssertionError("File size mismatch. Expected: " + testFile.length() + ", Actual: " + uploadedFile.length());
        }

        String sourceMd5 = calculateMd5(testFile);
        String uploadedMd5 = calculateMd5(uploadedFile);
        if (!sourceMd5.equals(uploadedMd5)) {
            throw new AssertionError("File content MD5 mismatch");
        }

        LOGGER.info("File upload test passed: {}", expectedFileName);
    }

    @Test
    public void testUploadDirectory() throws Exception {
        File testDir = new File(TEST_DIR_PATH);
        if (!testDir.exists()) {
            LOGGER.info("Test directory not found, skipping: {}", TEST_DIR_PATH);
            return;
        }

        FileTransferClient client = new FileTransferClient();
        client.uploadFile(TEST_DIR_PATH, SERVER_HOST, serverPort, 10000, null);

        String dirName = testDir.getName();
        File uploadedDir = new File(SAVE_PARENT_PATH, dirName);

        if (!uploadedDir.exists() || !uploadedDir.isDirectory()) {
            throw new AssertionError("Uploaded directory not found: " + uploadedDir.getAbsolutePath());
        }

        int sourceFileCount = countFiles(testDir);
        int uploadedFileCount = countFiles(uploadedDir);
        if (sourceFileCount != uploadedFileCount) {
            throw new AssertionError("File count mismatch. Expected: " + sourceFileCount + ", Actual: " + uploadedFileCount);
        }

        LOGGER.info("Directory upload test passed: {} with {} files", dirName, sourceFileCount);
    }

    @Test
    public void testUploadFileAndDirectory() throws Exception {
        File testFile = new File(TEST_FILE_PATH);
        File testDir = new File(TEST_DIR_PATH);
        boolean fileExists = testFile.exists();
        boolean dirExists = testDir.exists();

        if (!fileExists && !dirExists) {
            LOGGER.info("Neither test file nor directory found, skipping test");
            return;
        }

        FileTransferClient client = new FileTransferClient();

        if (fileExists) {
            client.uploadFile(TEST_FILE_PATH, SERVER_HOST, serverPort, 10000, null);
            File uploadedFile = new File(SAVE_PARENT_PATH, testFile.getName());
            if (!uploadedFile.exists()) {
                throw new AssertionError("Uploaded file not found: " + uploadedFile.getAbsolutePath());
            }
            if (uploadedFile.length() != testFile.length()) {
                throw new AssertionError("File size mismatch for: " + testFile.getName());
            }
            LOGGER.info("File upload verified: {}", testFile.getName());
        }

        if (dirExists) {
            client.uploadFile(TEST_DIR_PATH, SERVER_HOST, serverPort, 10000, null);
            File uploadedDir = new File(SAVE_PARENT_PATH, testDir.getName());
            if (!uploadedDir.exists() || !uploadedDir.isDirectory()) {
                throw new AssertionError("Uploaded directory not found: " + uploadedDir.getAbsolutePath());
            }
            LOGGER.info("Directory upload verified: {}", testDir.getName());
        }

        LOGGER.info("Combined file and directory upload test passed");
    }

    private int countFiles(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return 0;
        }
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFiles(file);
                }
            }
        }
        return count;
    }

    private String calculateMd5(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] digest = md.digest(md.digest(fileBytes));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
