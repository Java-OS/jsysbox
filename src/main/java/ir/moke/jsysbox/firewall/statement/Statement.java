package ir.moke.jsysbox.firewall.statement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.utils.json.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface Statement extends Serializable {

    static Statement getStatement(JsonNode jsonNode) {
        try {
            if (jsonNode.has("log")) {
                boolean hasLevel = jsonNode.get("log").has("level");
                boolean hasPrefix = jsonNode.get("log").has("prefix");

                LogStatement.LogLevel level = hasLevel ? LogStatement.LogLevel.valueOf(jsonNode.get("log").get("level").asText().toUpperCase()) : null;
                String prefix = hasPrefix ? jsonNode.get("log").get("prefix").asText() : null;

                return new LogStatement(level, prefix);
            } else if (jsonNode.has("limit")) {
                Long rate = jsonNode.get("limit").has("rate") ? jsonNode.get("limit").get("rate").asLong() : null;
                Long burst = jsonNode.get("limit").has("burst") ? jsonNode.get("limit").get("burst").asLong() : null;
                LimitStatement.TimeUnit timeUnit = jsonNode.get("limit").has("per") ? LimitStatement.TimeUnit.valueOf(jsonNode.get("limit").get("per").asText().toUpperCase()) : null;
                Boolean isOver = jsonNode.get("limit").has("inv") ? jsonNode.get("limit").get("inv").asBoolean() : null;
                LimitStatement.ByteUnit rateUnit = jsonNode.get("limit").has("rate_unit") ? LimitStatement.ByteUnit.valueOf(jsonNode.get("limit").get("rate_unit").asText().toUpperCase()) : null;
                LimitStatement.ByteUnit burstUnit = jsonNode.get("limit").has("burst_unit") ? LimitStatement.ByteUnit.valueOf(jsonNode.get("limit").get("burst_unit").asText().toUpperCase()) : null;
                if (rateUnit == null) {
                    return new LimitStatement(rate, timeUnit, isOver);
                } else if (burst == null) {
                    return new LimitStatement(rate, timeUnit, rateUnit, isOver);
                } else {
                    return new LimitStatement(rate, timeUnit, rateUnit, isOver, burst, burstUnit);
                }
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
                Integer port = jsonNode.get("snat").has("port") && !JsonUtils.isNullOrEmpty(jsonNode.get("snat").get("port")) ? jsonNode.get("snat").get("port").asInt() : null;
                List<NatStatement.Flag> flags = new ArrayList<>();
                if (jsonNode.get("snat").has("flags")) {
                    ArrayNode arr = (ArrayNode) jsonNode.get("snat").get("flags");
                    arr.forEach(item -> flags.add(NatStatement.Flag.fromValue(item.asText())));
                }
                return new NatStatement(NatStatement.Type.SNAT, addr, port, flags);
            } else if (jsonNode.has("dnat")) {
                String addr = jsonNode.get("dnat").get("addr").asText();
                Integer port = jsonNode.get("dnat").has("port") && !JsonUtils.isNullOrEmpty(jsonNode.get("dnat").get("port")) ? jsonNode.get("dnat").get("port").asInt() : null;
                List<NatStatement.Flag> flags = new ArrayList<>();
                if (jsonNode.get("dnat").has("flags")) {
                    ArrayNode arr = (ArrayNode) jsonNode.get("dnat").get("flags");
                    arr.forEach(item -> flags.add(NatStatement.Flag.fromValue(item.asText())));
                }
                return new NatStatement(NatStatement.Type.DNAT, addr, port, flags);
            } else if (jsonNode.has("redirect")) {
                Integer port = jsonNode.get("redirect").get("port").asInt();
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
                RejectStatement.Reason reason;
                if (type.equals("tcp reset")) {
                    reason = RejectStatement.Reason.TCP_RESET;
                } else {
                    reason = RejectStatement.Reason.fromValue(jsonNode.get("reject").get("expr").asText().toLowerCase());
                }
                return new RejectStatement(reason);
            }
            return null;
        } catch (Exception e) {
            throw new JSysboxException("Failed to parse statement '%s'".formatted(jsonNode.toString()), e);
        }
    }

}
