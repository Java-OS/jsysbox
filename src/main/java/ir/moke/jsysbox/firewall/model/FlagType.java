package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FlagType {
    CONSTANT("constant"),
    INTERVAL("interval"),
    TIMEOUT("timeout");

    private final String value;

    FlagType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
