package ir.moke;

import ir.moke.jsysbox.disk.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiskTest {
    private static final Logger logger = LoggerFactory.getLogger(DiskTest.class);

    private static final String DISK_BLK_PATH = "/tmp/virtual_disk.img";
    private static final String LOOP_DISK_PATH = "/dev/loop0";
    private static final File DISK_FILE = new File(DISK_BLK_PATH);
    private static final long DISK_SIZE = 100 * 1024 * 1024;

    @BeforeAll
    public static void init() {
        logger.info("Execute <init>");
        // first try to disconnect
        TestUtils.disConnectDisk();

        // now create disk and connect
        TestUtils.createVirtualDisk(DISK_FILE, DISK_SIZE);
        TestUtils.connectDisk(DISK_FILE);
    }

    @AfterAll
    public static void destroy() {
        logger.info("Execute <destroy>");
        TestUtils.disConnectDisk();
    }

    @Test
    @Order(0)
    public void checkDiskConnected() {
        logger.info("Execute <checkDiskConnected>");
        try {
            String value = Files.readString(Path.of("/sys/block/loop0/size")).trim();
            assertTrue(Integer.parseInt(value) > 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    public void createGPTPartitionTable() {
        logger.info("Execute <createGPTPartitionTable>");
        JDiskManager.initializePartitionTable(DISK_FILE.getAbsolutePath(), PartitionTable.GPT);
        PartitionTable partitionTable = JDiskManager.partitionTableType(DISK_BLK_PATH);
        assertEquals(PartitionTable.GPT, partitionTable);
    }

    @Test
    @Order(2)
    public void checkMBRPartitionTableType() {
        logger.info("Execute <checkMBRPartitionTableType>");
        JDiskManager.initializePartitionTable(DISK_FILE.getAbsolutePath(), PartitionTable.MSDOS);
        PartitionTable partitionTable = JDiskManager.partitionTableType(DISK_BLK_PATH);
        assertEquals(PartitionTable.MSDOS, partitionTable);
    }

    @Test
    @Order(3)
    public void checkDiskInformation() {
        logger.info("Execute <checkDiskInformation>");
        Disk diskInformation = JDiskManager.getDiskInformation(LOOP_DISK_PATH);
        System.out.println(diskInformation);
        assertNotNull(diskInformation);
        assertEquals(LOOP_DISK_PATH, diskInformation.blk());
        assertEquals(0, diskInformation.partitions());
        assertEquals(DISK_SIZE, diskInformation.size());
        assertEquals(PartitionTable.MSDOS, diskInformation.partitionTable());
    }

    /**
     * Partition table type is MSDOS (MBR)
     * sector size is 512 bytes
     * start sector should be 2048
     * <p>
     * partition alignment = 2048
     */
    @Test
    @Order(4)
    public void checkMBRCreatePartition() {
        logger.info("Execute <checkMBRCreatePartition>");
        final int PARTITION_ALIGNMENT = 2048;

        Disk disk = JDiskManager.getDiskInformation(LOOP_DISK_PATH);

        // First partition (primary)
        long startSector = JDiskManager.calculatePartitionSectorSize(1);
        long endSector = JDiskManager.calculatePartitionSectorSize(50) + PARTITION_ALIGNMENT;
        System.out.println(startSector + "    " + endSector);
        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);

        // Second partition (extended)
        startSector = endSector + 1;
        endSector = disk.sectors() - 1; // whole of disk
        System.out.println(startSector + "    " + endSector);
        JDiskManager.createExtendedPartition(LOOP_DISK_PATH, startSector, endSector);

        // Third partition (logical 1)
        PartitionInformation extendedPartition = JDiskManager.getPartitionInformation(DISK_BLK_PATH)[1];
        startSector = extendedPartition.startSector + PARTITION_ALIGNMENT;
        endSector = extendedPartition.startSector + JDiskManager.calculatePartitionSectorSize(10) + PARTITION_ALIGNMENT;
        System.out.println(startSector + "    " + endSector);
        JDiskManager.createLogicalPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.NTFS);

        // Fourth partition (logical 2)
        PartitionInformation firstLogicalPartition = JDiskManager.getPartitionInformation(DISK_BLK_PATH)[2];
        startSector = firstLogicalPartition.endSector + PARTITION_ALIGNMENT;
        endSector = extendedPartition.endSector;
        System.out.println(startSector + "    " + endSector);
        JDiskManager.createLogicalPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);
    }

    @Test
    @Order(5)
    public void checkGPTCreatePartition() {
        logger.info("Execute <checkGPTCreatePartition>");
        final int PARTITION_ALIGNMENT = 2048 - 1;

        createGPTPartitionTable();
        Disk disk = JDiskManager.getDiskInformation(LOOP_DISK_PATH);

        // First partition
        long startSector = JDiskManager.calculatePartitionSectorSize(1);
        long endSector = JDiskManager.calculatePartitionSectorSize(50) + PARTITION_ALIGNMENT;
        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);

        // Second partition
        startSector = endSector + 1;
        endSector = startSector + JDiskManager.calculatePartitionSectorSize(30) - 1;
        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.NTFS);

        // Third partition
        startSector = endSector + 1;
        endSector = startSector + JDiskManager.calculatePartitionSectorSize(10) - 1;
        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);

        // Last partition
        startSector = endSector + 1;
        endSector = disk.sectors() - 1;
        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);
    }

    @Test
    @Order(6)
    public void checkAllDiskInformations() {
        logger.info("Execute <checkAllDiskInformations>");
        List<Disk> allDiskInformation = JDiskManager.getAllDiskInformation();
        for (Disk disk : allDiskInformation) {
            System.out.println(disk);
        }
    }
}
