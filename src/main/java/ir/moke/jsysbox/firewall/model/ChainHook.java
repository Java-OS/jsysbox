package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

    public static ChainHook fromValue(String value) {
        return Arrays.stream(ChainHook.class.getEnumConstants())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
