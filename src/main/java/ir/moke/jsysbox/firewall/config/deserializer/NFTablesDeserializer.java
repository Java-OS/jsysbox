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
            if (node.has("metainfo")) {
                metaInfo = JFirewall.metaInfo();
            } else if (node.has("table")) {
                Table table = JFirewall.table(node.get("table").get("handle").asInt());
                tables.add(table);
            } else if (node.has("chain")) {
                String tableName = node.get("chain").get("table").asText();
                TableType tableType = TableType.fromValue(node.get("chain").get("family").asText());
                String chainName = node.get("chain").get("name").asText();
                Chain chain = JFirewall.chain(tableName, tableType, chainName);
                chains.add(chain);
            } else if (node.has("set")) {
                String tableName = node.get("set").get("table").asText();
                TableType tableType = TableType.fromValue(node.get("set").get("family").asText());
                String setName = node.get("set").get("name").asText();
                Set set = JFirewall.set(tableName, tableType, setName);
                sets.add(set);
            } else if (node.has("rule")) {
                String tableName = node.get("rule").get("table").asText();
                TableType tableType = TableType.fromValue(node.get("rule").get("family").asText());
                String chainName = node.get("rule").get("chain").asText();
                Chain chain = JFirewall.chain(tableName, tableType, chainName);
                int ruleHandle = node.get("rule").get("handle").asInt();
                Rule rule = JFirewall.rule(chain, ruleHandle);
                rules.add(rule);
            }

        }
        return new NFTables(metaInfo, tables, chains, rules, sets);
    }
}