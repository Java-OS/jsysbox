package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class IpExpression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public IpExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "ip %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
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

        private final String values;

        Field(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }
}
