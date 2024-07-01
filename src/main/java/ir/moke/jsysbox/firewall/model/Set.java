package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.moke.jsysbox.firewall.TableDeserializer;

import java.util.List;

public class Set {
    @JsonDeserialize(using = TableDeserializer.class)
    private Table table;
    private String name;
    private SetType type;
    private int handle;
    private int size;
    private List<FlagType> flags;
    private int timeout;
    @JsonProperty("gc-interval")
    private int gcInterval;
    private List<String> elements;

    public String getName() {
        return name;
    }

    public Table getTable() {
        return table;
    }

    public SetType getType() {
        return type;
    }

    public int getHandle() {
        return handle;
    }

    public int getSize() {
        return size;
    }

    public List<FlagType> getFlags() {
        return flags;
    }

    public int getTimeout() {
        return timeout;
    }

    public List<String> getElements() {
        return elements;
    }

    public int getGcInterval() {
        return gcInterval;
    }

    @Override
    public String toString() {
        return "Set{" +
                "table=" + table.toString() +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", handle=" + handle +
                ", size=" + size +
                ", flags=" + flags +
                ", timeout=" + timeout +
                ", gcInterval=" + gcInterval +
                ", elements=" + elements +
                '}';
    }
}
