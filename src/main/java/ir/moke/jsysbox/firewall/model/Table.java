package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.moke.jsysbox.firewall.config.serializer.TableSerializer;

import java.io.Serializable;
import java.util.Objects;

@JsonSerialize(using = TableSerializer.class)
public class Table implements Serializable {
    @JsonProperty("family")
    private TableType type;
    private String name;
    private int handle;

    public Table() {
    }

    public Table(TableType type, String name, int handle) {
        this.type = type;
        this.name = name;
        this.handle = handle;
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
        return (type == table.type && Objects.equals(name, table.name)) || (handle == table.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}
