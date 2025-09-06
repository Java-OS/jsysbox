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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        String libAbsolutePath = LIB_PATH.resolve(name).toAbsolutePath().toString();
        try (InputStream resource = JniNativeLoader.class.getResourceAsStream(libAbsolutePath)) {
            if (resource != null) {
                byte[] bytes = resource.readAllBytes();
                String currentHash = md5(bytes);
                Path targetLibFilePath = TEMP_DIR_PATH.resolve("%s-%s".formatted(name, getVersion()));
                if (Files.exists(targetLibFilePath)) {
                    byte[] oldBytes = Files.readAllBytes(targetLibFilePath);
                    String oldHash = md5(oldBytes);
                    if (!currentHash.equals(oldHash)) {
                        copySharedObject(targetLibFilePath, bytes);
                    }
                } else {
                    copySharedObject(targetLibFilePath, bytes);
                }
                System.load(targetLibFilePath.toString());
            } else {
                logger.error("Resource is null");
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    private static void copySharedObject(Path path, byte[] bytes) throws IOException {
        Files.write(path, bytes);
    }

    private static String md5(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(bytes);
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new JSysboxException(e);
        }
    }

    private static String getVersion() {
        Package pkg = JniNativeLoader.class.getPackage();
        return pkg.getImplementationVersion();
    }
}
