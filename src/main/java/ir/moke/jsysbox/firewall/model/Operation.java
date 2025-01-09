package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

    public static Operation fromValue(String value) {
        return Arrays.stream(Operation.class.getEnumConstants())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
