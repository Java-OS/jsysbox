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

import java.net.InetAddress;
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

        String ip = "192.168.0.12";
        String cidr = "24";

        try {
            // Convert the IP address to an integer
            int ipInt = ipToInt(ip);
            System.out.println(ipInt);

            // Calculate the network address
            int networkInt = ipInt & (0xFFFFFFFF << (32 - Integer.parseInt(cidr)));

            // Convert the network address back to a string
            String networkAddress = intToIp(networkInt);

            // Print the network address
            System.out.println("Network Address: " + networkAddress);
        } catch (Exception e) {
            System.err.println("Error: Invalid IP address or CIDR notation.");
            e.printStackTrace();
        }

    }

    private static int ipToInt(String ip) {
        String[] parts = ip.split("\\.");
        return (Integer.parseInt(parts[0]) <<  24) +
                (Integer.parseInt(parts[1]) <<  16) +
                (Integer.parseInt(parts[2]) <<  8) +
                Integer.parseInt(parts[3]);
    }

    private static String intToIp(int ipInt) {
        return ((ipInt >>  24) &  0xFF) + "." +
                ((ipInt >>  16) &  0xFF) + "." +
                ((ipInt >>  8) &  0xFF) + "." +
                (ipInt &  0xFF);
    }
}
