package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

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
            return "dccp type %s {%s}".formatted(operation.getValue(), String.join(",", typeList));
        } else {
            return "dccp %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
        }
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

        private final String values;

        Type(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }

    public enum Field {
        CODE("code"),
        CHECKSUM("checksum"),
        ID("id"),
        SEQUENCE("sequence"),
        MTU("mtu"),
        MAX_DELAY("max-delay");

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
