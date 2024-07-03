package ir.moke.jsysbox.firewall.model;

import java.util.List;

public class Expression {
    private Operation operation;
    private MatchType matchType;
    private Field field;
    private List<String> values;

    public Expression(MatchType matchType, Field field, Operation operation, List<String> values) {
        this.operation = operation;
        this.matchType = matchType;
        this.field = field;
        this.values = values;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "%s %s %s { %s }".formatted(matchType.getValue(), field.getValue(), operation.getValue(), String.join(",", values));
    }
}
