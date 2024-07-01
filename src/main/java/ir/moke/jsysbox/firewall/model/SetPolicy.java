package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SetPolicy {
    PERFORMANCE("performance"),
    MEMORY("memory");

    private final String value;

    SetPolicy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
