package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.annotation.JsonValue;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.List;

public class CtExpression implements Expression {

    private CtExpression.Field field;
    private Operation operation;
    private List<String> values;
    private Boolean originalType;
    private Type type;
    private Boolean over;
    private long value;

    public CtExpression(CtExpression.Field field, Operation operation, List<String> values) {
        this.field = field;
        this.values = values;
        this.operation = operation;
    }

    public CtExpression(Boolean originalType, Type type, List<String> values) {
        this.originalType = originalType;
        this.values = values;
        this.type = type;
    }

    public CtExpression(Boolean over, long value) {
        this.field = Field.COUNT;
        this.over = over;
        this.value = value;
    }

    @Override
    public String toString() {
        if (originalType != null) {
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
            if (Field.COUNT.equals(field)) {
                return "ct count %s %s".formatted((over != null && over) ? "over" : "", value);
            }

            return "ct %s %s {%s}".formatted(field.getValue(), operation.getValue(), String.join(",", values));
        }
    }

    public enum Type {
        BYTES("bytes"),
        PACKETS("packets"),
        SADDR("saddr"),
        DADDR("daddr"),
        L3PROTO("l3proto"),
        PROTOCOL("protocol"),
        PROTO_DST("proto_dst"),
        PROTO_SRC("proto_src");

        private final String values;

        Type(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }

    public enum Status {
        EXPECTED("expected"),
        SEEN_REPLY("seen-reply"),
        ASSURED("assured"),
        CONFIRMED("confirmed"),
        SNAT("snat"),
        DNAT("dnat"),
        DYING("dying");

        private final String values;

        Status(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }

    public enum State {
        NEW("new"),
        ESTABLISHED("established"),
        RELATED("related"),
        UNTRACKED("untracked");

        private final String values;

        State(String value) {
            this.values = value;
        }

        @JsonValue
        public String getValue() {
            return values;
        }
    }

    public enum Field {
        STATE("state"),
        DIRECTION("direction"),
        STATUS("status"),
        MARK("mark"),
        EXPIRATION("expiration"),
        HELPER("helper"),
        COUNT("count");

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
