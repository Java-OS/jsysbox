package ir.moke.jsysbox.firewall.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SetDeserializer extends JsonDeserializer<Set> {
    private static final TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
    };

    private static List<Map<String, Object>> getElements(JsonParser parser, JsonNode jsonNode) throws IOException {
        if (!jsonNode.has("elem")) return Collections.emptyList();
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return mapper.readValue(jsonNode.get("elem").traverse(), typeReference);
    }

    @Override
    public Set deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.readValueAsTree();
        String tableType = jsonNode.get("family").asText();
        String name = jsonNode.get("name").asText();
        String tableName = jsonNode.get("table").asText();
        SetType type = SetType.fromValue(jsonNode.get("type").asText());
        int handle = jsonNode.get("handle").asInt();
        String comment = jsonNode.get("comment").asText();
        int size = jsonNode.get("size").asInt();
        JsonNode flagsNode = jsonNode.get("flags");
        int timeout = jsonNode.get("timeout").asInt();
        int gcInterval = jsonNode.get("gc-interval").asInt();
        SetPolicy policy = SetPolicy.fromValue(jsonNode.get("policy").asText());

        List<FlagType> flagTypes = new ArrayList<>();
        if (flagsNode != null && flagsNode.isArray()) {
            for (JsonNode flagNode : flagsNode) {
                flagTypes.add(FlagType.fromValue(flagNode.asText()));
            }
        }

        List<Map<String, Object>> elements = getElements(parser, jsonNode);

        Table table = JFirewall.table(tableName, TableType.fromValue(tableType));

        return new Set(elements, flagTypes, table, name, type, handle, size, timeout, gcInterval, policy, comment);
    }
}
