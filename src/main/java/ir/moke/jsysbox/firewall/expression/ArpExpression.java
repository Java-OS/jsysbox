package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class ArpExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private List<ArpOperation> operations;

    public ArpExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public ArpExpression(List<ArpOperation> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        if (operations != null) {
            List<String> typeList = operations.stream().map(ArpExpression.ArpOperation::getValue).toList();
            return "arp operation %s {%s}".formatted(operation.getValue(), String.join(",", typeList));
        } else {
            return "arp %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    public enum ArpOperation {
        nak("nak"),
        inreply("inreply"),
        inrequest("inrequest"),
        rreply("rreply"),
        rrequest("rrequest"),
        reply("reply"),
        request("request");

        private final String values;

        ArpOperation(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }

    public enum Field {
        PTYPE("ptype"),
        HTYPE("htype"),
        HLEN("hlen"),
        PLEN("plen"),
        OPERATION("operation");

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
