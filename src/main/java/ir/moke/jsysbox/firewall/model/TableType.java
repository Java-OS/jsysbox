package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TableType {
    IPv4("ip"),
    IPv6("ip6"),
    INET("inet"),
    ARP("arp"),
    BRIDGE("bridge"),
    NETDEV("netdev");

    private final String value;

    TableType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
