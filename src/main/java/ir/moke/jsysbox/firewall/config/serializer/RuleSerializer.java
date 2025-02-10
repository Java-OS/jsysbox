package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.expression.CtExpression;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.expression.MatchType;
import ir.moke.jsysbox.firewall.expression.MetaExpression;
import ir.moke.jsysbox.firewall.model.Operation;
import ir.moke.jsysbox.firewall.model.Rule;
import ir.moke.jsysbox.firewall.statement.*;

import java.io.IOException;
import java.util.List;

public class RuleSerializer extends JsonSerializer<Rule> {

    @Override
    public void serialize(Rule rule, JsonGenerator gen, SerializerProvider serializers) {
        try {
            gen.writeStartObject();
            gen.writeObjectFieldStart("rule");
            gen.writeStringField("family", rule.getChain().getTable().getType().getValue());
            gen.writeStringField("table", rule.getChain().getTable().getName());
            gen.writeStringField("chain", rule.getChain().getName());
            gen.writeNumberField("handle", rule.getHandle());
            gen.writeStringField("comment", rule.getComment());

            // Start expr
            gen.writeArrayFieldStart("expr");

            // fill expressions
            if (rule.getExpressions() != null && !rule.getExpressions().isEmpty()) {
                for (Expression expression : rule.getExpressions()) {
                    MatchType matchType = expression.matchType();
                    if (matchType.equals(MatchType.CT)) {
                        parseCtExpression(gen, (CtExpression) expression);
                    } else {
                        parsePayloadExpression(gen, expression);
                    }
                }
            }

            // fill statement
            List<Statement> statements = rule.getStatements();
            for (Statement statement : statements) {
                if (statement instanceof VerdictStatement verdictStatement) {
                    parseVerdictStatement(gen, verdictStatement);
                } else if (statement instanceof LogStatement logStatement) {
                    parseLogStatement(gen, logStatement);
                } else if (statement instanceof RejectStatement rejectStatement) {
                    parseRejectStatement(gen, rejectStatement);
                } else if (statement instanceof CounterStatement counterStatement) {
                    parseCounterStatement(gen, counterStatement);
                } else if (statement instanceof LimitStatement limitStatement) {
                    parseLimitStatement(gen, limitStatement);
                } else if (statement instanceof NatStatement natStatement) {
                    parseNatStatement(gen, natStatement);
                }
            }

            // End expr
            gen.writeEndArray();

            gen.writeEndObject();
            gen.writeEndObject();
        } catch (Exception e) {
            throw new JSysboxException("Failed to serialize rule: %s".formatted(rule.getHandle()), e);
        }
    }

    private static void parseNatStatement(JsonGenerator gen, NatStatement natStatement) throws IOException {
        gen.writeStartObject();
        NatStatement.Type type = natStatement.getType();
        if (natStatement.getFlag() == null) {
            gen.writeStartObject();
            gen.writeStringField(type.name().toLowerCase(), null);
            gen.writeEndObject();
        } else {
            gen.writeObjectFieldStart(type.name().toLowerCase());
            if (type.equals(NatStatement.Type.SNAT) || type.equals(NatStatement.Type.DNAT)) {
                gen.writeStringField("addr", natStatement.getAddress());
                if (natStatement.getPort() != null) gen.writeNumberField("port", natStatement.getPort());
                if (natStatement.getFlag() != null) {
                    gen.writeArrayFieldStart("flags");
                    for (NatStatement.Flag flag : natStatement.getFlag()) {
                        gen.writeString(flag.getValue());
                    }
                    gen.writeEndArray();
                }
            } else if (type.equals(NatStatement.Type.REDIRECT)) {
                gen.writeNumberField("port", natStatement.getPort());
                if (natStatement.getFlag() != null) {
                    gen.writeArrayFieldStart("flags");
                    for (NatStatement.Flag flag : natStatement.getFlag()) {
                        gen.writeString(flag.getValue());
                    }
                    gen.writeEndArray();
                }
            } else {
                if (natStatement.getFlag() != null) {
                    gen.writeArrayFieldStart("flags");
                    for (NatStatement.Flag flag : natStatement.getFlag()) {
                        gen.writeString(flag.getValue());
                    }
                    gen.writeEndArray();
                }
            }
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    private static void parseLimitStatement(JsonGenerator gen, LimitStatement limitStatement) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("limit");

        Long rate = limitStatement.getRate();
        Long burst = limitStatement.getBurst();
        LimitStatement.TimeUnit timeUnit = limitStatement.getTimeUnit();
        Boolean isOver = limitStatement.isOver();
        LimitStatement.ByteUnit rateUnit = limitStatement.getRateUnit();
        LimitStatement.ByteUnit burstUnit = limitStatement.getBurstUnit();

        gen.writeNumberField("rate", rate);
        gen.writeStringField("rate_unit", rateUnit.name().toLowerCase());
        if (timeUnit != null) gen.writeStringField("per", timeUnit.name().toLowerCase());
        if (isOver != null) gen.writeBooleanField("inv", isOver);
        if (burst != null) gen.writeNumberField("burst", burst);
        if (burstUnit != null) gen.writeStringField("burst_unit", burstUnit.name().toLowerCase());

        gen.writeEndObject();
        gen.writeEndObject();
    }

    private static void parseCounterStatement(JsonGenerator gen, CounterStatement counterStatement) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("counter");
        gen.writeNumberField("packets", counterStatement.getPackets());
        gen.writeNumberField("bytes", counterStatement.getBytes());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    private static void parseRejectStatement(JsonGenerator gen, RejectStatement rejectStatement) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("reject");
        gen.writeStringField("type", rejectStatement.getReason().equals(RejectStatement.Reason.TCP_RESET) ? "tcp reset" : "icmp");
        if (rejectStatement.getReason() != null) {
            gen.writeStringField("expr", rejectStatement.getReason().getValue().toLowerCase());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    private static void parseLogStatement(JsonGenerator gen, LogStatement logStatement) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("log");
        if (logStatement.getLevel() != null) gen.writeStringField("level", logStatement.getLevel().name().toLowerCase());
        if (logStatement.getPrefix() != null) gen.writeStringField("prefix", logStatement.getPrefix());

        gen.writeEndObject();
        gen.writeEndObject();
    }

    private static void parseVerdictStatement(JsonGenerator gen, VerdictStatement verdictStatement) throws IOException {
        gen.writeStartObject();
        VerdictStatement.Type type = verdictStatement.getType();
        switch (type) {
            case ACCEPT -> gen.writeStringField("accept", null);
            case DROP -> gen.writeStringField("drop", null);
            case QUEUE -> gen.writeStringField("queue", null);
            case CONTINUE -> gen.writeStringField("continue", null);
            case RETURN -> gen.writeStringField("return", null);
            case JUMP -> {
                gen.writeObjectFieldStart("jump");
                gen.writeStringField("target", verdictStatement.getChainName());
                gen.writeEndObject();
            }
            case GOTO -> {
                gen.writeObjectFieldStart("goto");
                gen.writeStringField("target", verdictStatement.getChainName());
                gen.writeEndObject();
            }
        }
        gen.writeEndObject();
    }

    private static void parsePayloadExpression(JsonGenerator gen, Expression expression) throws IOException {
        //Start match
        gen.writeStartObject();
        gen.writeObjectFieldStart("match");

        // Operation
        gen.writeStringField("op", expression.getOperation().getValue());

        // Left
        gen.writeObjectFieldStart("left");
        if (expression.matchType().equals(MatchType.META)) {
            MetaExpression metaExpression = (MetaExpression) expression;
            gen.writeObjectFieldStart("meta");
            gen.writeStringField("key", metaExpression.getField().getValue());
            gen.writeEndObject();
        } else {
            gen.writeObjectFieldStart("payload");
            gen.writeStringField("protocol", expression.matchType().getValue());
            gen.writeStringField("field", expression.getField().getValue());
            gen.writeEndObject();
        }
        gen.writeEndObject();

        // Right
        if (expression.getValues().size() == 1) {
            gen.writeStringField("right", expression.getValues().getFirst());
        } else {
            gen.writeObjectFieldStart("right");
            gen.writeArrayFieldStart("set");
            for (String value : expression.getValues()) {
                gen.writeString(value);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

        // End match
        gen.writeEndObject();
        gen.writeEndObject();
    }

    private static void parseCtExpression(JsonGenerator gen, CtExpression expression) throws IOException {
        Operation operation = expression.getOperation();
        CtExpression.Field field = expression.getField();

        if (operation != null) {
            gen.writeStartObject();
            // Start match
            gen.writeObjectFieldStart("match");
            // Operation
            gen.writeStringField("op", operation.getValue());

            // Left
            gen.writeObjectFieldStart("left");
            gen.writeObjectFieldStart("ct");
            if (expression.getStates() != null) {
                gen.writeStringField("key", CtExpression.Field.STATE.getValue());
            } else if (expression.getStatuses() != null) {
                gen.writeStringField("key", CtExpression.Field.STATUS.getValue());
            } else {
                if (expression.getType() != null) {
                    gen.writeStringField("key", expression.getType().getValue());
                } else {
                    gen.writeStringField("key", field.getValue());
                }
            }
            if (expression.getOriginalType() != null) {
                gen.writeStringField("dir", expression.getOriginalType() ? "original" : "reply");
            }
            gen.writeEndObject();
            gen.writeEndObject();

            // Right
            if (expression.getValues().size() == 1) {
                gen.writeStringField("right", expression.getValues().getFirst());
            } else {
                gen.writeObjectFieldStart("right");
                gen.writeArrayFieldStart("set");
                for (String value : expression.getValues()) {
                    gen.writeString(value);
                }
                gen.writeEndArray();
                gen.writeEndObject();
            }

            // End match
            gen.writeEndObject();
            gen.writeEndObject();


        } else {
            gen.writeStartObject();
            gen.writeObjectFieldStart("ct count");
            gen.writeNumberField("val", expression.getValue());
            if (expression.getOver()) {
                gen.writeBooleanField("inv", expression.getOver());
            }
            gen.writeEndObject();
            gen.writeEndObject();
        }
    }
}
