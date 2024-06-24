package ir.moke.jsysbox.device;

import ir.moke.jsysbox.JSysboxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DeviceManager {
    public static int[] deviceIdentity(String blk) {
        int[] identity = new int[2];
        try (Stream<Path> stream = Files.list(Path.of("/sys/dev/block"))) {
            Path path = stream.filter(item -> {
                        try {
                            return Files.readSymbolicLink(item).getFileName().toString().equals(blk);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .findFirst()
                    .orElse(null);
            if (path != null) {
                identity[0] = Integer.parseInt(path.toString().split(":")[0]);
                identity[1] = Integer.parseInt(path.toString().split(":")[1]);
                return identity;
            }
            return null;
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

}
