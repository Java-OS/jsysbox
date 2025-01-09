package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
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
            return "%s type %s {%s}".formatted(matchType().getValue(), operation.getValue(), String.join(",", typeList));
        } else {
            return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    @Override
    public MatchType matchType() {
        return MatchType.DCCP;
    }

    public enum Field {
        DPORT("dport"),
        SPORT("sport"),
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
}
