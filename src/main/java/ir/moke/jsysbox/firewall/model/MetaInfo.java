package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.moke.jsysbox.firewall.config.serializer.MetaInfoSerializer;

import java.io.Serializable;

@JsonSerialize(using = MetaInfoSerializer.class)
public class MetaInfo implements Serializable {
    private String version;
    @JsonProperty("release_name")
    private String releaseName;
    @JsonProperty("json_schema_version")
    private String jsonSchemaVersion;

    public String getVersion() {
        return version;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getJsonSchemaVersion() {
        return jsonSchemaVersion;
    }
}
