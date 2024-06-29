package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChainHook {
    NETDEV_INGRESS("ingress"),
    NETDEV_EGRESS("ingress"),
    INET_PREROUTING("prerouting"),
    INET_INPUT("input"),
    INET_FORWARD("forward"),
    INET_OUTPUT("output"),
    INET_POSTROUTING("postrouting"),
    ARP_INPUT("input"),
    ARP_OUTPUT("output");

    private final String value;

    ChainHook(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
