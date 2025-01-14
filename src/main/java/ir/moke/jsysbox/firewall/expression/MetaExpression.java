package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class MetaExpression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public MetaExpression(MetaExpression.Field field, Operation operation, List<String> values) {
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
        return MatchType.META;
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
        CGROUP("cgroup"),
        CPU("cpu"),
        IIF("iif"),
        IIFGROUP("iifgroup"),
        IIFNAME("iifname"),
        IIFTYPE("iiftype"),
        L4PROTO("l4proto"),
        LENGTH("length"),
        MARK("mark"),
        NFPROTO("nfproto"),
        OIF("oif"),
        OIFGROUP("oifgroup"),
        OIFNAME("oifname"),
        OIFTYPE("oiftype"),
        PKTTYPE("pkttype"),
        PRIORITY("priority"),
        PROTOCOL("protocol"),
        RTCLASSID("rtclassid"),
        SKGID("skgid"),
        SKUID("skuid");

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
