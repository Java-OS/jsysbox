package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.Table;

import java.io.IOException;

public class TableSerializer extends JsonSerializer<Table> {

    @Override
    public void serialize(Table table, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeStartObject();
            gen.writeObjectFieldStart("table");
            gen.writeStringField("family", table.getType().getValue());
            gen.writeStringField("name", table.getName());
            gen.writeNumberField("handle", table.getHandle());
            gen.writeEndObject();
            gen.writeEndObject();
        } catch (Exception e) {
            throw new JSysboxException("Failed to serialize table: %s".formatted(table.toString()), e);
        }
    }
}
