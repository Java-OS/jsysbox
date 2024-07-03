package ir.moke.jsysbox.firewall.statement;

public enum RejectStatement implements Statement {
    REJECT("reject"),
    /* ICMP */
    ICMP_TYPE_HOST_UNREACHABLE("reject with icmp type host-unreachable"),
    ICMP_TYPE_NET_UNREACHABLE("reject with icmp type net-unreachable"),
    ICMP_TYPE_PROT_UNREACHABLE("reject with icmp type prot-unreachable"),
    ICMP_TYPE_PORT_UNREACHABLE("reject with icmp type port-unreachable"),
    ICMP_TYPE_NET_PROHIBITED("reject with icmp type net-prohibited"),
    ICMP_TYPE_HOST_PROHIBITED("reject with icmp type host-prohibited"),
    ICMP_TYPE_ADMIN_PROHIBITED("reject with icmp type admin-prohibited"),
    /* ICMP V6 */
    ICMPV6_TYPE_NO_ROUTE("reject with icmpv6 type no-route"),
    ICMPV6_TYPE_ADMIN_PROHIBITED("reject with icmpv6 type admin-prohibited"),
    ICMPV6_TYPE_ADDR_UNREACHABLE("reject with icmpv6 type addr-unreachable"),
    ICMPV6_TYPE_PORT_UNREACHABLE("reject with icmpv6 type port-unreachable"),
    /* ICMPX */
    ICMPX_TYPE_HOST_UNREACHABLE("reject with icmpx type host-unreachable"),
    ICMPX_TYPE_NO_ROUTE("reject with icmpx type no-route"),
    ICMPX_TYPE_ADMIN_PROHIBITED("reject with icmpx type admin-prohibited"),
    ICMPX_TYPE_PORT_UNREACHABLE("reject with icmpx type port-unreachable");

    private final String value;

    RejectStatement(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
