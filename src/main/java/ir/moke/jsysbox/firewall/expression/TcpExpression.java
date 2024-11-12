package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class TcpExpression implements Expression {

    private final Field field;
    private final Operation operation;
    private final List<String> values;

    public TcpExpression(Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "tcp %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
    }

    public enum Field {
        DPORT("dport"),
        SPORT("sport"),
        SEQUENCE("sequence"),
        ACKSEQ("ackseq"),
        FLAGS("flags"),
        WINDOW("window"),
        CHECKSUM("checksum"),
        URGPTR("urgptr"),
        DOFF("doff");

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
