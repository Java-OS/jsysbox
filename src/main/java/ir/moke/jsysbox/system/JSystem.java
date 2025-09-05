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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
            stream.filter(item -> !item.toFile().isDirectory()).forEach(item -> {
                String key = item.toString().substring("/proc/sys/".length()).replace("/", ".").trim();
                try (FileReader fileReader = new FileReader(item.toFile())) {
                    StringBuilder value = new StringBuilder();
                    int code;
                    while ((code = fileReader.read()) != -1) {
                        value.append((char) code);
                    }
                    items.put(key, value.toString().trim());
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
            Function<String[], ModInfo> modInfoFunction = item -> new ModInfo(item[0], Long.parseLong(item[1]), Integer.parseInt(item[2]), item[3].replaceAll(" Live.*", "").replaceAll(",$", ""));

            return Files.readAllLines(Path.of("/proc/modules")).stream().map(item -> item.split("\\s+", 4)).map(modInfoFunction).toList();
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
            int cpuFamily = lines.stream().filter(item -> item.startsWith("cpu family")).map(item -> Integer.parseInt(item.split(":")[1].trim())).findFirst().get();
            String modelName = lines.stream().filter(item -> item.startsWith("model name")).map(item -> item.split(":")[1]).findFirst().get();
            int cpuCores = lines.stream().filter(item -> item.startsWith("cpu cores")).map(item -> Integer.parseInt(item.split(":")[1].trim())).findFirst().get();
            return new CpuInfo(processors, vendorId, cpuFamily, modelName, cpuCores);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static MemoryInfo memoryInfo() {
        try {
            List<String> lines = Files.readAllLines(Path.of("/proc/meminfo"));
            long total = lines.stream().filter(item -> item.startsWith("MemTotal")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long free = lines.stream().filter(item -> item.startsWith("MemFree")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long available = lines.stream().filter(item -> item.startsWith("MemAvailable")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long buffers = lines.stream().filter(item -> item.startsWith("Buffers")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long cached = lines.stream().filter(item -> item.startsWith("Cached")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long swapCached = lines.stream().filter(item -> item.startsWith("SwapCached")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long active = lines.stream().filter(item -> item.startsWith("Active")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long inactive = lines.stream().filter(item -> item.startsWith("Inactive")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long swapTotal = lines.stream().filter(item -> item.startsWith("SwapTotal")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long swapFree = lines.stream().filter(item -> item.startsWith("SwapFree")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
            long shmem = lines.stream().filter(item -> item.startsWith("Shmem")).map(item -> Long.parseLong(item.split("\\s+")[1].trim())).findFirst().get();
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
                String core = line.split("\\s+")[0].trim();
                long user = Long.parseLong(line.split("\\s+")[1].trim());
                long nice = Long.parseLong(line.split("\\s+")[2].trim());
                long system = Long.parseLong(line.split("\\s+")[3].trim());
                long idle = Long.parseLong(line.split("\\s+")[4].trim());
                long ioWait = Long.parseLong(line.split("\\s+")[5].trim());
                long irq = Long.parseLong(line.split("\\s+")[6].trim());
                long softIrq = Long.parseLong(line.split("\\s+")[7].trim());
                long steal = Long.parseLong(line.split("\\s+")[8].trim());
                long guest = Long.parseLong(line.split("\\s+")[9].trim());
                long guestNice = Long.parseLong(line.split("\\s+")[10].trim());
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
            double fiveMinute = Double.parseDouble(content.split("\\s+")[1]);
            double fifteenMinute = Double.parseDouble(content.split("\\s+")[2]);
            return new LoadAverage(oneMinute, fiveMinute, fifteenMinute);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static String getCommandFromPid(Integer pid) {
        try {
            return Files.readString(Path.of("/proc").resolve(String.valueOf(pid)).resolve("comm")).trim();
        } catch (IOException e) {
            return null;
        }
    }

    public static String getKernelVersion() {
        try {
            return Files.readString(Path.of("/proc/sys/kernel/osrelease")).trim();
        } catch (IOException e) {
            throw new RuntimeException("Cannot detect kernel version", e);
        }
    }

    private native static void setUlimit(int limitId, int soft, int hard);

    private native static void setUlimitOnPID(int pid, int limitId, int soft, int hard);

    private native static int getUlimit(int limitId, boolean hard);

    public static int getUlimit(RLimit limit, boolean hard) {
        return getUlimit(limit.getCode(), hard);
    }

    public static List<Ulimit> getAllUlimits() {
        List<Ulimit> limits = new ArrayList<>();
        for (RLimit rLimit : RLimit.values()) {
            Ulimit ulimit = new Ulimit(rLimit, getUlimit(rLimit, false), getUlimit(rLimit, true));
            limits.add(ulimit);
        }
        return limits;
    }

    public static void setUlimit(Ulimit ulimit) {
        setUlimit(ulimit.limit(), ulimit.soft(), ulimit.hard());
    }

    public static void setUlimit(RLimit limit, Integer soft, Integer hard) {
        if (soft == null && hard == null) throw new JSysboxException("Both soft and hard value is null");
        int oldSoftValue = getUlimit(limit, soft != null);
        int oldHardValue = getUlimit(limit, hard != null);

        setUlimit(limit.getCode(), soft == null ? oldSoftValue : soft, hard == null ? oldHardValue : hard);
    }

    public static void setUlimitOnPID(int pid, RLimit rLimit, int soft, int hard) {
        setUlimitOnPID(pid, rLimit.getCode(), soft, hard);
    }

    public static List<Ulimit> getAllUlimits(int pid) {
        List<Ulimit> limits = new ArrayList<>();
        try {
            Path limitsPath = Path.of("/proc/" + pid + "/limits");
            List<String> lines = Files.readAllLines(limitsPath).stream().skip(1).toList();
            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");

                String name = line.substring(0, 25).trim();
                RLimit rLimit = RLimit.fromString(name);
                int soft = parseLimit(parts[3]);
                int hard = parseLimit(parts[4]);

                limits.add(new Ulimit(rLimit, soft, hard));
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return limits;
    }

    private static int parseLimit(String value) {
        if ("unlimited".equals(value)) return -1;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean insideContainer() {
        try {
            boolean dockerEnvExists = Files.exists(Paths.get("/.dockerenv"));
            boolean podmanEnvExists = Files.exists(Paths.get("/run/.containerenv"));
            boolean containersMountPointExists = Files.readAllLines(Paths.get("/proc/1/mountinfo"))
                    .stream()
                    .anyMatch(item -> item.contains("containers"));

            if ((dockerEnvExists || podmanEnvExists) && containersMountPointExists) return true;
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return false;
    }

    public static Set<ThreadInfo> threads(long pid) {
        try (Stream<Path> stream = Files.list(Paths.get("/proc/%s/task/".formatted(pid)))) {
            return stream.map(item -> {
                try {
                    String tid = item.toFile().getName();
                    String tName = Files.readString(item.resolve("comm")).trim();
                    return new ThreadInfo(pid, Long.parseLong(tid), tName);
                } catch (IOException e) {
                    throw new JSysboxException(e);
                }
            }).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static long pid() {
        return ProcessHandle.current().pid();
    }
}
