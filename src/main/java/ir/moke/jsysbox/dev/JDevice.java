package ir.moke.jsysbox.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ir.moke.jsysbox.system.JSystem.getKernelVersion;

public class JDevice {
    private static final String MODULES_ALIAS_PATH = "/lib/modules/" + getKernelVersion() + "/modules.alias";
    private static final List<String> DEVICE_PATHS = List.of(
            "/sys/bus/pci/devices",
            "/sys/bus/usb/devices",
            "/sys/bus/acpi/devices",
            "/sys/devices/platform"
    );

    public static List<Device> scanDevices() {
        List<Device> devices = new ArrayList<>();
        for (String basePath : DEVICE_PATHS) {
            try (Stream<Path> dirs = Files.list(Path.of(basePath))) {
                dirs.filter(Files::isDirectory).forEach(item -> {
                    try {
                        String vendor = readSafe(item.resolve("vendor"));
                        String product = readSafe(item.resolve("product"));
                        String device = readSafe(item.resolve("device"));
                        String modAlias = readSafe(item.resolve("modalias")).trim();
                        String type = getType(item);
                        if (!modAlias.isEmpty()) {
                            String module = findModuleForModalias(modAlias);
                            devices.add(new Device(vendor, product, device, modAlias, module, type, item));
                        }
                    } catch (Exception ignored) {
                    }
                });
            } catch (IOException ignored) {
            }
        }
        return devices;
    }

    private static String getType(Path item) {
        String type;
        if (item.startsWith("/sys/bus/pci")) type = "PCI";
        else if (item.startsWith("/sys/bus/usb")) type = "USB";
        else if (item.startsWith("/sys/bus/acpi")) type = "ACPI";
        else if (item.startsWith("/sys/devices/platform")) type = "Platform";
        else type = "Unknown";
        return type;
    }


    private static String readSafe(Path path) {
        try {
            return Files.exists(path) ? Files.readString(path).trim() : "";
        } catch (IOException e) {
            return "";
        }
    }

    public static String findModuleForModalias(String modalias) throws Exception {
        File aliasFile = new File(MODULES_ALIAS_PATH);
        if (!aliasFile.exists()) {
            throw new RuntimeException("modules.alias not found: " + MODULES_ALIAS_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(aliasFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("alias ")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length != 3) continue;

                String aliasPattern = parts[1];
                String moduleName = parts[2];

                String regex = toRegexFromAlias(aliasPattern);
                Pattern pattern = Pattern.compile(regex);

                if (pattern.matcher(modalias).matches()) {
                    return moduleName;
                }
            }
        }

        return null;
    }

    private static String toRegexFromAlias(String aliasPattern) {
        StringBuilder regex = new StringBuilder("^");
        for (char c : aliasPattern.toCharArray()) {
            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append(".");
                    break;
                case '.':
                case '\\':
                case '+':
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '^':
                case '$':
                case '|':
                    regex.append("\\").append(c);
                    break;
                default:
                    regex.append(c);
            }
        }
        regex.append("$");
        return regex.toString();
    }
}