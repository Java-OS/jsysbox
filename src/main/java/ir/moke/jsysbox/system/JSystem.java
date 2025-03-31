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

import com.sun.management.OperatingSystemMXBean;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;

import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class JSystem {
    private static final Path SYSCTL_BASE_PATH = Path.of("/proc/sys");
    private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    static {
        JniNativeLoader.load("jsystem");
    }

    public native static void reboot();

    public native static void shutdown();

    public native static boolean chroot(String target);

    public native static boolean setEnv(String key, String value);

    public native static boolean unSetEnv(String key);

    public native static String getEnv(String key);

    public native static String getHostname();

    public native static void setHostname(String hostname) throws JSysboxException;

    public native static void kill(long pid, long signal);

    public static Map<String, String> sysctl() {
        Map<String, String> items = new HashMap<>();
        try (Stream<Path> stream = Files.walk(SYSCTL_BASE_PATH)) {
            stream.filter(item -> !item.toFile().isDirectory())
                    .forEach(item -> {
                        String key = item.toString().substring("/proc/sys/".length()).replace("/", ".");
                        try (FileReader fileReader = new FileReader(item.toFile())) {
                            StringBuilder value = new StringBuilder();
                            int code;
                            while ((code = fileReader.read()) != -1) {
                                value.append((char) code);
                            }
                            items.put(key, value.toString());
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
            throw new JSysboxException(e);
        }
    }

    /**
     * load kernel parameter
     *
     * @param name       kernel module name
     * @param parameters module parameters should be passed as ["key1=value1","key2=value2"]
     */
    public native static void insmod(String name, String[] parameters);

    public native static void rmmod(String name);

    public native static Map<String, String> modinfo(String name);

    public static CpuInfo cpuInfo() {
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/cpuinfo"));
            long processors = lines.stream().filter(item -> item.startsWith("processor")).count();
            String vendorId = lines.stream().filter(item -> item.startsWith("vendor_id")).map(item -> item.split(":")[1]).findFirst().get();
            int cpuFamily = lines.stream().filter(item -> item.startsWith("cpu family")).map(item -> Integer.parseInt(item.split(":")[1])).findFirst().get();
            String modelName = lines.stream().filter(item -> item.startsWith("model name")).map(item -> item.split(":")[1]).findFirst().get();
            int cpuCores = lines.stream().filter(item -> item.startsWith("cpu cores")).map(item -> Integer.parseInt(item.split(":")[1])).findFirst().get();
            return new CpuInfo(processors, vendorId, cpuFamily, modelName, cpuCores);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static MemoryInfo memoryInfo() {
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/meminfo"));
            long total = lines.stream().filter(item -> item.startsWith("MemTotal")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long free = lines.stream().filter(item -> item.startsWith("MemFree")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long available = lines.stream().filter(item -> item.startsWith("MemAvailable")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long buffers = lines.stream().filter(item -> item.startsWith("Buffers")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long cached = lines.stream().filter(item -> item.startsWith("Cached")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long swapCached = lines.stream().filter(item -> item.startsWith("SwapCached")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long active = lines.stream().filter(item -> item.startsWith("Active")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long inactive = lines.stream().filter(item -> item.startsWith("Inactive")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long swapTotal = lines.stream().filter(item -> item.startsWith("SwapTotal")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long swapFree = lines.stream().filter(item -> item.startsWith("SwapFree")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            long shmem = lines.stream().filter(item -> item.startsWith("Shmem")).map(item -> Long.parseLong(item.split("\\s+")[1])).findFirst().get();
            return new MemoryInfo(total, free, available, buffers, cached, swapCached, active, inactive, swapTotal, swapFree, shmem);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static List<CpuStat> cpuStats() {
        try {
            List<CpuStat> cpuStatList = new ArrayList<>();
            List<String> lines = Files.readAllLines(Path.of("/proc/stat"));
            List<String> cpuLines = lines.stream().filter(item -> item.startsWith("cpu")).toList();
            for (String line : cpuLines) {
                String core = line.split("\\s+")[0];
                long user = Long.parseLong(line.split("\\s+")[1]);
                long nice = Long.parseLong(line.split("\\s+")[2]);
                long system = Long.parseLong(line.split("\\s+")[3]);
                long idle = Long.parseLong(line.split("\\s+")[4]);
                long ioWait = Long.parseLong(line.split("\\s+")[5]);
                long irq = Long.parseLong(line.split("\\s+")[6]);
                long softIrq = Long.parseLong(line.split("\\s+")[7]);
                long steal = Long.parseLong(line.split("\\s+")[8]);
                long guest = Long.parseLong(line.split("\\s+")[9]);
                long guestNice = Long.parseLong(line.split("\\s+")[10]);
                CpuStat cpuStat = new CpuStat(core, user, nice, system, idle, ioWait, irq, softIrq, steal, guest, guestNice);
                cpuStatList.add(cpuStat);
            }
            return cpuStatList;
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static double cpuLoad() {
        return osBean.getCpuLoad();
    }

    public static double jvmCpuLoad() {
        return osBean.getProcessCpuLoad();
    }

    public static LoadAverage loadAverage() {
        try {
            String content = Files.readString(Path.of("/proc/loadavg"));
            double oneMinute = Double.parseDouble(content.split("\\s+")[0]);
            double fiveMinute = Double.parseDouble(content.split("\\s+")[0]);
            double fifteenMinute = Double.parseDouble(content.split("\\s+")[0]);
            return new LoadAverage(oneMinute, fiveMinute, fifteenMinute);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }
}
