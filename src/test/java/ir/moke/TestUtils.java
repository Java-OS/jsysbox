package ir.moke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static void createVirtualDisk(File diskFile, long size) {
        try (RandomAccessFile raf = new RandomAccessFile(diskFile, "rw")) {
            raf.setLength(size);
            logger.info("### Virtual Disk Created {} ###", diskFile.getAbsolutePath());
            logger.info("Disk Size : {}", Files.readString(Path.of("/sys/block/loop0/size")).trim());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void connectDisk(File diskFile) {
        logger.info("### Connect disk executed {} ###", diskFile.getAbsolutePath());
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/usr/sbin/losetup", "/dev/loop0", diskFile.getAbsolutePath());
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void disConnectDisk() {
        logger.info("### Disconnect disk executed ###");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/usr/sbin/losetup", "-d", "/dev/loop0");
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleep(long mils) {
        try {
            Thread.sleep(mils);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
