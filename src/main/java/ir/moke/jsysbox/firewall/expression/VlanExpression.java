package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class VlanExpression implements Expression {

    private final VlanExpression.Field field;
    private final Operation operation;
    private final List<String> values;

    public VlanExpression(VlanExpression.Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "vlan %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
    }

    public enum Field {
        ID("id"),
        CFI("cfi"),
        PCP("pcp");

        private final String value;

        Field(String value) {
            this.value = value;
        }

        public static VlanExpression.Field getField(String value) {
            return Arrays.stream(VlanExpression.Field.class.getEnumConstants())
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
