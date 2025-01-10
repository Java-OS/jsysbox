package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.firewall.model.FlagType;
import ir.moke.jsysbox.firewall.model.Set;

import java.io.IOException;

public class SetSerializer extends JsonSerializer<Set> {

    @Override
    public void serialize(Set set, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("chain");
        gen.writeStringField("family", set.getTable().getType().getValue());
        gen.writeStringField("table", set.getTable().getName());
        gen.writeStringField("name", set.getName());
        gen.writeStringField("type", set.getType().getValue());
        gen.writeNumberField("handle", set.getHandle());
        gen.writeStringField("policy", set.getPolicy().getValue());
        gen.writeNumberField("size", set.getSize());
        gen.writeNumberField("timeout", set.getTimeout());
        gen.writeNumberField("gc-interval", set.getGcInterval());
        gen.writeStringField("comment", set.getComment());

        // > serialize flags
        gen.writeArrayFieldStart("flags");
        for (FlagType flag : set.getFlags()) {
            gen.writeString(flag.getValue());
        }
        gen.writeEndArray();
        // < End

        // > serialize elements
        gen.writeArrayFieldStart("elem");
        for (String item : set.getElements()) {
            gen.writeString(item);
        }
        gen.writeEndArray();
        // < End

        gen.writeEndObject();
        gen.writeEndObject();
    }
}
