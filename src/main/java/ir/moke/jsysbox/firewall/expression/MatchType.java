package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum MatchType {
    IP("ip"),
    IP6("ip6"),
    TCP("tcp"),
    UDP("udp"),
    UDPLITE("udplite"),
    SCTP("sctp"),
    DCCP("dccp"),
    AH("ah"),
    ESP("esp"),
    COMP("comp"),
    ICMP("icmp"),
    ICMPV6("icmpv6"),
    ETHER("ether"),
    DST("dst"),
    FRAG("frag"),
    HBH("hbh"),
    MH("mh"),
    RT("rt"),
    VLAN("vlan"),
    ARP("arp"),
    CT("ct"),
    META("meta");

    private final String value;

    MatchType(String value) {
        this.value = value;
    }

    public static MatchType fromValue(String value) {
        return Arrays.stream(MatchType.class.getEnumConstants())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
