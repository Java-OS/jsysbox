package ir.moke.jsysbox.firewall.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.model.Table;

import java.io.IOException;

public class TableDeserializer extends JsonDeserializer<Table> {

    @Override
    public Table deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        TextNode textNode = parser.getCodec().readTree(parser);
        return JFirewall.table(textNode.asText());
    }
}
