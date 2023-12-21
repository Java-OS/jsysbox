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

package ir.moke.test.network;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.network.JNetwork;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NetworkTest {
    private static final String ETHERNET_NAME = "wlan0";
    private static final String IP_ADDRESS = "20.20.20.12";
    private static final String NETMASK = "255.255.255.0";
    private static final short CIDR = 24;
    private static final String GATEWAY = "20.20.20.1";

    @Test
    @Order(0)
    public void flushInterface() {
        assertDoesNotThrow(() -> JNetwork.flush(ETHERNET_NAME));
    }

    @Test
    @Order(1)
    public void checkSetIpAddress() {
        try {
            JNetwork.setIp(ETHERNET_NAME, IP_ADDRESS, NETMASK);
            String ipAddress = JNetwork.getIpAddress(ETHERNET_NAME);
            String netmask = JNetwork.getNetmask(ETHERNET_NAME);
            Short cidr = JNetwork.getCidr(ETHERNET_NAME);
            Assertions.assertEquals(ipAddress, IP_ADDRESS);
            Assertions.assertEquals(netmask, NETMASK);
            Assertions.assertEquals(cidr, CIDR);
        } catch (JSysboxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    public void checkIfDown() {
        try {
            JNetwork.ifDown(ETHERNET_NAME);
            sleep();
            String content = Files.readString(Path.of("/sys/class/net/%s/operstate".formatted(ETHERNET_NAME)));
            Assertions.assertEquals(content.trim().replaceAll("\r\n", ""), "down");
        } catch (JSysboxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    public void checkIfUp() {
        try {
            JNetwork.ifUp(ETHERNET_NAME);
            sleep();
            String content = Files.readString(Path.of("/sys/class/net/%s/operstate".formatted(ETHERNET_NAME)));
            Assertions.assertEquals(content.trim().replaceAll("\r\n", ""), "up");
        } catch (JSysboxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    public void checkListInterfaces() {
        String[] list = JNetwork.networkInterfaces();
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.length > 1);
    }


    @Test
    @Order(4)
    public void checkListInterfacesWithUpDown() {
        try {
            String ethernet = "eth0";
            JNetwork.ifUp(ethernet);
            JNetwork.setIp(ethernet, "10.10.10.1", "255.255.255.0");
            String[] before = JNetwork.networkInterfaces();
            System.out.println("List interfaces (Before)");
            for (String s : before) {
                System.out.println("> " + s);
            }
            Assertions.assertTrue(Arrays.stream(before).anyMatch(item -> item.contains(ethernet)));

            JNetwork.ifDown(ethernet);
            String[] after = JNetwork.networkInterfaces();
            System.out.println("List interfaces (After)");
            for (String s : after) {
                System.out.println("> " + s);
            }
            Assertions.assertTrue(Arrays.stream(after).noneMatch(item -> item.contains(ethernet)));
        } catch (JSysboxException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @Order(5)
    public void checkDownAfterDown() {
        try {
            String ethernet = "eth0";
            JNetwork.ifUp(ethernet);
            JNetwork.setIp(ethernet, "10.10.10.1", "255.255.255.0");

            JNetwork.ifDown(ethernet);
            Assertions.assertDoesNotThrow(() -> JNetwork.ifDown(ethernet));
        } catch (JSysboxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(6)
    public void checkCIDR() {
        String n1 = JNetwork.cidrToNetmask(32);
        String n2 = JNetwork.cidrToNetmask(31);
        String n3 = JNetwork.cidrToNetmask(30);
        String n4 = JNetwork.cidrToNetmask(29);
        String n5 = JNetwork.cidrToNetmask(28);
        String n6 = JNetwork.cidrToNetmask(27);
        String n7 = JNetwork.cidrToNetmask(26);
        String n8 = JNetwork.cidrToNetmask(25);
        String n9 = JNetwork.cidrToNetmask(24);
        Assertions.assertEquals(n1,"255.255.255.255");
        Assertions.assertEquals(n2,"255.255.255.254");
        Assertions.assertEquals(n3,"255.255.255.252");
        Assertions.assertEquals(n4,"255.255.255.248");
        Assertions.assertEquals(n5,"255.255.255.240");
        Assertions.assertEquals(n6,"255.255.255.224");
        Assertions.assertEquals(n7,"255.255.255.192");
        Assertions.assertEquals(n8,"255.255.255.128");
        Assertions.assertEquals(n9,"255.255.255.0");
    }


    private static void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
