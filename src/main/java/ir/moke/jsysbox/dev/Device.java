package ir.moke.jsysbox.dev;

import java.nio.file.Path;

public record Device(String vendor,
                     String product,
                     String device,
                     String modAlias,
                     String module,
                     String type,
                     Path sys) {
}
