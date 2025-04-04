package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class ArpExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<ArpOperation> arpOperations;

    public ArpExpression(ArpExpression.Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public ArpExpression(List<ArpOperation> arpOperations) {
        this.arpOperations = arpOperations;
    }

    @Override
    public String toString() {
        if (arpOperations != null) {
            List<String> typeList = arpOperations.stream().map(ArpExpression.ArpOperation::getValue).toList();
            return "%s operation {%s}".formatted(matchType().getValue(), String.join(",", typeList));
        } else {
            return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public Operation getOperation() {
        return this.operation;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Override
    public MatchType matchType() {
        return MatchType.ARP;
    }

    public List<ArpOperation> getArpOperations() {
        return arpOperations;
    }

    public enum ArpOperation {
        inreply("inreply"), inrequest("inrequest"), nak("nak"), reply("reply"), request("request"), rreply("rreply"), rrequest("rrequest");

        private final String value;

        ArpOperation(String value) {
            this.value = value;
        }

        public static ArpOperation fromValue(String value) {
            return Arrays.stream(ArpOperation.class.getEnumConstants()).filter(item -> item.value.equals(value)).findFirst().orElse(null);
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public enum Field implements Expression.Field {
        HLEN("hlen"),
        HTYPE("htype"),
        OPERATION("operation"),
        PLEN("plen"),
        PTYPE("ptype"),
        TYPE("type");

        private final String value;

        Field(String value) {
            this.value = value;
        }

        public static Field fromValue(String value) {
            return Arrays.stream(Field.class.getEnumConstants()).filter(item -> item.value.equals(value)).findFirst().orElse(null);
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }
}
