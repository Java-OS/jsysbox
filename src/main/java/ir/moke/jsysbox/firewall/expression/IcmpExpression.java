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

    public IcmpExpression(Field field, Operation operation, List<String> values) {
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
    public MatchType matchType() {
        return MatchType.ICMP;
    }

    public enum Type {
        ECHO_REPLY("echo-reply"),
        DESTINATION_UNREACHABLE("destination-unreachable"),
        SOURCE_QUENCH("source-quench"),
        REDIRECT("redirect"),
        ECHO_REQUEST("echo-request"),
        TIME_EXCEEDED("time-exceeded"),
        PARAMETER_PROBLEM("parameter-problem"),
        TIMESTAMP_REQUEST("timestamp-request"),
        TIMESTAMP_REPLY("timestamp-reply"),
        INFO_REQUEST("info-request"),
        INFO_REPLY("info-reply"),
        ADDRESS_MASK_REQUEST("address-mask-request"),
        ADDRESS_MASK_REPLY("address-mask-reply"),
        ROUTER_ADVERTISEMENT("router-advertisement"),
        ROUTER_SOLICITATION("router-solicitation");

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

    public enum Field {
        TYPE("type"),
        CODE("code"),
        CHECKSUM("checksum"),
        ID("id"),
        SEQUENCE("sequence"),
        MTU("mtu"),
        GATEWAY("gateway");

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
