package ir.moke.jsysbox.hdd;

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

public class JPartition {
    static {
        JniNativeLoader.load("jpartition");
    }

    public native static boolean mount(String src, String dst, String type, int flags, String options);

    public native static boolean umount(String src);

    public native static HDDPartition getFilesystemStatistics(String blk);

    public native static void swapOn(String blk) throws JSysboxException;

    public native static void swapOff(String blk) throws JSysboxException;

    public native static void initPartitionTable(String blk, PartitionTable partitionTable);

    public native static void create(String blk, long start, long end, FilesystemType filesystemType);

    public native static void remove(String blk, long start, long end, FilesystemType filesystemType);

    public static List<String> mounts() {
        try {
            return Files.readAllLines(Paths.get("/proc/mounts"));
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean isMount(String uuid) {
        List<HDDPartition> partitions = partitions();
        return partitions.stream().anyMatch(item -> item.uuid().equals(uuid));
    }

    public static boolean isMountByMountPoint(String mountPoint) {
        List<String> mounts = mounts();
        if (mounts == null) return false;
        for (String line : mounts) {
            if (mountPoint.equals(line.split("\\s+")[1])) return true;
        }
        return false;
    }

    public static List<HDDPartition> partitions() {
        List<HDDPartition> partitions = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/partitions")).stream().skip(2).toList();
            for (String line : lines) {
                String[] split = line.split("\\s+");
                String blockDevice = split[4];
                if (!isScsiDeviceType(blockDevice)) { //filter only partitions
                    HDDPartition partition;
                    if (blockDevice.startsWith("dm-")) {
                        String lvmMapperPath = getLvmMapperPath("/dev/" + blockDevice);
                        partition = getFilesystemStatistics(lvmMapperPath);
                    } else {
                        partition = getFilesystemStatistics("/dev/" + blockDevice);
                    }
                    partitions.add(partition);
                }
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return partitions;
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

    public static HDDPartition getPartitionByUUID(String uuid) {
        List<HDDPartition> partitions = partitions();
        return partitions.stream().filter(item -> item.uuid().equals(uuid)).findFirst().orElse(null);
    }

    public static HDDPartition getPartitionByLabel(String label) {
        List<HDDPartition> partitions = partitions();
        return partitions.stream().filter(item -> Objects.equals(item.label(), label)).findFirst().orElse(null);
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

    public static HDDPartition getRootPartition() {
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
            return getFilesystemStatistics(rootBlk);
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
}
