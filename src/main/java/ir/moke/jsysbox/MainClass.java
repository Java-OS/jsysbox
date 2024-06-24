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

import ir.moke.jsysbox.disk.PartitionInformation;
import ir.moke.jsysbox.disk.PartitionManager;

import java.util.List;

public class MainClass {

    public static void main(String[] args) throws Exception {
        List<PartitionInformation> partitions = PartitionManager.partitions();
        for (PartitionInformation partition : partitions) {
            System.out.println(partition);
        }

//        System.out.println(PartitionManager.getRootPartition());
//        HDDPartition[] filesystemStatistics = PartitionManager.getFilesystemStatistics("/tmp/test-disk.img");
//        PartitionInformation[] filesystemStatistics = PartitionManager.getPartitionInformation(args[0]);
//        for (PartitionInformation filesystemStatistic : filesystemStatistics) {
//            System.out.println(filesystemStatistic);
//        }
//        String diskPath = "/tmp/test-disk.img";
//        PartitionTable partitionTable = JFilesystem.partitionTableType(diskPath);
//        System.out.println("Partition table : " + partitionTable);
//        if (partitionTable == null) {
//            System.out.println("Initialize new partition table");
//            JFilesystem.initializePartitionTable(diskPath, PartitionTable.MSDOS);
//        }
//
//        long size = 50;
//        long lastSector = ((size * 1024 * 1024) / 512) + 2048 - 1;
//
//        JFilesystem.createPartition(diskPath, 2048, lastSector, FilesystemType.EXT3);
    }
}