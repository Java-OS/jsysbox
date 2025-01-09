package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ChainType {
    FILTER("filter"),
    ROUTE("route"),
    NAT("nat");

    private final String value;

    ChainType(String value) {
        this.value = value;
    }

    public static ChainType fromValue(String value) {
        return Arrays.stream(ChainType.class.getEnumConstants())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
