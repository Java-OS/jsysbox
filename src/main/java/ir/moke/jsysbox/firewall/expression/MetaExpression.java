package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class MetaExpression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public MetaExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "meta %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
    }

    public enum Field {
        IIFNAME("iifname"),
        OIFNAME("oifname"),
        IIF("iif"),
        OIF("oif"),
        IIFTYPE("iiftype"),
        OIFTYPE("oiftype"),
        LENGTH("length"),
        PROTOCOL("protocol"),
        NFPROTO("nfproto"),
        L4PROTO("l4proto"),
        MARK("mark"),
        PRIORITY("priority"),
        SKUID("skuid"),
        SKGID("skgid"),
        RTCLASSID("rtclassid"),
        PKTTYPE("pkttype"),
        CPU("cpu"),
        IIFGROUP("iifgroup"),
        OIFGROUP("oifgroup"),
        CGROUP("cgroup");

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
