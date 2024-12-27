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

package ir.moke.jsysbox.network;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class JNetwork {
    private static final Path ROUTE_TABLE_PATH = Paths.get("/proc/net/route");
    private static final Path ETHERNET_STATISTICS_PATH = Paths.get("/proc/net/dev");
    private static final Path SYS_NET_PATH = Paths.get("/sys/class/net");
    private static final int DEFAULT_METRICS = 600;
    private static final int DEFAULT_TTL = 96;

    static {
        JniNativeLoader.load("jnetwork");
    }

    /**
     * @param iface     interface iface
     * @param ipAddress ip address
     * @param netmask   netmask
     */
    public native static void setIp(String iface, String ipAddress, String netmask) throws JSysboxException;

    /**
     * @param iface Interface name
     */
    public native static void ifUp(String iface) throws JSysboxException;

    /**
     * @param iface Interface name
     */
    public native static void ifDown(String iface) throws JSysboxException;

    /**
     * @param destination target ip address
     * @param netmask     netmask
     * @param gateway     gateway
     * @param iface       interface iface
     * @param metrics     route metrics
     * @param isHost      route type host or network
     * @param delete      add or delete
     */
    public native static int updateRoute(String destination, String netmask, String gateway, String iface, int metrics, boolean isHost, boolean delete) throws JSysboxException;

    public native static String[] availableEthernetList();

    public native static String[] activeEthernetList();

    private native static void initResolve();

    public static void flush(String iface) throws JSysboxException {
        setIp(iface, "0.0.0.0", "");
    }

    /**
     * @return list of available interfaces
     * @deprecated please use ethernetList method
     */
    @Deprecated
    public static List<String> availableInterfaces() {
        try (Stream<Path> list = Files.list(SYS_NET_PATH)) {
            return list.map(Path::toFile)
                    .map(File::getName)
                    .toList();
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static String getMacAddress(String iface) {
        try {
            return Files.readString(SYS_NET_PATH.resolve(iface).resolve(Path.of("address"))).trim();
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static String getIpAddress(String iface) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(iface);
            if (networkInterface == null) return null;
            return networkInterface.getInterfaceAddresses().stream().filter(item -> item.getAddress() instanceof Inet4Address).findFirst().map(item -> item.getAddress().getHostAddress()).orElse(null);
        } catch (SocketException e) {
            throw new JSysboxException(e);
        }
    }

    public static Short getCidr(String iface) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(iface);
            if (networkInterface == null) return null;
            return networkInterface.getInterfaceAddresses().stream().filter(item -> item.getAddress() instanceof Inet4Address).findFirst().map(InterfaceAddress::getNetworkPrefixLength).orElse(null);
        } catch (SocketException e) {
            throw new JSysboxException(e);
        }
    }

    public static String getNetmask(String iface) {
        Short cidr = getCidr(iface);
        if (cidr == null) return null;
        return cidrToNetmask(cidr);
    }

    /**
     * @param active if true, display only active network interfaces
     * @return list of @{@link Ethernet}
     */
    public static List<Ethernet> ethernetList(boolean active) {
        List<Ethernet> list = new ArrayList<>();
        String[] networkInterfaces = active ? activeEthernetList() : availableEthernetList();
        for (String iface : networkInterfaces) {
            String mac = getMacAddress(iface);
            String ip = getIpAddress(iface);
            Short cidr = getCidr(iface);
            String netmask = getNetmask(iface);
            EthernetStatistic ethernetStatistic = getEthernetStatistic(iface);
            Ethernet ethernet = new Ethernet(iface, mac, ip, netmask, cidr, ethernetStatistic, ethernetIsUp(iface));
            list.add(ethernet);
        }
        return list;
    }

    private static EthernetStatistic getEthernetStatistic(String iface) {
        try {
            List<String> allLines = Files.readAllLines(ETHERNET_STATISTICS_PATH);
            for (String line : allLines) {
                String[] s = line.trim().split("\\s+");
                if (s[0].contains(iface)) {
                    long rx_bytes = Long.parseLong(s[1]);
                    long rx_pkts = Long.parseLong(s[2]);
                    long rx_errors = Long.parseLong(s[3]);
                    long tx_bytes = Long.parseLong(s[9]);
                    long tx_pkts = Long.parseLong(s[10]);
                    long tx_errors = Long.parseLong(s[11]);
                    return new EthernetStatistic(rx_pkts, rx_bytes, rx_errors, tx_pkts, tx_bytes, tx_errors);
                }
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return null;
    }

    /**
     * @param iface interface name
     * @return {@link Ethernet}
     */
    public static Ethernet ethernet(String iface) {
        return ethernetList(false).stream().filter(item -> item.iface().equals(iface)).findFirst().orElse(null);
    }

    public static boolean isEthernetExists(String iface) {
        return ethernetList(false).stream().anyMatch(item -> item.iface().equals(iface));
    }

    public static boolean ethernetIsUp(String iface) {
        return Arrays.asList(activeEthernetList()).contains(iface);
    }

    public static void addHostToRoute(String destination, String gateway, String iface, Integer metrics) throws JSysboxException {
        updateRoute(destination, null, gateway, iface, metrics, true, false);
    }

    public static void addNetworkToRoute(String destination, String netmask, String gateway, String iface, Integer metrics) throws JSysboxException {
        updateRoute(destination, netmask, gateway, iface, metrics, false, false);
    }

    /**
     * @param gateway ip address
     */
    public static void setDefaultGateway(String gateway) throws JSysboxException {
        updateRoute("0.0.0.0", "0.0.0.0", gateway, null, 0, false, false);
    }

    public static void deleteRoute(int id) throws JSysboxException {
        Optional<Route> optionalRoute = route().stream()
                .filter(item -> item.getId() == id)
                .findFirst();
        if (optionalRoute.isPresent()) {
            Route route = optionalRoute.get();
            updateRoute(route.getDestination(), route.getNetmask(), route.getGateway(), route.getIface(), route.getMetrics(), false, true);
        }
    }

    public static List<Route> route() {
        List<Route> routeList = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(ROUTE_TABLE_PATH);
            for (int i = 1; i < lines.size(); i++) {
                routeList.add(getRoute(lines.get(i), i));
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return routeList;
    }

    public static boolean isRouteExists(String destination, String netmask, String gateway, String iface, Integer metrics) {
        Optional<Route> optionalRoute = route()
                .stream()
                .filter(item -> item.getIface().equals((iface != null && !iface.isEmpty()) ? iface : upGatewayInterface()))
                .filter(item -> item.getDestination().equals(destination))
                .filter(item -> item.getNetmask().equals(netmask))
                .filter(item -> item.getGateway().equals((gateway != null && !gateway.isEmpty()) ? gateway : "0.0.0.0"))
                .filter(item -> item.getMetrics() == metrics)
                .findFirst();
        return optionalRoute.isPresent();
    }

    public static String upGatewayInterface() {
        return route().stream()
                .filter(item -> item.getGateway().equals("0.0.0.0"))
                .filter(item -> item.getDestination().equals("0.0.0.0"))
                .map(Route::getIface).findFirst().orElse(null);
    }

    public static void ping(String destination, String iface, Integer ttl, Integer count, Integer timeout, Integer interval) {
        if (count == null || count < 1) count = 4;
        if (timeout == null || timeout <= 1000) timeout = 1000;
        if (ttl == null || ttl <= 0) ttl = DEFAULT_TTL;
        if (interval == null || interval < 800) interval = 800;
        try {
            InetAddress addr = InetAddress.getByName(destination);
            System.out.printf("Ping %s\n", destination);
            int reachableCount = 0;
            for (int i = 0; i < count; i++) {
                Instant startTime = Instant.now();
                boolean reachable;
                if (iface != null && !iface.isEmpty()) {
                    NetworkInterface networkInterface = NetworkInterface.getByName(iface);
                    reachable = addr.isReachable(networkInterface, ttl, timeout);
                } else {
                    reachable = addr.isReachable(timeout);
                }
                if (reachable) reachableCount++;
                long diff = Duration.between(startTime, Instant.now()).toMillis();
                System.out.printf("%d: from %s time=%d ms\n", reachableCount, destination, diff);
                sleep(interval);
            }
        } catch (IOException e) {
            System.out.println("ping: " + e.getMessage());
        }
    }

    private static Route getRoute(String line, int id) {
        String[] s = line.split("\\s+");
        String iface = s[0];
        String destination = hexToIp(s[1]);
        String gateway = hexToIp(s[2]);
        String netmask = hexToIp(s[7]);
        int flags = Integer.parseInt(s[3]);
        int refcnt = Integer.parseInt(s[4]);
        int use = Integer.parseInt(s[5]);
        int metrics = Integer.parseInt(s[6]);
        int mtu = Integer.parseInt(s[8]);
        int window = Integer.parseInt(s[9]);
        int irtt = Integer.parseInt(s[10]);
        return new Route(id, destination, netmask, gateway, iface, flags, use, metrics, mtu, window, irtt, refcnt);
    }

    public static String hexToIp(String hex) {
        StringBuilder ip = new StringBuilder();
        for (int i = 0; i < hex.length(); i = i + 2) {
            ip.insert(0, Integer.valueOf(hex.substring(i, i + 2), 16) + ".");
        }
        return ip.deleteCharAt(ip.length() - 1).toString();
    }

    private static void sleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            throw new JSysboxException(e);
        }
    }

    public static String bytesToMac(byte[] bytes) {
        String[] hexadecimal = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            hexadecimal[i] = String.format("%02X", bytes[i]);
        }
        return String.join(":", hexadecimal);
    }

    public static String cidrToNetmask(int cidr) {
        int shft = 0xffffffff << (32 - cidr);
        int oct1 = ((byte) ((shft & 0xff000000) >> 24)) & 0xff;
        int oct2 = ((byte) ((shft & 0x00ff0000) >> 16)) & 0xff;
        int oct3 = ((byte) ((shft & 0x0000ff00) >> 8)) & 0xff;
        int oct4 = ((byte) (shft & 0x000000ff)) & 0xff;
        return oct1 + "." + oct2 + "." + oct3 + "." + oct4;
    }

    public static void setDnsNameServers(String... ipAddresses) throws JSysboxException {
        try {
            Path path = Path.of("/etc/resolv.conf");
            StringBuilder sb = new StringBuilder();
            Arrays.stream(ipAddresses).map(item -> "nameserver " + item + "\n").forEach(sb::append);
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new JSysboxException(e.getMessage());
        }

        initResolve();
    }

    public static ConcurrentMap<String, String> hosts() {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        try {
            List<String> lines = Files.readAllLines(Path.of("/etc/hosts")).stream().filter(item -> !item.trim().startsWith("#")).toList();
            for (String line : lines) {
                String[] split = line.split("\\s+");
                map.put(split[0], split[1]);
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return map;
    }

    public static void addHost(String ip, String hostname) throws JSysboxException {
        String line = ip + " " + hostname + "\n";
        Path path = Path.of("/etc/hosts");
        try {
            Files.write(path, line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new JSysboxException(e.getMessage());
        }
    }

    public static void removeHost(String hostname) throws JSysboxException {
        try {
            Path path = Path.of("/etc/hosts");
            StringBuilder sb = new StringBuilder();
            Files.readAllLines(path)
                    .stream()
                    .filter(item -> !item.split("\\s+")[1].equals(hostname))
                    .map(item -> item + "\n")
                    .forEach(sb::append);
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new JSysboxException(e.getMessage());
        }

    }

    public static ConcurrentMap<String, String> networks() {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        try {
            List<String> lines = Files.readAllLines(Path.of("/etc/networks")).stream().filter(item -> !item.trim().startsWith("#")).toList();
            for (String line : lines) {
                String[] split = line.split("\\s+");
                map.put(split[0], split[1]);
            }
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
        return map;
    }

    public static void addNetwork(String name, String network) throws JSysboxException {
        String line = name + " " + network + "\n";
        Path path = Path.of("/etc/networks");
        try {
            Files.write(path, line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new JSysboxException(e.getMessage());
        }
    }

    public static void removeNetwork(String name) throws JSysboxException {
        try {
            Path path = Path.of("/etc/networks");
            StringBuilder sb = new StringBuilder();
            Files.readAllLines(path)
                    .stream()
                    .filter(item -> !item.split("\\s+")[0].equals(name))
                    .map(item -> item + "\n")
                    .forEach(sb::append);
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new JSysboxException(e.getMessage());
        }
    }

    public static String calculateNetwork(String ip, int cidr) {
        int ipInt = ipToInt(ip);
        int networkInt = ipInt & (0xFFFFFFFF << (32 - cidr));
        return intToIp(networkInt);
    }

    private static int ipToInt(String ip) {
        String[] parts = ip.split("\\.");
        return (Integer.parseInt(parts[0]) << 24) +
                (Integer.parseInt(parts[1]) << 16) +
                (Integer.parseInt(parts[2]) << 8) +
                Integer.parseInt(parts[3]);
    }

    private static String intToIp(int ipInt) {
        return ((ipInt >> 24) & 0xFF) + "." +
                ((ipInt >> 16) & 0xFF) + "." +
                ((ipInt >> 8) & 0xFF) + "." +
                (ipInt & 0xFF);
    }
}
