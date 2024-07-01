package ir.moke.jsysbox.firewall;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import ir.moke.jsysbox.firewall.model.Table;

import java.io.IOException;

public class TableDeserializer extends JsonDeserializer<Table> {

    @Override
    public Table deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        TreeNode treeNode = parser.getCodec().readTree(parser);
        return JFirewall.table(((TextNode) treeNode).asText());
    }
}
