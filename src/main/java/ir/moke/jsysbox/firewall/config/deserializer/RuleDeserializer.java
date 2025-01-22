package ir.moke.jsysbox.firewall.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.model.Chain;
import ir.moke.jsysbox.firewall.model.Rule;
import ir.moke.jsysbox.firewall.model.Table;
import ir.moke.jsysbox.firewall.model.TableType;
import ir.moke.jsysbox.firewall.statement.Statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RuleDeserializer extends JsonDeserializer<Rule> {

    private static List<Expression> parseExpressions(JsonNode jsonNode) {
        List<Expression> expList = new ArrayList<>();
        ArrayNode exprArr = (ArrayNode) jsonNode.get("expr");
        int size = exprArr.size();
        for (int i = 0; i < size; i++) {
            JsonNode node = exprArr.get(i);
            if (node.has("match")) {
                Expression expression = Expression.getExpression(node);
                expList.add(expression);
            }
        }
        return expList;
    }

    private static List<Statement> parseStatement(JsonNode jsonNode) {
        List<Statement> sttList = new ArrayList<>();
        ArrayNode exprArr = (ArrayNode) jsonNode.get("expr");
        int size = exprArr.size();
        for (int i = 0; i < size; i++) {
            JsonNode node = exprArr.get(i);
            if (!node.has("match")) {
                Statement statement = Statement.getStatement(node);
                sttList.add(statement);
            }
        }
        return sttList;
    }

    @Override
    public Rule deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.readValueAsTree();
        String tableType = jsonNode.get("family").asText();
        String tableName = jsonNode.get("table").asText();
        String chainName = jsonNode.get("chain").asText();
        int handle = jsonNode.get("handle").asInt();
        String comment = (jsonNode.get("comment") != null || !jsonNode.get("comment").isEmpty()) ? jsonNode.get("comment").asText() : null;

        Table table = JFirewall.table(tableName, TableType.fromValue(tableType));
        Chain chain = JFirewall.chain(table, chainName);

        List<Expression> expList = parseExpressions(jsonNode);
        List<Statement> sttList = parseStatement(jsonNode);

        return new Rule(chain, expList, sttList, comment, handle);
    }
}
