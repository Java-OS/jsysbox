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
1. jsystem 
    * system [reboot, shutdown]
    * FileSystem [mount, unmount]
    * Environment [set, unset, get] 
2. jnetwork
   * ethernet [list, set ip address]
   * routing [list, update, delete, default gateway]
   * ping 
