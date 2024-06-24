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
import java.util.Objects;
import java.util.stream.Stream;

public class JDiskManager {
    static {
        JniNativeLoader.load("jdisk_manager");
    }

    public native static boolean mount(String src, String dst, String type, int flags, String options);

    public native static boolean umount(String src);

    private native static String[] getDisks();

    /**
     * @param blk hard drive block device
     *            for example :
     *            /dev/sda
     *            /dev/sdb
     *            NOTE: WITHOUT PARTITION NUMBER SIGNATURE
     * @return array of {@link PartitionInformation}
     */
    public native static PartitionInformation[] getPartitionInformation(String blk);

    public native static void swapOn(String blk) throws JSysboxException;

    public native static void swapOff(String blk) throws JSysboxException;

    public native static PartitionTable partitionTableType(String blk);

    public native static void initializePartitionTable(String blk, PartitionTable partitionTable);

    public native static void createPartition(String blk, long start, long end, FilesystemType filesystemType, boolean isPrimary);

    public native static void deletePartition(String blk, int partitionNumber);

    public static List<String> listHardDrives() {
        Path path = Path.of("/sys/block");
        try (Stream<Path> stream = Files.list(path)) {
            return stream.map(item -> "/dev/" + item.toString()).toList();
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static List<String> mounts() {
        try {
            return Files.readAllLines(Paths.get("/proc/mounts"));
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean isMount(String uuid) {
        List<PartitionInformation> partitions = partitions();
        return partitions.stream().anyMatch(item -> item.uuid.equals(uuid));
    }

    public static boolean isMountByMountPoint(String mountPoint) {
        List<String> mounts = mounts();
        if (mounts == null) return false;
        for (String line : mounts) {
            if (mountPoint.equals(line.split("\\s+")[1])) return true;
        }
        return false;
    }

    public static List<PartitionInformation> partitions() {
        List<PartitionInformation> list = new ArrayList<>();
        List<String> disks = Arrays.stream(getDisks()).filter(item -> !item.contains("sr")).toList();
        for (String disk : disks) {
            try {
                PartitionInformation[] partitionInformation = getPartitionInformation(disk);
                if (partitionInformation != null) list.addAll(Arrays.asList(partitionInformation));
            } catch (JSysboxException ignore) {
            }
        }
        return list;
    }

    public static boolean isScsiDeviceType(String blk) {
        return Files.exists(Path.of("/sys/block/" + blk + "/device/type"));
    }

    public static String mountPoint(String blk) {
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/mounts"));
            for (String line : lines) {
                String srcBlk = line.split("\\s+")[0];
                String mountPoint = line.split("\\s+")[1];
                Path path = Path.of(srcBlk);
                if (Files.isSymbolicLink(path) && path.toRealPath().toString().endsWith(blk)) {
                    return mountPoint;
                } else if (srcBlk.endsWith(blk)) {
                    return mountPoint;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static PartitionInformation getPartitionByUUID(String uuid) {
        try (Stream<Path> listStream = Files.list(Path.of("/dev/disk/by-uuid/"))) {
            List<Path> list = listStream.toList();
            for (Path path : list) {
                if (Path.of(uuid).equals(path.toRealPath())) {
                    Path realPath = getRealPathOfDevice(path);
                    return getPartitionInformation(realPath.toString())[0];
                }
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
        return null;
    }

    public static PartitionInformation getPartitionByLabel(String label) {
        try (Stream<Path> listStream = Files.list(Path.of("/dev/disk/by-label/"))) {
            List<Path> list = listStream.toList();
            for (Path path : list) {
                if (Path.of(label).equals(path.toRealPath())) {
                    Path realPath = getRealPathOfDevice(path);
                    return getPartitionInformation(realPath.toString())[0];
                }
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
        return null;
    }

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

    public static boolean isSwapActivated(String blk) {
        try {
            return Files.readAllLines(Path.of("/proc/swaps"))
                    .stream()
                    .skip(1)
                    .map(item -> item.split("\\s+")[0])
                    .anyMatch(item -> item.equals(blk));
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public static boolean isCompactDisk(String blk) {
        return getCompactDisks().stream().anyMatch(item -> item.drive_name().equals(blk));
    }

    public static List<Disk> getDiskInformation() {
        List<Disk> disks = new ArrayList<>();
        try {
            String[] blkDisks = getDisks();
            for (String blkPath : blkDisks) {
                String blkName = blkPath.substring(blkPath.lastIndexOf("/") + 1);
                Path vendorPath = Path.of("/sys/block/%s/device/vendor".formatted(blkName));
                Path modelPath = Path.of("/sys/block/%s/device/model".formatted(blkName));
                Path sizePath = Path.of("/sys/block/%s/size".formatted(blkName));

                String vendor = Files.exists(vendorPath) ? Files.readString(vendorPath).trim() : null;
                String model = Files.exists(modelPath) ? Files.readString(modelPath).trim() : null;
                Long sectorSize = Files.exists(sizePath) ? Long.parseLong(Files.readString(sizePath).trim()) : null;
                boolean digit = Character.isDigit(blkName.charAt(blkName.length() - 1));
                if (digit) continue;
                PartitionInformation[] partitionInformations = getPartitionInformation(blkPath);

                Long sizeInBytes = sectorSize != null ? sectorSize * 512 : null;

                PartitionTable partitionTable = partitionTableType(blkPath);
                Disk disk = new Disk(blkPath, vendor, model, sizeInBytes, sectorSize, partitionTable, partitionInformations != null ? partitionInformations.length : null);
                disks.add(disk);
            }
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
        return disks;
    }

    public static Disk getDiskInformation(String blk) {
        return getDiskInformation().stream().filter(item -> Objects.equals(item.blk(), blk)).findFirst().orElse(null);
    }

    /**
     * each sector is 512 byte
     * 1MB = (1 * 1024 * 1024) / 512
     *
     * @param size size of partition in MB
     * @return size of sectors
     */
    public static long calculatePartitionSectorSize(long size) {
        return (size * 1024 * 1024) / 512;
    }
}
