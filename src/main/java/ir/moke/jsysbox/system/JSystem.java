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
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class JSystem {
    private static final Path SYSCTL_BASE_PATH = Path.of("/proc/sys");

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
     * load kernel parameter
     * @param name       kernel module name
     * @param parameters module parameters should be passed as ["key1=value1","key2=value2"]
     */
    public native static void insmod(String name, String[] parameters);

    public native static void rmmod(String name);

    public native static Map<String, String> modinfo(String name);
}
