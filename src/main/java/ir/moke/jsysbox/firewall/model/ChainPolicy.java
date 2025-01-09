package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ChainPolicy {
    ACCEPT("accept"),
    DROP("drop");

    private final String value;

    ChainPolicy(String value) {
        this.value = value;
    }

    public static ChainPolicy fromValue(String value) {
        return Arrays.stream(ChainPolicy.class.getEnumConstants())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
