package ir.moke.jsysbox.firewall.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.model.Chain;
import ir.moke.jsysbox.firewall.model.Rule;
import ir.moke.jsysbox.firewall.model.Table;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RuleDeserializer extends JsonDeserializer<Rule> {
    private static final TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
    };

    private static List<Map<String, Object>> getExpressions(JsonParser parser, JsonNode jsonNode) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return mapper.readValue(jsonNode.get("expr").traverse(), typeReference);
    }

    @Override
    public Rule deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.readValueAsTree();
        String tableName = jsonNode.get("table").asText();
        String chainName = jsonNode.get("chain").asText();
        String comment = jsonNode.get("comment").asText();
        int handle = jsonNode.get("handle").asInt();

        Table table = JFirewall.table(tableName);
        Chain chain = JFirewall.chain(table, chainName);

        List<Map<String, Object>> expressions = getExpressions(parser, jsonNode);

        return new Rule(chain, expressions, comment, handle);
    }
}
