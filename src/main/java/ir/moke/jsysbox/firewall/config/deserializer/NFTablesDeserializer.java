package ir.moke.jsysbox.firewall.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NFTablesDeserializer extends JsonDeserializer<NFTables> {

    @Override
    public NFTables deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.readValueAsTree();
        ArrayNode root = (ArrayNode) jsonNode.get("nftables");

        List<Table> tables = new ArrayList<>();
        List<Chain> chains = new ArrayList<>();
        List<Set> sets = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        MetaInfo metaInfo = null;

        for (JsonNode node : root) {
            if (node.has("metaInfo")) {
                metaInfo = JFirewall.metaInfo();
            }
            if (node.has("table")) {
                Table table = JFirewall.table(node.get("table").get("handle").asInt());
                tables.add(table);
            } else if (node.has("chain")) {
                Chain chain = JFirewall.chain(node.get("chain").get("handle").asInt());
                chains.add(chain);
            } else if (node.has("set")) {
                Set set = JFirewall.set(node.get("set").get("handle").asInt());
                sets.add(set);
            } else if (node.has("rule")) {
                Rule rule = JFirewall.rule(node.get("rule").get("handle").asInt());
                rules.add(rule);
            }

        }
        return new NFTables(metaInfo, tables, chains, rules, sets);
    }
}