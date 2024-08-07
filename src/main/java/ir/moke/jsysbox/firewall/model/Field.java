package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Field {
    DSCP("dscp"),
    LENGTH("length"),
    ID("id"),
    FRAG_OFF("frag-off"),
    TTL("ttl"),
    PROTOCOL("protocol"),
    CHECKSUM("checksum"),
    SADDR("saddr"),
    DADDR("daddr"),
    VERSION("version"),
    HDRLENGTH("hdrlength"),
    FLOWLABEL("flowlabel"),
    NEXTHDR("nexthdr"),
    HOPLIMIT("hoplimit"),
    DPORT("dport"),
    SPORT("sport"),
    SEQUENCE("sequence"),
    ACKSEQ("ackseq"),
    FLAGS("flags"),
    WINDOW("window"),
    URGPTR("urgptr"),
    DOFF("doff"),
    VTAG("vtag"),
    CHUNK("chunk"),
    TYPE("type"),
    RESERVED("reserved"),
    SPI("spi"),
    CPI("cpi"),
    CODE("code"),
    MTU("mtu"),
    GATEWAY("gateway"),
    MAX_DELAY("max-delay"),
    MORE_FRAGMENTS("more-fragments"),
    SEG_LEFT("seg-left"),
    CFI("cfi"),
    PCP("pcp"),
    PTYPE("ptype"),
    HTYPE("htype"),
    HLEN("hlen"),
    PLEN("plen"),
    OPERATION("operation"),
    STATE("state"),
    DIRECTION("direction"),
    STATUS("status"),
    MARK("mark"),
    EXPIRATION("expiration"),
    HELPER("helper"),
    ORIGINAL("original"),
    REPLY("reply"),
    COUNT("count"),
    IIFNAME("iifname"),
    OIFNAME("oifname"),
    IIF("iif"),
    OIF("oif"),
    IIFTYPE("iiftype"),
    OIFTYPE("oiftype"),
    NFPROTO("nfproto"),
    L4PROTO("l4proto"),
    PRIORITY("priority"),
    SKUID("skuid"),
    SKGID("skgid"),
    RTCLASSID("rtclassid"),
    PKTTYPE("pkttype"),
    CPU("cpu"),
    IIFGROUP("iifgroup"),
    OIFGROUP("oifgroup"),
    CGROUP("cgroup");

    private final String values;

    Field(String value) {
        this.values = value;
    }

    @JsonValue
    public String getValue() {
        return values;
    }
}
