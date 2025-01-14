package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.Chain;

import java.io.IOException;

public class ChainSerializer extends JsonSerializer<Chain> {

    @Override
    public void serialize(Chain chain, JsonGenerator gen, SerializerProvider serializers) {
        try {
            gen.writeStartObject();
            gen.writeObjectFieldStart("chain");
            gen.writeStringField("family", chain.getTable().getType().getValue());
            gen.writeStringField("table", chain.getTable().getName());
            gen.writeStringField("name", chain.getName());
            gen.writeNumberField("handle", chain.getHandle());
            if (chain.getType() != null) gen.writeStringField("type", chain.getType().getValue());
            if (chain.getHook() != null) gen.writeStringField("hook", chain.getHook().getValue());
            if (chain.getPriority() != null) gen.writeNumberField("prio", chain.getPriority());
            if (chain.getPolicy() != null) gen.writeStringField("policy", chain.getPolicy().getValue());
            gen.writeEndObject();
            gen.writeEndObject();
        } catch (Exception e) {
            throw new JSysboxException("Failed to serialize chain: %s".formatted(chain.toString()), e);
        }
    }
}
