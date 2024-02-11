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

import ir.moke.jsysbox.network.JNetwork;
import ir.moke.jsysbox.system.HDDPartition;
import ir.moke.jsysbox.system.JSystem;
import ir.moke.jsysbox.system.MountOption;

import java.util.List;
import java.util.Map;

import static ir.moke.jsysbox.system.MountOption.*;

public class MainClass {

    public static void main(String[] args) throws Exception {
//        List<HDDPartition> partitions = JSystem.partitions();
//        for (HDDPartition partition : partitions) {
//            System.out.println(partition);
//        }

//        JSystem.swapOn("/dev/sdb2");
//        JSystem.swapOff("/dev/sdb2");

//        JNetwork.addHost("7.7.7.7","aaa.com");

//        Map<String, String> hosts = JNetwork.hosts();
//        hosts.forEach((k,v) -> System.out.println(k + "   " + v));

        JNetwork.removeHost("7.7.7.7");

    }
}
