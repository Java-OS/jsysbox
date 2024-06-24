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

import ir.moke.jsysbox.disk.FilesystemType;
import ir.moke.jsysbox.disk.JDiskManager;
import ir.moke.jsysbox.disk.PartitionTable;

public class MainClass {
    /*
     * Create disk size 100M :
     * dd if=/dev/urandom of=/tmp/test-disk.img bs=1M count=100
     * */
    private static final String DISK_BLK = "/tmp/test-disk.img";

    public static void main(String[] args) {

        System.out.println("Format partition table " + PartitionTable.GPT);
        JDiskManager.initializePartitionTable(DISK_BLK, PartitionTable.GPT);

        //Note first partition started from 2048
        // Primary Partition blk(1)
        long p1_sector_size = JDiskManager.calculatePartitionSectorSize(20);
        long start = 2048;
        long end = start + p1_sector_size;
        JDiskManager.createPartition(DISK_BLK, start, end, FilesystemType.EXT3, true);
        System.out.println("Primary partition 1 successfully created");

        // Primary Partition blk(2)
        long p2_sector_size = JDiskManager.calculatePartitionSectorSize(30);
        start = end + 1;
        end = start + p2_sector_size;
        JDiskManager.createPartition(DISK_BLK, start, end, FilesystemType.NTFS, true);
        System.out.println("Primary partition 2 successfully created");

        // Extended Partition blk(3)
        long p3_sector_size = JDiskManager.calculatePartitionSectorSize(30);
        start = end + 1;
        end = start + p3_sector_size;
        JDiskManager.createPartition(DISK_BLK, start, end, FilesystemType.NTFS, true);
        System.out.println("Extended partition 3 successfully created");

        // Primary Partition blk(4)
        long p4_sector_size = JDiskManager.calculatePartitionSectorSize(30);
        start = end + 1;
        end = start + p4_sector_size;
        JDiskManager.createPartition(DISK_BLK, start, end, FilesystemType.NTFS, true);
        System.out.println("Primary partition 4 successfully created");
    }
}