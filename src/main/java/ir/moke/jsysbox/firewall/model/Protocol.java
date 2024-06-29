package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Protocol {
    IP("ip"),
    IP6("ip6"),
    TCP("tcp"),
    UDP("udp"),
    ICMP("icmp"),
    ICMPV6("icmpv6"),
    SCTP("sctp"),
    ARP("arp"),
    ETHER("ether");

    private final String value;

    Protocol(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
