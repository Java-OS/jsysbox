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
    private native static String[] getDisks();

    /**
     * @param blkDisk hard drive block device
     *                for example:
     *                /dev/sda
     *                /dev/sdb
     *                NOTE: WITHOUT PARTITION NUMBER SIGNATURE
     * @return array of {@link PartitionInformation}
     */
    public native static PartitionInformation[] getPartitionInformation(String blkDisk);

    /**
     * Activate swap partition
     *
     * @param blkPartition swap partition block device address
     *                     example: /dev/sdb2
     * @throws JSysboxException
     */
    public native static void swapOn(String blkPartition) throws JSysboxException;

    /**
     * Deactivate swap partition
     *
     * @param blkPartition swap partition block device address
     *                     example: /dev/sdb2
     * @throws JSysboxException
     */
    public native static void swapOff(String blkPartition) throws JSysboxException;

    /**
     * Get current partition table type of disk
     *
     * @param blkDisk block device address of disk
     *                example: /dev/sda
     * @return return Type of partition table {@link PartitionInformation}
     */
    public native static PartitionTable partitionTableType(String blkDisk);

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
     * @param blkDisk       block device address of disk
     *                      example: /dev/sdx
     * @param start         Partition start sector
     *                      NOTE: First partition started from 2048
     * @param end           Partition end sector
     * @param partitionType Partition filesystem type
     * @param isPrimary     set true if you want to create primary partition on MBR partition table
     */
    private native static void createPartition(String blkDisk, long start, long end, String partitionType, boolean isPrimary);

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
     * Synchronize cached writes to persistent storage
     */
    public static native void sync() ;

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
        return partitions.stream().anyMatch(item -> item.uuid.equals(uuid));
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
        List<String> disks = Arrays.stream(getDisks()).filter(item -> !item.contains("sr")).toList();
        for (String disk : disks) {
            try {
                if (disk.contains("dm-")) disk = getLvmMapperPath(disk);
                PartitionInformation[] partitionInformation = getPartitionInformation(disk);
                if (partitionInformation != null) list.addAll(Arrays.asList(partitionInformation));
            } catch (JSysboxException ignore) {
            }
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
                    return getPartitionInformation(realPath.toString())[0];
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
                    return getPartitionInformation(realPath.toString())[0];
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
            return getPartitionInformation(rootBlk)[0];
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
     * return list of compact disks like cdrom or dvdrom
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
            int columns = lines.get(0).split("\\s+").length;
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
        try {
            String[] blkDisks = getDisks();
            for (String blkPath : blkDisks) {
                Disk disk = getDiskInformation(blkPath);
                disks.add(disk);
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
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
            String blkName = blkDisk.substring(blkDisk.lastIndexOf("/") + 1);
            Path vendorPath = Path.of("/sys/block/%s/device/vendor".formatted(blkName));
            Path modelPath = Path.of("/sys/block/%s/device/model".formatted(blkName));
            Path sizePath = Path.of("/sys/block/%s/size".formatted(blkName));

            String vendor = Files.exists(vendorPath) ? Files.readString(vendorPath).trim() : null;
            String model = Files.exists(modelPath) ? Files.readString(modelPath).trim() : null;
            Long sectorSize = Files.exists(sizePath) ? Long.parseLong(Files.readString(sizePath).trim()) : null;

            PartitionInformation[] partitionInformations = getPartitionInformation(blkDisk);

            Long sizeInBytes = sectorSize != null ? sectorSize * 512 : null;

            PartitionTable partitionTable = partitionTableType(blkDisk);
            return new Disk(blkDisk, vendor, model, sizeInBytes, sectorSize, partitionTable, partitionInformations != null ? partitionInformations.length : null);
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

    public static void createPartition(String blkDisk, long start, long end, FilesystemType filesystemType) {
        createPartition(blkDisk, start, end, filesystemType.getType(), true);
    }

    public static void createExtendedPartition(String blkDisk, long start, long end) {
        createPartition(blkDisk, start, end, null, false);
    }

    public static void createLogicalPartition(String blkDisk, long start, long end, FilesystemType filesystemType) {
        createPartition(blkDisk, start, end, filesystemType.getType(), false);
    }
}
