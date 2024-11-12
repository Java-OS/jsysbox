package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class Ip6Expression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public Ip6Expression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "ip6 %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
    }

    public enum Field {
        DSCP("dscp"),
        FLOWLABEL("flowlabel"),
        LENGTH("length"),
        NEXTHDR("nexthdr"),
        HOPLIMIT("hoplimit"),
        SADDR("saddr"),
        DADDR("daddr"),
        VERSION("version");

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
