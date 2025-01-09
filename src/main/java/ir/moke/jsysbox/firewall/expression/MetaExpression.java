package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
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
        return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
    }

    @Override
    public MatchType matchType() {
        return MatchType.META;
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
