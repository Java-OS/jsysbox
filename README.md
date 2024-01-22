### Build 
```shell
sudo apt install gcc g++ make automake cmake g++-aarch64-linux-gnu 
mvn clean compile package -DskipTests
```
### Current Implementation 
1. jsystem 
    * system [reboot, shutdown]
    * FileSystem [mount, unmount]
    * Environment [set, unset, get] 
2. jnetwork
   * ethernet [list, set ip address]
   * routing [list, update, delete, default gateway]
   * ping 
