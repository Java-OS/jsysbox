package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChainType {
    FILTER("filter"),
    ROUTE("route"),
    NAT("nat");

    private final String value;

    ChainType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
