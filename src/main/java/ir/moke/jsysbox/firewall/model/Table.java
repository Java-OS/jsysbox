package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Table {
    @JsonProperty("family")
    private TableType type;
    private String name;
    private int handle;

    public Table() {
    }

    public Table(TableType type, String name) {
        this.type = type;
        this.name = name;
    }

    public TableType getType() {
        return type;
    }

    public void setType(TableType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    @Override
    public String toString() {
        return "Table{" +
                "family=" + type +
                ", name='" + name + '\'' +
                ", handle=" + handle +
                '}';
    }
}
