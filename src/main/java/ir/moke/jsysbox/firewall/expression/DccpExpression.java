package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class DccpExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<Type> types;

    public DccpExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public DccpExpression(List<Type> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        if (types != null) {
            List<String> typeList = types.stream().map(Type::getValue).toList();
            return "dccp type %s {%s}".formatted(operation.getValue(), String.join(",", typeList));
        } else {
            return "dccp %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    public enum Field {
        DPORT("dport"),
        SPORT("sport"),
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
        REQUEST("request"),
        RESPONSE("response"),
        DATA("data"),
        ACK("ack"),
        DATAACK("dataack"),
        CLOSEREQ("closereq"),
        CLOSE("close"),
        RESET("reset"),
        SYNC("sync"),
        SYNCACK("syncack");

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
