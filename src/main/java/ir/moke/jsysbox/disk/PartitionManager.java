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

public class PartitionManager {
    static {
        JniNativeLoader.load("jpartition_manager");
    }

    public native static boolean mount(String src, String dst, String type, int flags, String options);

    public native static boolean umount(String src);

    public native static String[] getDisks();

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

    public native static void createPartition(String blk, long start, long end, FilesystemType filesystemType);

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
}
