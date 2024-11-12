package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class IcmpExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<Type> types;

    public IcmpExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public IcmpExpression(List<Type> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        if (types != null) {
            List<String> typeList = types.stream().map(IcmpExpression.Type::getValue).toList();
            return "dccp type %s {%s}".formatted(operation.getValue(), String.join(",", typeList));
        } else {
            return "dccp %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
        }
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
        GATEWAY("gateway");

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
