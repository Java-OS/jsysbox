package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getValue() {
        return value;
    }
}
