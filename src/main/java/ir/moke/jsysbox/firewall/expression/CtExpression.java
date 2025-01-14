package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.Arrays;
import java.util.List;

public class CtExpression implements Expression {

    private Field field;
    private Operation operation;
    private List<String> values;
    private Boolean originalType;
    private Type type;
    private Boolean over;
    private Long value;
    private List<Status> statuses;
    private List<State> states;

    public CtExpression(CtExpression.Field field, Operation operation, List<String> values) {
        if (field.equals(Field.STATE)) {
            states = values.stream().map(State::fromValue).toList();
        } else if (field.equals(Field.STATUS)) {
            statuses = values.stream().map(Status::fromValue).toList();
        } else {
            this.field = field;
        }
        this.values = values;
        this.operation = operation;
    }

    public CtExpression(Boolean originalType, Type type, List<String> values) {
        this.operation = Operation.EQ;
        this.originalType = originalType;
        this.values = values;
        this.type = type;
    }

    public CtExpression(Boolean over, Long value) {
        this.field = Field.COUNT;
        this.over = over;
        this.value = value;
    }

    @Override
    public String toString() {
        if (states != null && !states.isEmpty()) {
            return "%s %s %s {%s}".formatted(matchType().getValue(), Field.STATE.getValue(), operation.getValue(), String.join(",", values));
        } else if (statuses != null && !statuses.isEmpty()) {
            return "%s %s %s {%s}".formatted(matchType().getValue(), Field.STATUS.getValue(), operation.getValue(), String.join(",", values));
        } else if (originalType != null) {
            String typeMode = originalType ? "original" : "reply";
            StringBuilder sb = new StringBuilder("ct ").append(typeMode).append(" ");
            switch (type) {
                case BYTES, PACKETS -> sb.append("{ ").append(String.join(",", values)).append(" }");
                case SADDR -> sb.append("ip saddr").append("{ ").append(String.join(",", values)).append(" }");
                case DADDR -> sb.append("ip daddr").append("{ ").append(String.join(",", values)).append(" }");
                case L3PROTO -> sb.append("l3proto").append("{ ").append(String.join(",", values)).append(" }");
                case PROTOCOL -> sb.append("protocol").append("{ ").append(String.join(",", values)).append(" }");
                case PROTO_DST -> sb.append("proto-dst").append("{ ").append(String.join(",", values)).append(" }");
                case PROTO_SRC -> sb.append("proto-src").append("{ ").append(String.join(",", values)).append(" }");
            }
            return sb.toString();
        } else {
            if (field.equals(Field.COUNT)) {
                return "%s count %s %s".formatted(matchType().getValue(), (over != null && over) ? "over" : "", value);
            } else {
                return "%s %s %s {%s}".formatted(matchType().getValue(), field.getValue(), operation.getValue(), String.join(",", values));
            }
        }
    }

    public List<State> getStates() {
        return states;
    }

    public List<Status> getStatuses() {
        return statuses;
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
        return MatchType.CT;
    }

    public Boolean getOriginalType() {
        return originalType;
    }

    public Type getType() {
        return type;
    }

    public Boolean getOver() {
        return over;
    }

    public Long getValue() {
        return value;
    }

    public enum Type {
        BYTES("bytes"),
        L3PROTO("l3proto"),
        PACKETS("packets"),
        PROTOCOL("protocol"),
        PROTO_DST("proto-dst"),
        PROTO_SRC("proto-src"),
        DADDR("ip daddr"),
        SADDR("ip daddr");

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

    public enum Status {
        ASSURED("assured"),
        CONFIRMED("confirmed"),
        DNAT("dnat"),
        DYING("dying"),
        EXPECTED("expected"),
        SEEN_REPLY("seen-reply"),
        SNAT("snat");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public static Status fromValue(String value) {
            return Arrays.stream(Status.class.getEnumConstants())
                    .filter(item -> item.value.equals(value))
                    .findFirst()
                    .orElse(null);
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public enum State {
        ESTABLISHED("established"),
        NEW("new"),
        RELATED("related"),
        UNTRACKED("untracked");

        private final String value;

        State(String value) {
            this.value = value;
        }

        public static State fromValue(String value) {
            return Arrays.stream(State.class.getEnumConstants())
                    .filter(item -> item.value.equals(value))
                    .findFirst()
                    .orElse(null);
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public enum Field implements Expression.Field {
        COUNT("count"),
        DIRECTION("direction"),
        EXPIRATION("expiration"),
        HELPER("helper"),
        MARK("mark"),
        STATE("state"),
        STATUS("status");

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
}
