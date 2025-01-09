package ir.moke.jsysbox.firewall.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.model.*;

import java.io.IOException;

public class ChainDeserializer extends JsonDeserializer<Chain> {

    @Override
    public Chain deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode chainNode = parser.readValueAsTree();

        String tableName = chainNode.get("table").asText();
        TableType tableType = TableType.fromValue(chainNode.get("family").asText());
        String name = chainNode.get("name").asText();
        int handle = chainNode.get("handle").asInt();
        ChainType type = chainNode.has("type") ? ChainType.fromValue(chainNode.get("type").asText()) : null;
        ChainHook hook = chainNode.has("hook") ? ChainHook.fromValue(chainNode.get("hook").asText()) : null;
        Integer priority = chainNode.has("prio") ? chainNode.get("prio").asInt() : null;
        ChainPolicy policy = chainNode.has("hook") ? ChainPolicy.fromValue(chainNode.get("policy").asText()) : null;

        Table table = JFirewall.table(tableName, tableType);

        return new Chain(table, name, handle, type, hook, priority, policy);
    }
}
