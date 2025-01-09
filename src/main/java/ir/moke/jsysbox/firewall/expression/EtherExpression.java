package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class EtherExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<Type> types;

    public EtherExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public EtherExpression(List<Type> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        if (types != null) {
            List<String> typeList = types.stream().map(EtherExpression.Type::getValue).toList();
            return "%s type {%s}".formatted(matchType().getValue(), String.join(",", typeList));
        } else {
            return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    @Override
    public MatchType matchType() {
        return MatchType.ETHER;
    }

    public enum Field {
        SADDR("saddr"),
        DADDR("daddr"),
        TYPE("type");

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

    public enum Type {
        ARP("arp"),
        IP("ip"),
        IP6("ip6"),
        VLAN("vlan");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            return Arrays.stream(Type.class.getEnumConstants())
                    .filter(item -> item.value.equals(value.equals("8021q") ? "vlan" : value))
                    .findFirst()
                    .orElse(null);
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }
}
