package ir.moke.jsysbox.firewall.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.model.Chain;
import ir.moke.jsysbox.firewall.model.Rule;
import ir.moke.jsysbox.firewall.model.Table;
import ir.moke.jsysbox.firewall.model.TableType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RuleDeserializer extends JsonDeserializer<Rule> {
    private static final TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
    };

    private static List<Map<String, Object>> getExpressions(JsonParser parser, JsonNode jsonNode) throws IOException {
        if (!jsonNode.has("expr")) return Collections.emptyList();
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return mapper.readValue(jsonNode.get("expr").traverse(), typeReference);
    }

    @Override
    public Rule deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.readValueAsTree();
        String tableType = jsonNode.get("family").asText();
        String tableName = jsonNode.get("table").asText();
        String chainName = jsonNode.get("chain").asText();
        int handle = jsonNode.get("handle").asInt();
        String comment = jsonNode.get("comment").asText();

        Table table = JFirewall.table(tableName, TableType.fromValue(tableType));
        Chain chain = JFirewall.chain(table, chainName);

        List<Map<String, Object>> expressions = getExpressions(parser, jsonNode);

        List<Expression> expList = new ArrayList<>();
        ArrayNode exprArr = (ArrayNode) jsonNode.get("expr");
        int size = exprArr.size();
        for (int i = 0; i < size - 2; i++) {
            JsonNode node = exprArr.get(i);
            Expression expression = Expression.getExpression(node);
            expList.add(expression);
        }

        System.out.println(">>>> : " + expList);


        return new Rule(chain, expressions, comment, handle);
    }
}
