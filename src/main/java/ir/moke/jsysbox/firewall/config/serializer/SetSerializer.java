package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.FlagType;
import ir.moke.jsysbox.firewall.model.Set;

import java.io.IOException;

public class SetSerializer extends JsonSerializer<Set> {

    @Override
    public void serialize(Set set, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeStartObject();
            gen.writeObjectFieldStart("set");
            gen.writeStringField("family", set.getTable().getType().getValue());
            gen.writeStringField("table", set.getTable().getName());
            gen.writeStringField("name", set.getName());
            gen.writeStringField("type", set.getType().getValue());
            gen.writeNumberField("handle", set.getHandle());
            if (set.getTimeout() != null) gen.writeNumberField("timeout", set.getTimeout());
            if (set.getGcInterval() != null) gen.writeNumberField("gc-interval", set.getGcInterval());
            if (set.getSize() != null) gen.writeNumberField("size", set.getSize());
            if (set.getComment() != null) gen.writeStringField("comment", set.getComment());
            if (set.getPolicy() != null) gen.writeStringField("policy", set.getPolicy().getValue());

            if (set.getFlags() != null) {
                // > serialize flags
                gen.writeArrayFieldStart("flags");
                for (FlagType flag : set.getFlags()) {
                    gen.writeString(flag.getValue());
                }
                gen.writeEndArray();
                // < End
            }

            if (set.getElements() != null) {
                // > serialize elements
                gen.writeArrayFieldStart("elem");
                for (String item : set.getElements()) {
                    gen.writeString(item);
                }
                gen.writeEndArray();
                // < End
            }

            gen.writeEndObject();
            gen.writeEndObject();
        } catch (Exception e) {
            throw new JSysboxException("Failed to serialize set: %s".formatted(set.toString()), e);
        }
    }
}
