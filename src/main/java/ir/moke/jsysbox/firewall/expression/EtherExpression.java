package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

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
            return "ether type %s {%s}".formatted(operation.getValue(), String.join(",", typeList));
        } else {
            return "ether %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    public enum Field {
        SADDR("saddr"),
        DADDR("daddr"),
        TYPE("type");

        private final String values;

        Field(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }

    public enum Type {
        ARP("arp"),
        IP("ip"),
        IP6("ip6"),
        VLAN("vlan");

        private final String values;

        Type(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }
}
