package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class Icmpv6Expression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<Type> types;

    public Icmpv6Expression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public Icmpv6Expression(List<Type> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        if (types != null) {
            List<String> typeList = types.stream().map(Icmpv6Expression.Type::getValue).toList();
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
        return MatchType.ICMPV6;
    }

    public enum Type {
        DESTINATION_UNREACHABLE("destination-unreachable"),
        PACKET_TOO_BIG("packet-too-big"),
        TIME_EXCEEDED("time-exceeded"),
        ECHO_REQUEST("echo-request"),
        ECHO_REPLY("echo-reply"),
        MLD_LISTENER_QUERY("mld-listener-query"),
        MLD_LISTENER_REPORT("mld-listener-report"),
        MLD_LISTENER_REDUCTION("mld-listener-reduction"),
        ND_ROUTER_SOLICIT("nd-router-solicit"),
        ND_ROUTER_ADVERT("nd-router-advert"),
        ND_NEIGHBOR_SOLICIT("nd-neighbor-solicit"),
        ND_NEIGHBOR_ADVERT("nd-neighbor-advert"),
        PARAMETER_PROBLEM("parameter-problem"),
        MLD2_LISTENER_REPORT("mld2-listener-report");

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
        MAX_DELAY("max-delay");

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
