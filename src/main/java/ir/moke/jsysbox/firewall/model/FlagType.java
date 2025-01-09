package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum FlagType {
    CONSTANT("constant"),
    INTERVAL("interval"),
    TIMEOUT("timeout");

    private final String value;

    FlagType(String value) {
        this.value = value;
    }

    public static FlagType fromValue(String value) {
        return Arrays.stream(FlagType.class.getEnumConstants())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
