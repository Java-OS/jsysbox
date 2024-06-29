package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SetType {
    IPV4_ADDR("ipv4_addr"),
    IPV6_ADDR("ipv6_addr"),
    ETHER_ADDR("ether_addr"),
    INET_PROTO("inet_proto"),
    INET_SERVICE("inet_service"),
    MARK("mark"),
    IFNAME("ifname");

    private final String value;

    SetType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
