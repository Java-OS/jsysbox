/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ir.moke.jsysbox.disk;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class JDiskManager {

    static {
        JniNativeLoader.load("jdisk_manager");
    }

    /**
     * Mount partition
     *
     * @param blkPartition    partition block device
     *                        example: /dev/sda1
     * @param targetDirectory Mount point directory
     * @param type            Filesystem type
     * @param flags           mount flags
     * @param options         mount options
     * @return return true if successfully mounted
     */
    public native static boolean mount(String blkPartition, String targetDirectory, String type, int flags, String options);

    /**
     * unmount partition
     *
     * @param targetDirectory mount point directory
     * @return return true if successfully unmounted
     */
    public native static boolean umount(String targetDirectory);

    /**
     * Get list of available disk
     *
     * @return list block device address of available disks
     * example: ["/dev/sda", "/dev/sdb"]
     */
    public native static String[] disks();

    /**
     * get type of partition on DOS partition table type
     * @param blkDisk           hard drive block device
     * @param partitionNumber   number of partition
     * @return enum type of {@link PartitionType}
     */
    public static PartitionType partitionType(String blkDisk, int partitionNumber) {
        PartitionTable partitionTable = partitionTableType(blkDisk);
        if (partitionTable.equals(PartitionTable.GPT)) return PartitionType.PRIMARY;
        if (partitionNumber > 4) return PartitionType.LOGICAL;
        if (isExtended(blkDisk, partitionNumber)) return PartitionType.EXTENDED;
        return PartitionType.PRIMARY;
    }

    public native static String partitionUUID(String blkPartition);

    public native static String partitionLabel(String blkPartition);

    public native static long partitionBlockSize(String mountPoint);

    public native static long partitionAvailableSize(String mountPoint);

    /**
     * Activate swap partition
     *
     * @param blkPartition swap partition block device address
     *                     example: /dev/sdb2
     */
    public native static void swapOn(String blkPartition) throws JSysboxException;

    /**
     * Deactivate swap partition
     *
     * @param blkPartition swap partition block device address
     *                     example: /dev/sdb2
     */
    public native static void swapOff(String blkPartition) throws JSysboxException;

    /**
     * Write new partition table on disk
     *
     * @param blkDisk        block device address of disk
     *                       example: /dev/sdx
     * @param partitionTable type of partition table {@link PartitionInformation}
     */
    public native static void initializePartitionTable(String blkDisk, PartitionTable partitionTable);

    /**
     * Create new partition
     *
     * @param blkDisk             block device address of disk
     *                            example: /dev/sdx
     * @param partitionNumber     index of partition number
     * @param start               Partition start sector
     *                            NOTE: First partition started from 2048
     * @param size                Partition size
     * @param filesystemType      Type of partition {@link GptType}
     * @param isPrimary           set true if you want to create primary partition on MBR partition table
     */
    private native static void createPartition(String blkDisk,
                                               int partitionNumber,
                                               long start,
                                               long size,
                                               String filesystemType,
                                               boolean isPrimary);

    /**
     * delete partition
     *
     * @param blkDisk         block device address of disk
     *                        example: /dev/sdx
     * @param partitionNumber partition number
     */
    public native static void deletePartition(String blkDisk, int partitionNumber);

    /**
     * Set bootable flag on partition
     * Note: only work on MBR partition table
     *
     * @param blkDisk         block device address of disk
     *                        example: /dev/sdx
     * @param partitionNumber partition number
     */
    public native static void bootable(String blkDisk, int partitionNumber);

    /**
     * count of disk partitions
     * @param blkDisk         block device address of disk
     *                        example: /dev/sdx
     * @return partition count
     */
    public native static int partitionCount(String blkDisk);

    /**
     * Synchronize cached writes to persistent storage
     */
    public static native void sync();

    /**
     * Content of /proc/mounts
     *
     * @return return list of mount information
     */
    public static List<String> mounts() {
        try {
            return Files.readAllLines(Paths.get("/proc/mounts"));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Check partition mounted
     *
     * @param uuid partition by uuid
     * @return return true if mounted
     */
    public static boolean isMount(String uuid) {
        List<PartitionInformation> partitions = partitions();
        return partitions.stream().anyMatch(item -> item.uuid().equals(uuid));
    }

    /**
     * Check directory used as mount point
     *
     * @param mountPoint mount point directory
     * @return return true if exists in /proc/mounts
     */
    public static boolean isMountByMountPoint(String mountPoint) {
        List<String> mounts = mounts();
        if (mounts == null) return false;
        for (String line : mounts) {
            if (mountPoint.equals(line.split("\\s+")[1])) return true;
        }
        return false;
    }

    /**
     * List of all available partitions
     *
     * @return List of {@link PartitionInformation}
     */
    public static List<PartitionInformation> partitions() {
        List<PartitionInformation> list = new ArrayList<>();
        List<String> disks = Arrays.stream(disks()).filter(item -> !item.contains("sr")).toList();
        for (String disk : disks) {
            if (disk.contains("dm-")) disk = getLvmMapperPath(disk);
            return partitions(disk);
        }
        return list;
    }

    public static List<PartitionInformation> partitions(String blkDisk) {
        List<PartitionInformation> list = new ArrayList<>();
        Disk diskInformation = getDiskInformation(blkDisk);
        if (diskInformation == null) throw new JSysboxException("disk does not exists");
        String diskName = Path.of(diskInformation.blk()).getName(1).toString();
        List<MbrType> mbrTypes = readMBR(blkDisk);
        try (Stream<Path> stream = Files.list(Path.of("/sys/block/%s".formatted(diskName)))) {
            int disk_sector_size = diskInformation.sectorSize();
            List<Path> partitions = stream.filter(item -> item.toString().matches(".*/%s/%s.*".formatted(diskName, diskName))).sorted().toList();
            for (int i = 0; i < partitions.size(); i++) {
                Path partitionPath = partitions.get(i);
                String devPath = "/dev/" + partitionPath.getFileName().toString();
                String mountPoint = mountPoint(devPath);
                int number = Integer.parseInt(Files.readString(partitionPath.resolve("partition")).trim());
                PartitionType partitionType = partitionType(blkDisk, number);

                long sector_start = Long.parseLong(Files.readString(partitionPath.resolve("start")).trim());
                long sector_size = partitionType.equals(PartitionType.EXTENDED) ? getExtendedPartitionSize(blkDisk, diskInformation.sectorSize()) : Long.parseLong(Files.readString(partitionPath.resolve("size")).trim());
                long sector_end = sector_start + sector_size;
                String uuid = partitionUUID(devPath);
                String label = partitionLabel(devPath);
                long block_size = sector_size * disk_sector_size;
                long free_space_size = mountPoint != null ? partitionAvailableSize(mountPoint) : 0;
                FilesystemType filesystemType = mbrTypes.get(i);

                PartitionInformation partitionInformation = new PartitionInformation(
                        devPath,
                        number,
                        mountPoint,
                        uuid,
                        label,
                        filesystemType,
                        partitionType,
                        block_size,
                        disk_sector_size * sector_size,
                        disk_sector_size * free_space_size,
                        sector_start,
                        sector_end,
                        sector_size
                );
                list.add(partitionInformation);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * Get mount point of partition
     *
     * @param blkPartition block device of partition
     *                     example: /dev/sdx1
     * @return mount point address
     */
    public static String mountPoint(String blkPartition) {
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/mounts"));
            for (String line : lines) {
                String srcBlk = line.split("\\s+")[0];
                String mountPoint = line.split("\\s+")[1];
                Path path = Path.of(srcBlk);
                if (Files.isSymbolicLink(path) && path.toRealPath().toString().endsWith(blkPartition)) {
                    return mountPoint;
                } else if (srcBlk.endsWith(blkPartition)) {
                    return mountPoint;
                }
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return null;
    }

    public static void initDevDisk() {
        Path diskPath = Path.of("/dev/disk");

        if (!Files.exists(diskPath)) {
            try {
                Files.createDirectory(diskPath);
                Path byUUID = diskPath.resolve("by-uuid");
                Path byPath = diskPath.resolve("by-label");

                Files.createDirectory(byUUID);
                Files.createDirectory(byPath);

                List<PartitionInformation> partitions = partitions();
                for (PartitionInformation partition : partitions) {
                    String uuid = partition.uuid();
                    String label = partition.label();

                    Path path = Path.of(partition.blk());
                    if (uuid != null && !uuid.isEmpty()) {
                        Files.createSymbolicLink(byUUID.resolve(uuid), path);
                    }

                    if (label != null && !label.isEmpty()) {
                        Files.createSymbolicLink(byPath.resolve(label), path);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get {@link PartitionInformation} from uuid of partition
     *
     * @param uuid Partition uuid
     * @return {@link PartitionInformation}
     */
    public static PartitionInformation getPartitionByUUID(String uuid) {
        try (Stream<Path> listStream = Files.list(Path.of("/dev/disk/by-uuid/"))) {
            List<Path> list = listStream.toList();
            for (Path path : list) {
                if (Path.of(uuid).equals(path.getFileName())) {
                    Path realPath = getRealPathOfDevice(path);
                    return partitions(realPath.toString()).getFirst();
                }
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
        return null;
    }

    /**
     * Get {@link PartitionInformation} from label of partition
     *
     * @param label Partition label
     * @return {@link PartitionInformation}
     */
    public static PartitionInformation getPartitionByLabel(String label) {
        try (Stream<Path> listStream = Files.list(Path.of("/dev/disk/by-label/"))) {
            List<Path> list = listStream.toList();
            for (Path path : list) {
                if (Path.of(label).equals(path.getFileName())) {
                    Path realPath = getRealPathOfDevice(path);
                    return partitions(realPath.toString()).getFirst();
                }
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
        return null;
    }

    /**
     * Get mapper address of lvm logical partition /dev/mapper directory
     *
     * @param dmPath lvm block device dm address
     *               example: /dev/dm-0
     * @return mapper path of lvm logical partition
     */
    public static String getLvmMapperPath(String dmPath) {
        try (Stream<Path> listStream = Files.list(Path.of("/dev/mapper/"))) {
            List<Path> list = listStream.toList();
            for (Path path : list) {
                if (Path.of(dmPath).equals(path.toRealPath())) {
                    return path.toString();
                }
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
        return null;
    }

    public static boolean isLvm(String blkId) {
        return getLvmMapperPath(blkId) != null;
    }

    /**
     * Get root partition of current booted operating system
     *
     * @return {@link PartitionInformation}
     */
    public static PartitionInformation getRootPartition() {
        try {
            byte[] bytes = Files.readAllBytes(Path.of("/proc/cmdline"));
            String line = new String(bytes);
            String rootBlk = Arrays.stream(line.split(" "))
                    .filter(item -> item.contains("root="))
                    .map(item -> item.split("=", 2)[1])
                    .findFirst()
                    .orElse(null);
            if (rootBlk == null) return null;
            if (rootBlk.startsWith("UUID")) return getPartitionByUUID(rootBlk.split("=")[1]);
            return partitions(rootBlk).getFirst();
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

    /**
     * Check swap partition activated
     *
     * @param blkPartition Partition block device address
     * @return true if activated
     */
    public static boolean isSwapActivated(String blkPartition) {
        try {
            return Files.readAllLines(Path.of("/proc/swaps"))
                    .stream()
                    .skip(1)
                    .map(item -> item.split("\\s+")[0])
                    .anyMatch(item -> item.equals(blkPartition));
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

    private static Path getRealPathOfDevice(Path path) {
        try {
            Path linkPath = Files.readSymbolicLink(path);
            return path.getParent().resolve(linkPath).toRealPath();
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    /**
     * return list of compact disks like cdrom
     *
     * @return List of {@link CompactDisk}
     */
    public static List<CompactDisk> getCompactDisks() {
        List<CompactDisk> compactDiskList = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/sys/dev/cdrom/info"))
                    .stream()
                    .skip(2)
                    .filter(item -> !item.isEmpty())
                    .map(item -> item.split(":\\s+", 2)[1])
                    .toList();
            int columns = lines.getFirst().split("\\s+").length;
            List<String> values = new ArrayList<>();
            for (int i = 0; i < columns; i++) {
                for (String line : lines) {
                    String col = line.split("\\s+")[i];
                    values.add(col);
                }
                CompactDisk compactDisk = new CompactDisk(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5), values.get(6), values.get(7), values.get(8), values.get(9), values.get(10), values.get(11), values.get(12), values.get(13), values.get(14), values.get(15), values.get(16), values.get(17), values.get(18), values.get(19));
                compactDiskList.add(compactDisk);
                values.clear();
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return compactDiskList;
    }

    /**
     * Check block device name is compact disk
     *
     * @param blkName Block device name
     *                Example: sr0
     * @return true if is compact disk
     */
    public static boolean isCompactDisk(String blkName) {
        return getCompactDisks().stream().anyMatch(item -> item.drive_name().equals(blkName));
    }

    public static List<Disk> getAllDiskInformation() {
        List<Disk> disks = new ArrayList<>();
        String[] blkDisks = disks();
        for (String blkPath : blkDisks) {
            try {
                Disk disk = getDiskInformation(blkPath);
                if (disk != null) disks.add(disk);
            } catch (Exception ignore) {
            }
        }
        return disks;
    }

    /**
     * Get information of disk
     *
     * @param blkDisk Get information of disk
     * @return {@link Disk}
     */
    public static Disk getDiskInformation(String blkDisk) {
        try {
            if (isLvm(blkDisk)) return null;
            if (isCompactDisk(blkDisk.replaceAll("/dev/", ""))) return null;
            String blkName = blkDisk.substring(blkDisk.lastIndexOf("/") + 1);
            Path vendorPath = Path.of("/sys/block/%s/device/vendor".formatted(blkName));
            Path modelPath = Path.of("/sys/block/%s/device/model".formatted(blkName));
            Path sizePath = Path.of("/sys/block/%s/size".formatted(blkName));
            Path sectorSizePath = Path.of("/sys/block/%s/queue/hw_sector_size".formatted(blkName));

            String vendor = Files.exists(vendorPath) ? Files.readString(vendorPath).trim() : null;
            String model = Files.exists(modelPath) ? Files.readString(modelPath).trim() : null;
            Long sizeInSector = Files.exists(sizePath) ? Long.parseLong(Files.readString(sizePath).trim()) : null;
            int sectorSize = Files.exists(sectorSizePath) ? Integer.parseInt(Files.readString(sectorSizePath).trim()) : 512;

            int partitionCount = partitionCount(blkDisk);

            Long sizeInBytes = sizeInSector != null ? sizeInSector * sectorSize : null;

            PartitionTable partitionTable = partitionTableType(blkDisk);
            return new Disk(blkDisk, vendor, model, sizeInBytes, sizeInSector, partitionTable, partitionCount, sectorSize);
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

    /**
     * Calculate sector of partition
     * each sector is 512 byte
     * 1MB = (1 * 1024 * 1024) / 512
     *
     * @param size size of partition in MB
     * @return size of sectors
     */
    public static long calculatePartitionSectorSize(long size) {
        return (size * 1024 * 1024) / 512;
    }

    public static void createPartition(String blkDisk, int partitionNumber, long start, long size, MbrType type) {
        if (partitionNumber < 0) throw new JSysboxException("Partition number should be >= 0");
        createPartition(blkDisk, partitionNumber, start, size, "0x%02X".formatted(type.getCode()), true);
    }

    public static void createExtendedPartition(String blkDisk, int partitionNumber, long start, long size) {
        createPartition(blkDisk, partitionNumber, start, size, "0x%02X".formatted(MbrType.W95_EXT_LBA.getCode()), true);
    }

    public static void createLogicalPartition(String blkDisk, int partitionNumber, long start, long size, MbrType type) {
        createPartition(blkDisk, partitionNumber, start, size, "0x%02X".formatted(type.getCode()), false);
    }

    /**
     * Get current partition table type of disk
     *
     * @param blkDisk block device address of disk
     *                example: /dev/sda
     * @return return Type of partition table {@link PartitionInformation}
     */
    public static PartitionTable partitionTableType(String blkDisk) {
        try (RandomAccessFile disk = new RandomAccessFile(blkDisk, "r")) {
            byte[] mbr = new byte[512];
            disk.readFully(mbr);

            if ((mbr[510] & 0xFF) != 0x55 || (mbr[511] & 0xFF) != 0xAA) {
                throw new JSysboxException("invalid partition table signature");
            }

            int type = mbr[446 + 4] & 0xFF;

            if (type == 0xEE) {
                return PartitionTable.GPT;
            } else {
                return PartitionTable.MBR;
            }
        } catch (Exception e) {
            throw new JSysboxException(e.getMessage());
        }
    }

    private static long u32(byte[] buf, int pos) {
        return ((long) (buf[pos] & 0xFF)) |
               ((long) (buf[pos + 1] & 0xFF) << 8) |
               ((long) (buf[pos + 2] & 0xFF) << 16) |
               ((long) (buf[pos + 3] & 0xFF) << 24);
    }

    private static long u64(byte[] buf, int pos) {
        return ((long) (buf[pos] & 0xFF)) |
               ((long) (buf[pos + 1] & 0xFF) << 8) |
               ((long) (buf[pos + 2] & 0xFF) << 16) |
               ((long) (buf[pos + 3] & 0xFF) << 24) |
               ((long) (buf[pos + 4] & 0xFF) << 32) |
               ((long) (buf[pos + 5] & 0xFF) << 40) |
               ((long) (buf[pos + 6] & 0xFF) << 48) |
               ((long) (buf[pos + 7] & 0xFF) << 56);
    }

    private static List<MbrType> readMBR(String blkDisk) {
        try (RandomAccessFile disk = new RandomAccessFile(blkDisk, "r")) {
            byte[] mbr = new byte[512];
            disk.seek(0);
            disk.readFully(mbr);

            List<MbrType> list = new ArrayList<>();

            int partNum = 1;
            for (int i = 0; i < 4; i++) {
                int offset = 446 + i * 16;
                int type = mbr[offset + 4] & 0xFF;
                long startLBA = u32(mbr, offset + 8);

                if (type == 0) continue;


                list.add(MbrType.fromValue(type));

                boolean isExtended = (type == 0x05 || type == 0x0F);
                if (isExtended) {
                    int logicalNumber = partNum + 1;
                    List<MbrType> extList = parseExtended(disk, startLBA, logicalNumber);
                    list.addAll(extList);
                }

                partNum++;
            }
            return list;
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

    private static List<MbrType> parseExtended(RandomAccessFile disk, long ebrStart, int logicalNumber) throws Exception {
        long extendedBase = ebrStart;

        List<MbrType> list = new ArrayList<>();
        while (true) {
            disk.seek(ebrStart * 512);
            byte[] ebr = new byte[512];
            disk.readFully(ebr);

            if ((ebr[510] & 0xFF) != 0x55 || (ebr[511] & 0xFF) != 0xAA)
                break;

            int type = ebr[446 + 4] & 0xFF;

            if (type != 0) {
                MbrType mbrType = MbrType.fromValue(type);
                list.add(mbrType);
                logicalNumber++;
            }

            int nextType = ebr[462 + 4] & 0xFF;
            long nextStart = u32(ebr, 462 + 8);

            if (nextType == 0 || nextStart == 0)
                break;

            ebrStart = extendedBase + nextStart;
        }
        return list;
    }

    private static GptType readGPT(RandomAccessFile disk, int partitionNumber) throws Exception {
        byte[] header = new byte[512];
        disk.seek(512);   // LBA1
        disk.readFully(header);

        String signature = new String(header, 0, 8, StandardCharsets.US_ASCII);
        if (!"EFI PART".equals(signature)) {
            throw new RuntimeException("Invalid GPT header");
        }

        long numEntries = u32(header, 0x50);
        long entrySize = u32(header, 0x54);
        long firstLBA = u64(header, 0x48);

        long total = numEntries * entrySize;
        byte[] entries = new byte[(int) total];

        disk.seek(firstLBA * 512);
        disk.readFully(entries);

        List<GptType> list = new ArrayList<>();
        for (int i = 0; i < numEntries; i++) {
            int offset = (int) (i * entrySize);

            byte[] typeGUID = Arrays.copyOfRange(entries, offset, offset + 16);

            boolean empty = true;
            for (byte b : typeGUID)
                if (b != 0) {
                    empty = false;
                    break;
                }

            if (empty) continue;
            GptType gptType = GptType.fromGUID(typeGUID);
            list.add(gptType);
        }

        return list.get(partitionNumber - 1);
    }

    private static boolean isExtended(String blkDisk, int partitionNumber) {
        try (RandomAccessFile disk = new RandomAccessFile(blkDisk, "r")) {
            byte[] mbr = new byte[512];
            disk.seek(0);
            disk.readFully(mbr);

            for (int i = 0; i < 4; i++) {
                if (i != partitionNumber - 1) continue;
                int offset = 446 + i * 16;
                int type = mbr[offset + 4] & 0xFF;
                if (type == 0) continue;
                return (type == 0x05 || type == 0x0F);
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long getExtendedPartitionSize(String blkDisk, long sectorSize) {
        try (RandomAccessFile disk = new RandomAccessFile(blkDisk, "r")) {
            byte[] mbr = new byte[512];
            disk.seek(0);
            disk.readFully(mbr);

            for (int i = 0; i < 4; i++) {
                int offset = 446 + i * 16;
                int type = mbr[offset + 4] & 0xFF;
                if (type == 0) continue;
                long size = u32(mbr, offset + 12);
                boolean isExtended = (type == 0x05 || type == 0x0F);
                if (isExtended) {
                    return size * sectorSize;
                }
            }
            return 0;
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }
}
