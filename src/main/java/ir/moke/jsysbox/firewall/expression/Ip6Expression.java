package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class Ip6Expression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public Ip6Expression(Ip6Expression.Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public MatchType matchType() {
        return MatchType.IP6;
    }

    @Override
    public Operation getOperation() {
        return this.operation;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    public enum Field implements Expression.Field {
        DADDR("daddr"),
        DSCP("dscp"),
        FLOWLABEL("flowlabel"),
        HOPLIMIT("hoplimit"),
        LENGTH("length"),
        NEXTHDR("nexthdr"),
        SADDR("saddr"),
        VERSION("version");

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
