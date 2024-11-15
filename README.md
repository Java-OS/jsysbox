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

### We have two options to build this project    
#### Option 1:   
Install dependencies on your linux distro and try to execute this commands
```
Please install dependencies :
```shell
# Clone the project
git clone git@github.com:Java-OS/jsysbox.git 

# For build c/c++ source codes
sudo apt install gcc g++ make automake cmake libparted-dev libkmod-dev libnftables-dev

# Install this packages for production or test
sudo apt install libgmp10 libparted2t64 libkmod2 libzstd1 libblkid1 libcap2 libssl3t64 libdevmapper1.02.1 libgcc-s1 libgmp10 libjansson4 liblzma5 libc6 libmnl0 libnftables1 libnftnl11 libpcre2-8-0 libselinux1 libstdc++6 libudev1 libxtables12 libzstd1
  
mvn clean compile package -DskipTests
```

### Option 2    
Too simple build with docker    
```shell
# Clone the project
git clone git@github.com:Java-OS/jsysbox.git 

# execute script
cd jsysbox ; ./build_with_docker.sh
```

### Usage :     
Add dependency to your project:     
```xml
<dependency>
   <groupId>ir.moke</groupId>
   <artifactId>jsysbox</artifactId>
   <version>0.3.1</version>
</dependency>
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
