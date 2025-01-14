package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class IcmpExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<Type> types;

    public IcmpExpression(IcmpExpression.Field field, Operation operation, List<String> values) {
        this.field = field;
        this.operation = operation;
        this.values = values;
    }

    public IcmpExpression(List<Type> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        if (types != null) {
            List<String> typeList = types.stream().map(IcmpExpression.Type::getValue).toList();
            return "%s type {%s}".formatted(matchType().getValue(), String.join(",", typeList));
        } else {
            if (operation.equals(Operation.EQ) || operation.equals(Operation.NE)) {
                return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
            } else {
                return "%s %s %s %s".formatted(matchType().getValue(), field.getValue(), operation.getValue(), values.getFirst());
            }
        }
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    public List<Type> getTypes() {
        return types;
    }

    @Override
    public MatchType matchType() {
        return MatchType.ICMP;
    }

    @Override
    public Operation getOperation() {
        return this.operation;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    public enum Type {
        ADDRESS_MASK_REPLY("address-mask-reply"),
        ADDRESS_MASK_REQUEST("address-mask-request"),
        DESTINATION_UNREACHABLE("destination-unreachable"),
        ECHO_REPLY("echo-reply"),
        ECHO_REQUEST("echo-request"),
        INFO_REPLY("info-reply"),
        INFO_REQUEST("info-request"),
        PARAMETER_PROBLEM("parameter-problem"),
        REDIRECT("redirect"),
        ROUTER_ADVERTISEMENT("router-advertisement"),
        ROUTER_SOLICITATION("router-solicitation"),
        SOURCE_QUENCH("source-quench"),
        TIMESTAMP_REPLY("timestamp-reply"),
        TIMESTAMP_REQUEST("timestamp-request"),
        TIME_EXCEEDED("time-exceeded");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            return Arrays.stream(Type.class.getEnumConstants())
                    .filter(item -> item.value.equals(value))
                    .findFirst()
                    .orElse(null);
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public enum Field implements Expression.Field {
        CHECKSUM("checksum"),
        CODE("code"),
        GATEWAY("gateway"),
        ID("id"),
        MTU("mtu"),
        SEQUENCE("sequence"),
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
}
