package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SetPolicy {
    PERFORMANCE("performance"),
    MEMORY("memory");

    private final String value;

    SetPolicy(String value) {
        this.value = value;
    }

    public static SetPolicy fromValue(String value) {
        return Arrays.stream(SetPolicy.class.getEnumConstants())
                .filter(item -> item.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
