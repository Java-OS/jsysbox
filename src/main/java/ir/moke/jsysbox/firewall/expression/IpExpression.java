package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class IpExpression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public IpExpression(IpExpression.Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public Field getField() {
        return field;
    }

    public Operation getOperation() {
        return operation;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
    }

    @Override
    public MatchType matchType() {
        return MatchType.IP;
    }


    public enum Field implements Expression.Field {
        CHECKSUM("checksum"),
        DADDR("daddr"),
        DSCP("dscp"),
        FRAG_OFF("frag-off"),
        HDRLENGTH("hdrlength"),
        ID("id"),
        LENGTH("length"),
        PROTOCOL("protocol"),
        SADDR("saddr"),
        TTL("ttl"),
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
