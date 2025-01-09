package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class DstExpression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public DstExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
    }

    @Override
    public MatchType matchType() {
        return MatchType.DST;
    }

    public enum Field {
        NEXTHDR("nexthdr"),
        HDRLENGTH("hdrlength");

        private final String value;

        Field(String value) {
            this.value = value;
        }
        public static Field fromValue(String value) {
            return Arrays.stream(Field.class.getEnumConstants())
                    .filter(item -> item.value.equals(value))
                    .findFirst()
                    .orElse(null);
        }
        @JsonValue
        public String getValue() {
            return value;
        }
    }
}
