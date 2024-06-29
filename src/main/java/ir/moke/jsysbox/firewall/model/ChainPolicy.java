package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChainPolicy {
    ACCEPT("accept"),
    DROP("drop"),
    QUEUE("queue"),
    CONTINUE("continue"),
    RETURN("return"),
    JUMP("jump"),
    GOTO("goto");

    private final String value;

    ChainPolicy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
