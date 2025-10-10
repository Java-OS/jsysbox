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

package ir.moke.jsysbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JniNativeLoader {
    private static final Logger logger = LoggerFactory.getLogger(JniNativeLoader.class);
    private static final Path LIB_PATH = Paths.get("/META-INF/native");
    private static Path TEMP_DIR_PATH = Paths.get("/tmp/jni");

    static {
        try {
            if (!Files.exists(TEMP_DIR_PATH)) {
                Files.createDirectory(TEMP_DIR_PATH);
            }
            String envPath = System.getProperty("java.tmp.path");
            if (envPath != null && !envPath.isEmpty()) TEMP_DIR_PATH = Paths.get(envPath);

        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static synchronized void load(String name) {
        String arch = System.getProperty("os.arch");
        if (arch.equals("amd64")) {
            extractLibrary("lib" + name + "_x86_64.so");
        } else {
            extractLibrary("lib" + name + "_arm64.so");
        }
    }

    private static void extractLibrary(String name) {
        Path libAbsolutePath = LIB_PATH.resolve(name).toAbsolutePath();
        try (InputStream resource = JniNativeLoader.class.getResourceAsStream(libAbsolutePath.toString())) {
            if (resource != null) {
                if (libAbsolutePath.toFile().exists()) {
                    Files.delete(libAbsolutePath);
                }
                copySharedObject(name, resource);
                System.load(TEMP_DIR_PATH.resolve(name).toString());
            } else {
                logger.error("Resource is null");
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    private static void copySharedObject(String name, InputStream resource) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(TEMP_DIR_PATH.resolve(name).toFile())) {
            resource.transferTo(outputStream);
        }
    }
}
