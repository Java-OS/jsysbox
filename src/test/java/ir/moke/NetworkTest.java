package ir.moke;

import ir.moke.jsysbox.network.Ethernet;
import ir.moke.jsysbox.network.JNetwork;
import ir.moke.jsysbox.network.Route;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NetworkTest {
    private static final Logger logger = LoggerFactory.getLogger(NetworkTest.class);

    @Test
    @Order(0)
    public void checkAvailableEthernets() {
        logger.info("Execute <checkAvailableEthernets>");
        String[] ethers = JNetwork.availableEthernetList();
        for (String eth : ethers) {
            Ethernet ethernet = JNetwork.ethernet(eth);
            System.out.println(ethernet);
        }
    }

    @Test
    @Order(1)
    public void checkEthernetIsUp() {
        logger.info("Execute <checkEthernetIsUp>");
        Assertions.assertTrue(JNetwork.ethernetIsUp("eth0"));
    }

    @Test
    @Order(2)
    public void checkEthernetUpDown() {
        logger.info("Execute <checkEthernetUpDown>");
        JNetwork.ifDown("eth0");
        Assertions.assertFalse(JNetwork.ethernetIsUp("eth0"));

        JNetwork.ifUp("eth0");
        Assertions.assertTrue(JNetwork.ethernetIsUp("eth0"));
    }

    @Test
    @Order(3)
    public void checkChangeIpAddress() {
        logger.info("Execute <checkChangeIpAddress>");
        JNetwork.setIp("eth0", "20.20.20.12", "255.255.255.0");
        Assertions.assertEquals("20.20.20.12", JNetwork.getIpAddress("eth0"));
        Assertions.assertEquals("255.255.255.0", JNetwork.getNetmask("eth0"));

        Ethernet ethernet = JNetwork.ethernet("eth0");
        Assertions.assertEquals("20.20.20.12", ethernet.ip());
        Assertions.assertEquals("255.255.255.0", ethernet.netmask());
    }

    @Test
    @Order(4)
    public void checkRouteTable() {
        logger.info("Execute <checkRouteTable>");
        List<Route> routeList = JNetwork.route();
        for (Route route : routeList) {
            String str = "%s %s %s %s".formatted(route.getId(), route.getGateway(), route.getDestination(), route.getNetmask());
            System.out.println(str);
        }
    }

    @Test
    @Order(5)
    public void checkAddHostToRouteTable() {
        logger.info("Execute <checkAddHostToRouteTable>");
        JNetwork.addHostToRoute("129.12.20.20", null, "eth0", 500);
        boolean exists = JNetwork.route().stream().anyMatch(item -> item.getDestination().equals("129.12.20.20"));
        Assertions.assertTrue(exists);
    }

    @Test
    @Order(6)
    public void checkAddNetworkToRouteTable() {
        logger.info("Execute <checkAddNetworkToRouteTable>");
        JNetwork.addNetworkToRoute("32.12.12.0", "255.255.0.0", null, "eth0", 500);
        boolean exists = JNetwork.route().stream().anyMatch(item -> item.getDestination().equals("32.12.0.0"));
        Assertions.assertTrue(exists);
    }

    @Test
    @Order(7)
    public void checkFlush() {
        logger.info("Execute <checkFlush>");
        JNetwork.flush("eth0");
        Assertions.assertNull(JNetwork.ethernet("eth0").ip());
    }
}
