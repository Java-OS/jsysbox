package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return type == table.type && Objects.equals(name, table.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}
