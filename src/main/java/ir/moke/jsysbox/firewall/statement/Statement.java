package ir.moke.jsysbox.firewall.statement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.moke.jsysbox.JSysboxException;

import java.util.ArrayList;
import java.util.List;

public interface Statement {

    static Statement getStatement(JsonNode jsonNode) {
        try {
            if (jsonNode.has("limit")) {
                Long rate = jsonNode.has("rate") ? jsonNode.get("rate").asLong() : null;
                LimitStatement.TimeUnit timeUnit = jsonNode.has("per") ? LimitStatement.TimeUnit.valueOf(jsonNode.get("per").asText().toUpperCase()) : null;
                Boolean isOver = jsonNode.has("inv") ? jsonNode.get("inv").asBoolean() : null;
                LimitStatement.ByteUnit byteUnit = jsonNode.has("rate_unit") ? LimitStatement.ByteUnit.valueOf(jsonNode.get("rate_unit").asText().toUpperCase()) : null;
                if (byteUnit != null) return new LimitStatement(rate, timeUnit, byteUnit, isOver);
                return new LimitStatement(rate, timeUnit, isOver);
            } else if (jsonNode.has("drop")) {
                return new VerdictStatement(VerdictStatement.Type.DROP);
            } else if (jsonNode.has("accept")) {
                return new VerdictStatement(VerdictStatement.Type.ACCEPT);
            } else if (jsonNode.has("queue")) {
                return new VerdictStatement(VerdictStatement.Type.QUEUE);
            } else if (jsonNode.has("continue")) {
                return new VerdictStatement(VerdictStatement.Type.CONTINUE);
            } else if (jsonNode.has("return")) {
                return new VerdictStatement(VerdictStatement.Type.RETURN);
            } else if (jsonNode.has("jump")) {
                String target = jsonNode.get("jump").get("target").asText();
                return new VerdictStatement(VerdictStatement.Type.JUMP, target);
            } else if (jsonNode.has("goto")) {
                String target = jsonNode.get("goto").get("target").asText();
                return new VerdictStatement(VerdictStatement.Type.GOTO, target);
            } else if (jsonNode.has("snat")) {
                String addr = jsonNode.get("snat").get("addr").asText();
                int port = jsonNode.get("snat").get("port").asInt();
                List<NatStatement.Flag> flags = new ArrayList<>();
                if (jsonNode.get("snat").has("flags")) {
                    ArrayNode arr = (ArrayNode) jsonNode.get("snat").get("flags");
                    arr.forEach(item -> flags.add(NatStatement.Flag.fromValue(item.asText())));
                }
                return new NatStatement(NatStatement.Type.SNAT, addr, port, flags);
            } else if (jsonNode.has("dnat")) {
                String addr = jsonNode.get("dnat").get("addr").asText();
                int port = jsonNode.get("dnat").get("port").asInt();
                List<NatStatement.Flag> flags = new ArrayList<>();
                if (jsonNode.get("dnat").has("flags")) {
                    ArrayNode arr = (ArrayNode) jsonNode.get("dnat").get("flags");
                    arr.forEach(item -> flags.add(NatStatement.Flag.fromValue(item.asText())));
                }
                return new NatStatement(NatStatement.Type.DNAT, addr, port, flags);
            } else if (jsonNode.has("redirect")) {
                int port = jsonNode.get("redirect").get("port").asInt();
                List<NatStatement.Flag> flags = new ArrayList<>();
                if (jsonNode.get("redirect").has("flags")) {
                    ArrayNode arr = (ArrayNode) jsonNode.get("redirect").get("flags");
                    arr.forEach(item -> flags.add(NatStatement.Flag.fromValue(item.asText())));
                }
                return new NatStatement(NatStatement.Type.REDIRECT, port, flags);
            } else if (jsonNode.has("masquerade")) {
                List<NatStatement.Flag> flags = new ArrayList<>();
                if (jsonNode.get("masquerade").has("flags")) {
                    ArrayNode arr = (ArrayNode) jsonNode.get("masquerade").get("flags");
                    arr.forEach(item -> flags.add(NatStatement.Flag.fromValue(item.asText())));
                }
                return new NatStatement(flags);
            } else if (jsonNode.has("counter")) {
                long packets = jsonNode.get("counter").get("packets").asLong();
                long bytes = jsonNode.get("counter").get("bytes").asLong();
                return new CounterStatement(packets, bytes);
            } else if (jsonNode.has("reject")) {
                String type = jsonNode.get("reject").get("type").asText();
                String expr = jsonNode.get("reject").get("expr").asText();
                return new RejectStatement(RejectStatement.Type.fromValue(type.toLowerCase()), RejectStatement.Reason.fromValue(expr.toLowerCase()));
            }
            return null;
        } catch (Exception e) {
            throw new JSysboxException("Failed to parse statement '%s'".formatted(jsonNode.toString()), e);
        }
    }

}
