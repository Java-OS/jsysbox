package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaInfo {
    private String version;
    @JsonProperty("release_name")
    private String releaseName ;
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
