### Build 
```shell
sudo apt install gcc g++ make automake cmake g++-aarch64-linux-gnu 
mvn clean compile package -DskipTests
```
### Current Implementation 
1. Networking :
      * list available interfaces
      * Set/Get interaface ip address
      * Interface statistics
      * IfUp/IfDown
      * Add/Delete/List routes (specific method to set default gateway)
2. FileSystem:
      * Mount/Umount/MountPoints Filesystems
      * Set/Get system environments
      * Set/Get hostname
3. Date and Time
      * Set/Get system time zone
      * Set/Get system date and time
      * Sync system to hardware clock (like: hwclock --systohc)
      * Sync hardware to system clock (like: hwclock --hctosys)
