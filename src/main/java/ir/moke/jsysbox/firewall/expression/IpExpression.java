package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class IpExpression implements Expression {

    private final IpExpression.Field field;
    private final Operation operation;
    private final List<String> values;

    public IpExpression(IpExpression.Field field, Operation operation, List<String> values) {
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
        return MatchType.IP;
    }

    public enum Field {
        DSCP("dscp"),
        LENGTH("length"),
        ID("id"),
        FRAG_OFF("frag-off"),
        TTL("ttl"),
        PROTOCOL("protocol"),
        CHECKSUM("checksum"),
        SADDR("saddr"),
        DADDR("daddr"),
        VERSION("version"),
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
