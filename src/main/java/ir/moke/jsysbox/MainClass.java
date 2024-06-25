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

import ir.moke.jsysbox.disk.JDiskManager;
import ir.moke.jsysbox.disk.PartitionInformation;
import ir.moke.jsysbox.disk.PartitionTable;
import ir.moke.jsysbox.disk.PartitionType;
import ir.moke.jsysbox.system.JSystem;
import ir.moke.jsysbox.system.ModInfo;

import java.util.List;
import java.util.Map;

public class MainClass {
    public static void main(String[] args) {
        System.out.println("#### Partition System ####");
        JDiskManager.initializePartitionTable("/tmp/disk.img", PartitionTable.MSDOS);

        // create partition (1)
        long p1_sector_size = JDiskManager.calculatePartitionSectorSize(20);
        long start = 2048;
        long end = start + p1_sector_size;
        JDiskManager.createPartition("/tmp/disk.img", start, end, PartitionType.EXT3);

        // create partition (2)
        long p2_sector_size = JDiskManager.calculatePartitionSectorSize(20);
        start = end + 1;
        end = start + p2_sector_size;
        JDiskManager.createPartition("/tmp/disk.img", start, end, PartitionType.HFS);

        // create extended partition (3)
        long p3_sector_size = JDiskManager.calculatePartitionSectorSize(40);
        start = end + 1;
        end = start + p3_sector_size;
        JDiskManager.createExtendedPartition("/tmp/disk.img", start, end);

        // create logical partition (5)
        start = start + 2048;
        end = start + JDiskManager.calculatePartitionSectorSize(10);
        JDiskManager.createLogicalPartition("/tmp/disk.img", start, end, PartitionType.NTFS);

        PartitionInformation[] partitionInformation = JDiskManager.getPartitionInformation("/tmp/disk.img");
        for (PartitionInformation information : partitionInformation) {
            System.out.println(information);
        }

        System.out.println("#### Linux kernel Modules ####");
        List<ModInfo> lsmod = JSystem.lsmod();
        System.out.println("Loaded modules size: " + lsmod.size());

        // load module (xfs)
        JSystem.insmod("xfs", null);

        // module information (xfs)
        Map<String, String> xfsModuleInfo = JSystem.modinfo("xfs");
        xfsModuleInfo.forEach((k, v) -> System.out.println(k + "     " + v));

        // remove module (xfs)
        JSystem.rmmod("xfs");

        System.out.println("### Linux kernel parameters ###");
        System.out.println("Parameter size: " + JSystem.sysctl().size());

        // value of net.ipv4.ip_forward
        String value = JSystem.sysctl("net.ipv4.ip_forward");
        System.out.println(value);

        // value of net.ipv4.ip_forward
        JSystem.sysctl("net.ipv4.ip_forward", value.equals("0") ? "1" : "0");
    }
}