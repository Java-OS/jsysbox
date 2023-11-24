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

import ir.moke.jsysbox.system.JSystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class MainClass {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        boolean changed = JSystem.chroot("/data2/Docker/Dockerfiles/Debian/testing-chroot");
        if (changed) {
            Files.list(Path.of("/")).forEach(item -> System.out.println(item.getFileName()));
        }
    }
}
