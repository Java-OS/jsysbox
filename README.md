```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Build 
```shell
sudo apt install gcc g++ make automake cmake g++-aarch64-linux-gnu 
mvn clean compile package -DskipTests
```
### Current Implementation 
1. Networking :
      * list available interfaces
      * Set/Get interface ip address
      * Interface statistics
      * IfUp/IfDown
      * Add/Delete/List routes (specific method to set default gateway)
      * Configuration DNS resolv.conf 
2. FileSystem:
      * Mount/Umount/MountPoints Filesystems
      * List Disks
      * Compact Disk
      * Type/Initialize/Reformat/Create/Remove/Information Partition and PartitionTable
      * On/Off Swap
3. Date and Time
      * Set/Get system date and time
      * Set/Get system timezone
      * Sync system to hardware clock (like: hwclock --systohc)
      * Sync hardware to system clock (like: hwclock --hctosys)
4. Firewall (Based on nftables)
      * Table/Chain/Set/Rule
      * Save & Restore 
5. System
      * List/Install/Remove/Information Kernel Modules
      * Control Kernel Configurations (equivalent sysctl)
      * Control Hostname
      * Set/Get system environments
      * Set/Get hostname
      * Reboot
      * Shutdown
      * kill
      * chroot