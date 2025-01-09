package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

    public static TableType fromValue(String value) {
        return Arrays.stream(TableType.class.getEnumConstants())
                .filter(item -> item.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
