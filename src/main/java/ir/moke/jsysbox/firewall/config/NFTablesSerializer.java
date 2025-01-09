package ir.moke.jsysbox.firewall.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.firewall.model.NFTables;

import java.io.IOException;

public class NFTablesSerializer extends JsonSerializer<NFTables> {

    @Override
    public void serialize(NFTables nfTables, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    }
}