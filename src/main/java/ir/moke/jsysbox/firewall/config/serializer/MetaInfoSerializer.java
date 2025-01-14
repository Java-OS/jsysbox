package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.MetaInfo;

import java.io.IOException;

public class MetaInfoSerializer extends JsonSerializer<MetaInfo> {

    @Override
    public void serialize(MetaInfo metaInfo, JsonGenerator gen, SerializerProvider serializers) {
        try {
            gen.writeStartObject();
            gen.writeObjectFieldStart("metainfo");
            gen.writeStringField("version", metaInfo.getVersion());
            gen.writeStringField("release_name", metaInfo.getReleaseName());
            gen.writeStringField("json_schema_version", metaInfo.getJsonSchemaVersion());
            gen.writeEndObject();
            gen.writeEndObject();
        } catch (Exception e) {
            throw new JSysboxException("Failed to serialize metaInfo: %s".formatted(metaInfo.toString()), e);
        }
    }
}
