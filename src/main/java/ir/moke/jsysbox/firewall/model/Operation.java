package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Operation {
    EQ("=="),
    NE("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">=");

    private final String value;

    Operation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
