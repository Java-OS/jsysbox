package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.firewall.model.Chain;

import java.io.IOException;

public class ChainSerializer extends JsonSerializer<Chain> {

    @Override
    public void serialize(Chain chain, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("chain");
        gen.writeStringField("family", chain.getTable().getType().getValue());
        gen.writeStringField("table", chain.getTable().getName());
        gen.writeStringField("name", chain.getName());
        gen.writeNumberField("handle", chain.getHandle());
        gen.writeStringField("type", chain.getType().getValue());
        gen.writeStringField("hook", chain.getHook().getValue());
        gen.writeNumberField("prio", chain.getPriority());
        gen.writeStringField("policy", chain.getPolicy().getValue());
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
