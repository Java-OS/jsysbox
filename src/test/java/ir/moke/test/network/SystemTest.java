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

package ir.moke.test.network;

import ir.moke.jsysbox.hdd.HDDPartition;
import ir.moke.jsysbox.hdd.JPartition;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemTest {

    @Test
    @Order(0)
    public void checkPartitions() {
        List<HDDPartition> partitions = JPartition.partitions();
        Assertions.assertNotNull(partitions);
    }

    @Test
    @Order(0)
    public void checkFileSystemStatistics() {
        HDDPartition filesystemStatistics = JPartition.getFilesystemStatistics("/data1");
        Assertions.assertNotNull(filesystemStatistics);
    }
}
