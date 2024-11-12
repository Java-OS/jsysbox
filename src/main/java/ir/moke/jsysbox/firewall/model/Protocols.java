package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Protocols {
    IP(0, "ip"),
    HOPOPT(0, "hopopt"),
    ICMP(1, "icmp"),
    IGMP(2, "igmp"),
    GGP(3, "ggp"),
    IP_ENCAP(4, "ipencap"),
    ST(5, "st"),
    TCP(6, "tcp"),
    EGP(8, "egp"),
    IGP(9, "igp"),
    PUP(12, "pup"),
    UDP(17, "udp"),
    HMP(20, "hmp"),
    XNS_IDP(22, "xns-idp"),
    RDP(27, "rdp"),
    ISO_TP4(29, "iso-tp4"),
    DCCP(33, "dccp"),
    XTP(36, "xtp"),
    DDP(37, "ddp"),
    IDPR_CMTP(38, "idpr-cmtp"),
    IPV6(41, "ipv6"),
    IPV6_ROUTE(43, "ipv6-route"),
    IPV6_FRAG(44, "ipv6-frag"),
    IDRP(45, "idrp"),
    RSVP(46, "rsvp"),
    GRE(47, "gre"),
    IPSEC_ESP(50, "esp"),
    IPSEC_AH(51, "ah"),
    SKIP(57, "skip"),
    IPV6_ICMP(58, "ipv6-icmp"),
    IPV6_NONXT(59, "ipv6-nonxt"),
    IPV6_OPTS(60, "ipv6-opts"),
    RSPF(73, "rspf"),
    VMTP(81, "vmtp"),
    EIGRP(88, "eigrp"),
    OSPFIGP(89, "ospf"),
    AX25(93, "ax.25"),
    IPIP(94, "ipip"),
    ETHERIP(97, "etherip"),
    ENCAP(98, "encap"),
    PIM(103, "pim"),
    IPCOMP(108, "ipcomp"),
    VRRP(112, "vrrp"),
    L2TP(115, "l2tp"),
    ISIS(124, "isis"),
    SCTP(132, "sctp"),
    FC(133, "fc"),
    MOBILITY_HEADER(135, "mobility-header"),
    UDPLITE(136, "udplite"),
    MPLS_IN_IP(137, "mpls-in-ip"),
    HIP(139, "hip"),
    SHIM6(140, "shim6"),
    WESP(141, "wesp"),
    ROHC(142, "rohc");

    private final int code;
    private final String value;

    Protocols(int code, String value) {
        this.code = code;
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}
