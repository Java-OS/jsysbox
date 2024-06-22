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
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class JSystem {
    private static final Path SYSCTL_BASE_PATH = Path.of("/proc/sys");

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

    public native static String getHostname();

    public native static void setHostname(String hostname) throws JSysboxException;

    public native static HDDPartition getFilesystemStatistics(String blk);

    public native static void swapOn(String blk) throws JSysboxException;

    public native static void swapOff(String blk) throws JSysboxException;

    public native static void kill(long pid, long signal);

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

    public static Map<String, String> sysctl() {
        Map<String, String> items = new HashMap<>();
        try (Stream<Path> stream = Files.walk(SYSCTL_BASE_PATH)) {
            stream.filter(item -> !item.toFile().isDirectory())
                    .forEach(item -> {
                        String key = item.toString().substring("/proc/sys/".length()).replace("/", ".");
                        try {
                            String value = new String(Files.readAllBytes(item));
                            items.put(key, value);
                        } catch (IOException e) {
                            items.put(key, "");
                        }
                    });
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return items;
    }

    public static String sysctl(String key) {
        return sysctl().get(key);
    }

    public static void sysctl(String key, String value) {
        Path keyPath = SYSCTL_BASE_PATH.resolve(Path.of(key.replace(".", "/")));
        try {
            Files.write(keyPath, value.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static List<ModInfo> lsmod() {
        try {
            Function<String[], ModInfo> modInfoFunction = item -> new ModInfo(
                    item[0],
                    Long.parseLong(item[1]),
                    Integer.parseInt(item[2]),
                    item[3].replaceAll(" Live.*", "").replaceAll(",$", "")
            );

            return Files.readAllLines(Path.of("/proc/modules"))
                    .stream()
                    .map(item -> item.split("\\s+", 4))
                    .map(modInfoFunction)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param name       kernel module name
     * @param parameters module parameters should be passed as ["key1=value1","key2=value2"]
     */
    public native static void insmod(String name, String[] parameters);

    public native static void rmmod(String name);

    public native static Map<String, String> modinfo(String name);
}
