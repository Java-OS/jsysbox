/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.moke.jsysbox.system;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JSystem {

    static {
        JniNativeLoader.load("jsystem");
    }

    public native static void reboot();

    public native static void shutdown();

    public native static boolean mount(String src, String dst, String type, int flags, String options);

    public native static boolean umount(String src);

    public native static boolean chroot(String target);

    public native static boolean setEnv(String key, String value);

    public native static boolean unSetEnv(String key);

    public native static String getEnv(String key);

    public native static void setHostname(String hostname) throws JSysboxException;

    public native static String getHostname();

    public native static HDDPartition getFilesystemStatistics(String mountPoint);

    /*
     * Do not activate this methods . Too buggy
     * */
//    private native static String[] envList();
//
//    public static List<String> environments() {
//        String[] envList = envList();
//        return Arrays.stream(envList).filter(Objects::nonNull)
//                .filter(item -> !item.isEmpty())
//                .toList();
//    }

    public static List<String> mounts() {
        try {
            return Files.readAllLines(Paths.get("/proc/mounts"));
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean isMount(String mountpoint) {
        List<String> mounts = JSystem.mounts();
        if (mounts == null) return false;
        return mounts.stream().anyMatch(item -> item.contains(mountpoint));
    }

    public static List<HDDPartition> partitions() {
        List<HDDPartition> partitions = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/partitions")).stream().skip(2).toList();
            for (String line : lines) {
                String[] split = line.split("\\s+");
                String blockDevice = split[4];
                if (!isScsiDeviceType(blockDevice)) {
                    String mountPoint = mountPoint(blockDevice);
                    HDDPartition partition;
                    if (mountPoint == null) {
                        partition = new HDDPartition("/dev/" + blockDevice, null, Long.parseLong(split[3]), null);
                    } else {
                        partition = getFilesystemStatistics(mountPoint);
                    }
                    partitions.add(partition);
                }
            }
        } catch (IOException ignore) {
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

    public static String getPartitionByUUID(String blkPath) {
        try {
            List<HDDPartition> partitions = JSystem.partitions();
            for (HDDPartition hddPartition : partitions) {
                try (Stream<Path> listStream = Files.list(Path.of("/dev/disk/by-uuid/"))) {
                    List<Path> list = listStream.toList();
                    for (Path path : list) {
                        if (Path.of(hddPartition.partition()).toRealPath().equals(path.toRealPath())) {
                            return path.toString();
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    public static String getPartitionByLabel(String blkPath) {
        try {
            List<HDDPartition> partitions = JSystem.partitions();
            for (HDDPartition hddPartition : partitions) {
                try (Stream<Path> listStream = Files.list(Path.of("/dev/disk/by-label/"))) {
                    List<Path> list = listStream.toList();
                    for (Path path : list) {
                        if (Path.of(hddPartition.partition()).toRealPath().equals(path.toRealPath())) {
                            return path.toString();
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}
