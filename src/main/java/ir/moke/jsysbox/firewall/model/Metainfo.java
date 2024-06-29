package ir.moke.jsysbox.firewall.model;

public class Metainfo {
    private String version;
    private String release_name;
    private int json_schema_version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease_name() {
        return release_name;
    }

    public void setRelease_name(String release_name) {
        this.release_name = release_name;
    }

    public int getJson_schema_version() {
        return json_schema_version;
    }

    public void setJson_schema_version(int json_schema_version) {
        this.json_schema_version = json_schema_version;
    }
}
