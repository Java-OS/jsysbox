package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChainHook {
    INGRESS("ingress"),
    EGRESS("egress"),
    PREROUTING("prerouting"),
    FORWARD("forward"),
    POSTROUTING("postrouting"),
    INPUT("input"),
    OUTPUT("output");

    private final String value;

    ChainHook(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
