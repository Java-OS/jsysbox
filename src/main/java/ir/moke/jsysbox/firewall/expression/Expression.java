package ir.moke.jsysbox.firewall.expression;

import com.fasterxml.jackson.databind.JsonNode;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.Operation;

import java.util.ArrayList;
import java.util.List;

public interface Expression {
    static Expression getExpression(JsonNode node) {
        try {
            Operation operation = null;
            List<String> values = null;
            MatchType matchType = null;
            String field = null;
            Integer ctCount = null;
            String ctKey = null;
            boolean isOriginal = false;
            boolean isOver = false;

            if (node.has("match") && node.get("match").get("left").has("payload")) {
                operation = Operation.fromValue(node.get("match").get("op").asText());
                matchType = MatchType.fromValue(node.get("match").get("left").get("payload").get("protocol").asText());
                field = node.get("match").get("left").get("payload").get("field").asText();
                values = getRight(node);
            } else if (node.has("match") && node.get("match").get("left").has("ct")) {
                ctKey = node.get("match").get("left").get("ct").get("key").asText();
                if (node.get("match").get("left").get("ct").has("dir")) {
                    String dir = node.get("match").get("left").get("ct").get("dir").asText();
                    isOriginal = dir.equals("original");
                }
            } else if (node.has("ct count")) {
                ctCount = node.get("ct count").get("val").asInt();
                isOver = node.get("ct count").has("inv");
            } else {
                return null;
            }

            return switch (matchType) {
                case IP -> createIpExpression(field, operation, values);
                case IP6 -> createIp6Expression(field, operation, values);
                case TCP -> createTcpExpression(field, operation, values);
                case UDP -> createUdpExpression(field, operation, values);
                case UDPLITE -> createUdpLightExpression(field, operation, values);
                case SCTP -> createSctpExpression(field, operation, values);
                case DCCP -> createDccpExpression(field, operation, values);
                case AH -> createAhExpression(field, operation, values);
                case ESP -> createEspExpression(field, operation, values);
                case COMP -> createCompExpression(field, operation, values);
                case ICMP -> createIcmpExpression(field, operation, values);
                case ICMPV6 -> createIcmp6Expression(field, operation, values);
                case ETHER -> createEtherExpression(field, values, operation);
                case DST -> createDstExpression(field, operation, values);
                case FRAG -> createFragExpression(field, operation, values);
                case HBH -> createHbhExpression(field, operation, values);
                case MH -> createMhExpression(field, operation, values);
                case RT -> createRtExpression(field, operation, values);
                case VLAN -> createVlanExpression(field, operation, values);
                case ARP -> createArpExpression(field, operation, values);
                case CT -> createCtExpression(field, operation, values, isOver, ctCount, ctKey, isOriginal);
                case META -> createMetaExpression(field, operation, values);
                case null -> null;
            };
        } catch (Exception e) {
            throw new JSysboxException("Failed to parse expression '%s'".formatted(node.toString()), e);
        }
    }

    private static List<String> getRight(JsonNode node) {
        List<String> values = new ArrayList<>();
        JsonNode rightNode = node.get("match").get("right");
        if (rightNode.has("set")) {
            for (JsonNode item : rightNode.get("set")) {
                values.add(item.asText());
            }
        } else {
            values.add(rightNode.asText());
        }
        return values;
    }

    private static Expression createMetaExpression(String fieldValue, Operation operation, List<String> values) {
        return new MetaExpression(MetaExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createCtExpression(String fieldValue, Operation operation, List<String> values, boolean isOver, Integer count, String key, Boolean isOriginal) {
        if (count != null) {
            return new CtExpression(isOver, count);
        } else if (key != null) {
            return new CtExpression(isOriginal, CtExpression.Type.fromValue(key), values);
        } else {
            return new CtExpression(CtExpression.Field.fromValue(fieldValue), operation, values);
        }
    }

    private static Expression createArpExpression(String fieldValue, Operation operation, List<String> values) {
        ArpExpression.Field field = ArpExpression.Field.fromValue(fieldValue);
        if (field.equals(ArpExpression.Field.TYPE)) {
            return new ArpExpression(values.stream().map(ArpExpression.ArpOperation::fromValue).toList());
        } else {
            return new ArpExpression(field, operation, values);
        }
    }

    private static Expression createVlanExpression(String fieldValue, Operation operation, List<String> values) {
        return new VlanExpression(VlanExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createRtExpression(String fieldValue, Operation operation, List<String> values) {
        return new RtExpression(RtExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createMhExpression(String fieldValue, Operation operation, List<String> values) {
        return new MhExpression(MhExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createHbhExpression(String fieldValue, Operation operation, List<String> values) {
        return new HbhExpression(HbhExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createFragExpression(String fieldValue, Operation operation, List<String> values) {
        return new FragExpression(FragExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createDstExpression(String fieldValue, Operation operation, List<String> values) {
        return new DstExpression(DstExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createIcmpExpression(String fieldValue, Operation operation, List<String> values) {
        IcmpExpression.Field field = IcmpExpression.Field.fromValue(fieldValue);
        if (field.equals(IcmpExpression.Field.TYPE)) {
            return new IcmpExpression(values.stream().map(IcmpExpression.Type::fromValue).toList());
        } else {
            return new IcmpExpression(field, operation, values);
        }
    }

    private static Expression createIcmp6Expression(String fieldValue, Operation operation, List<String> values) {
        Icmpv6Expression.Field field = Icmpv6Expression.Field.fromValue(fieldValue);
        if (field.equals(Icmpv6Expression.Field.TYPE)) {
            return new Icmpv6Expression(values.stream().map(Icmpv6Expression.Type::fromValue).toList());
        } else {
            return new Icmpv6Expression(field, operation, values);
        }
    }

    private static Expression createCompExpression(String fieldValue, Operation operation, List<String> values) {
        return new CompExpression(CompExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createEtherExpression(String fieldValue, List<String> values, Operation operation) {
        EtherExpression.Field field = EtherExpression.Field.fromValue(fieldValue);
        if (field.equals(EtherExpression.Field.TYPE)) {
            return new EtherExpression(values.stream().map(EtherExpression.Type::fromValue).toList());
        } else {
            return new EtherExpression(field, operation, values);
        }
    }

    private static Expression createTcpExpression(String fieldValue, Operation operation, List<String> values) {
        return new TcpExpression(TcpExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createUdpExpression(String fieldValue, Operation operation, List<String> values) {
        return new UdpExpression(UdpExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createUdpLightExpression(String fieldValue, Operation operation, List<String> values) {
        return new UdpLightExpression(UdpLightExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createSctpExpression(String fieldValue, Operation operation, List<String> values) {
        return new SctpExpression(SctpExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createDccpExpression(String fieldValue, Operation operation, List<String> values) {
        DccpExpression.Field field = DccpExpression.Field.fromValue(fieldValue);
        if (field.equals(DccpExpression.Field.TYPE)) {
            return new DccpExpression(values.stream().map(DccpExpression.Type::fromValue).toList());
        } else {
            return new DccpExpression(field, operation, values);
        }
    }

    private static Expression createIpExpression(String fieldValue, Operation operation, List<String> values) {
        return new IpExpression(IpExpression.Field.fromValue(fieldValue), operation, values);
    }

    static Expression createIp6Expression(String field, Operation operation, List<String> values) {
        return new Ip6Expression(Ip6Expression.Field.fromValue(field), operation, values);
    }

    private static Expression createAhExpression(String fieldValue, Operation operation, List<String> values) {
        return new AhExpression(AhExpression.Field.fromValue(fieldValue), operation, values);
    }

    private static Expression createEspExpression(String fieldValue, Operation operation, List<String> values) {
        return new EspExpression(EspExpression.Field.fromValue(fieldValue), operation, values);
    }

    MatchType matchType();
}
