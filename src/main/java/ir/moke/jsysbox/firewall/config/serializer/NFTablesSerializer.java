package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.*;

import java.io.IOException;

public class NFTablesSerializer extends JsonSerializer<NFTables> {

    @Override
    public void serialize(NFTables nfTables, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeStartObject(); // Start NFTables JSON object

            gen.writeArrayFieldStart("nftables");
            // Serialize MetaInfo
            gen.writeObject(nfTables.getMetaInfo());

            // Serialize Tables
            for (Table table : nfTables.getTables()) {
                gen.writeObject(table);
            }

            // Serialize Chains
            for (Chain chain : nfTables.getChains()) {
                gen.writeObject(chain);
            }

            // Serialize Sets
            for (Set set : nfTables.getSets()) {
                gen.writeObject(set);
            }

            // Serialize Rules
            for (Rule rule : nfTables.getRules()) {
                gen.writeObject(rule);
            }

            gen.writeEndArray(); // End NFTables
            gen.writeEndObject(); // End object
        } catch (Exception e) {
            throw new JSysboxException("Failed to serialize nftables: %s".formatted(e.getMessage()), e);
        }
    }
}
