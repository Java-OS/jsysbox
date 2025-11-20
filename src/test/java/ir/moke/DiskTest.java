package ir.moke;

import ir.moke.jsysbox.JSysboxException;
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
            throw new JSysboxException(e);
        }
    }

    @Test
    @Order(1)
    public void checkDisks() {
        logger.info("Execute <checkDisks>");
        String[] disks = JDiskManager.disks();
        assertTrue(disks.length != 0);
    }

    @Test
    @Order(2)
    public void checkMBRPartitionTableType() {
        logger.info("Execute <checkMBRPartitionTableType>");
        JDiskManager.initializePartitionTable(DISK_FILE.getAbsolutePath(), PartitionTable.MBR);
        PartitionTable partitionTable = JDiskManager.partitionTableType(DISK_BLK_PATH);
        assertEquals(PartitionTable.MBR, partitionTable);
    }

    @Test
    @Order(3)
    public void checkDiskInformation() {
        logger.info("Execute <checkDiskInformation>");
        Disk diskInformation = JDiskManager.getDiskInformation(LOOP_DISK_PATH);
        System.out.println(diskInformation);
        System.out.println(diskInformation);
        assertNotNull(diskInformation);
        assertEquals(LOOP_DISK_PATH, diskInformation.blk());
        assertEquals(0, diskInformation.partitions());
        assertEquals(DISK_SIZE, diskInformation.size());
        assertEquals(PartitionTable.MBR, diskInformation.partitionTable());
    }

    /**
     * Partition table type is DOS (MBR)
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
        Assertions.assertNotNull(disk);
        System.out.println("Disk sector size: " + disk.sectors());

        // First primary partition
        long sector_start = PARTITION_ALIGNMENT;
        long sector_size = JDiskManager.calculatePartitionSectorSize(50);
        System.out.printf("Creating primary partition #1 - start:[%d] size:[%d]%n", sector_start, sector_size);
        JDiskManager.createPartition(LOOP_DISK_PATH, 0, sector_start, sector_size, FilesystemType.LINUX);

        // Second extended partition
        sector_start = sector_size + PARTITION_ALIGNMENT;
        sector_size = disk.sectors() - sector_start; // whole available free space
        System.out.printf("Creating primary partition #2 - start:[%d] size:[%d]%n", sector_start, sector_size);
        JDiskManager.createExtendedPartition(LOOP_DISK_PATH, 1, sector_start, sector_size);

        // Third logical partition
        sector_start = sector_start + PARTITION_ALIGNMENT;
        sector_size = JDiskManager.calculatePartitionSectorSize(10);
        System.out.printf("Creating logical partition #5 - start:[%d] size:[%d]%n", sector_start, sector_size);
        JDiskManager.createLogicalPartition(LOOP_DISK_PATH, 4, sector_start, sector_size, FilesystemType.NTFS);

        // Fourth partition (logical 2)
        sector_start = sector_start + sector_size + PARTITION_ALIGNMENT;
        sector_size = disk.sectors() - sector_start;
        System.out.printf("Creating logical partition #6 - start:[%d] size:[%d]%n", sector_start, sector_size);
        JDiskManager.createLogicalPartition(LOOP_DISK_PATH, 5, sector_start, sector_size, FilesystemType.LINUX);
    }


    @Test
    @Order(5)
    public void checkListPartitions() {
        logger.info("Execute <checkListPartitions>");
        List<PartitionInformation> partitions = JDiskManager.partitions(LOOP_DISK_PATH);
        for (PartitionInformation partition : partitions) {
            System.out.println(partition);
        }
    }

//    @Test
//    @Order(5)
//    public void createGPTPartitionTable() {
//        logger.info("Execute <createGPTPartitionTable>");
//        JDiskManager.initializePartitionTable(DISK_FILE.getAbsolutePath(), PartitionTable.GPT);
//        PartitionTable partitionTable = JDiskManager.partitionTableType(DISK_BLK_PATH);
//        assertEquals(PartitionTable.GPT, partitionTable);
//    }

//    @Test
//    @Order(5)
//    public void checkGPTCreatePartition() {
//        logger.info("Execute <checkGPTCreatePartition>");
//        final int PARTITION_ALIGNMENT = 2048 - 1;
//
//        createGPTPartitionTable();
//        Disk disk = JDiskManager.getDiskInformation(LOOP_DISK_PATH);
//
//        // First partition
//        long startSector = JDiskManager.calculatePartitionSectorSize(1);
//        long endSector = JDiskManager.calculatePartitionSectorSize(50) + PARTITION_ALIGNMENT;
//        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);
//
//        // Second partition
//        startSector = endSector + 1;
//        endSector = startSector + JDiskManager.calculatePartitionSectorSize(30) - 1;
//        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.NTFS);
//
//        // Third partition
//        startSector = endSector + 1;
//        endSector = startSector + JDiskManager.calculatePartitionSectorSize(10) - 1;
//        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);
//
//        // Last partition
//        startSector = endSector + 1;
//        endSector = disk.sectors() - PARTITION_ALIGNMENT;
//        JDiskManager.createPartition(LOOP_DISK_PATH, startSector, endSector, FilesystemType.EXT4);
//    }
//
//    @Test
//    @Order(6)
//    public void checkGetAllDiskInformations() {
//        List<Disk> disks = JDiskManager.getAllDiskInformation();
//        disks.forEach(System.out::println);
//    }
//
//    @Test
//    @Order(7)
//    public void checkListPartitions() {
//        List<PartitionInformation> partitions = JDiskManager.partitions();
//        for (PartitionInformation partition : partitions) {
//            System.out.println(partition);
//        }
//    }
//
//    @Test
//    @Order(7)
//    public void checkDevDisk() {
//        JDiskManager.initDevDisk();
//    }
}
