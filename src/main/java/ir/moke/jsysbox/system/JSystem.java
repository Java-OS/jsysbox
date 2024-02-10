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
import java.util.Objects;
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
        return mounts.stream()
                .map(item -> item.split("\\s+")[1])
                .anyMatch(item -> item.equals(mountpoint));
    }

    public static List<HDDPartition> partitions() {
        List<HDDPartition> partitions = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/partitions")).stream().skip(2).toList();
            for (String line : lines) {
                String[] split = line.split("\\s+");
                String blockDevice = split[4];
                if (!isScsiDeviceType(blockDevice)) {
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

    public static HDDPartition getPartitionByUUID(String uuid) {
        List<HDDPartition> partitions = JSystem.partitions();
        return partitions.stream().filter(item -> item.uuid().equals(uuid)).findFirst().orElse(null);
    }

    public static HDDPartition getPartitionByLabel(String label) {
        List<HDDPartition> partitions = JSystem.partitions();
        return partitions.stream().filter(item -> Objects.equals(item.label(), label)).findFirst().orElse(null);
    }

    public static String getLvmMapperPath(String dmPath) {
        try {
            try (Stream<Path> listStream = Files.list(Path.of("/dev/mapper/"))) {
                List<Path> list = listStream.toList();
                for (Path path : list) {
                    if (Path.of(dmPath).equals(path.toRealPath())) {
                        return path.toString();
                    }
                }
            }
        } catch (Exception ignore) {
        }

        return null;
    }
}
