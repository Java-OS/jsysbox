package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.moke.jsysbox.firewall.config.SetDeserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonDeserialize(using = SetDeserializer.class)
public class Set {
    private List<Map<String, Object>> elements = new ArrayList<>();
    private List<FlagType> flags = new ArrayList<>();
    private Table table;
    private String name;
    private SetType type;
    private int handle;
    private int size;
    private int timeout;
    private int gcInterval;
    private String comment;
    private SetPolicy policy;

    public Set() {
    }

    public Set(List<Map<String, Object>> elements, List<FlagType> flags, Table table, String name, SetType type, int handle, int size, int timeout, int gcInterval, SetPolicy policy, String comment) {
        this.elements = elements;
        this.flags = flags;
        this.table = table;
        this.name = name;
        this.type = type;
        this.handle = handle;
        this.size = size;
        this.timeout = timeout;
        this.gcInterval = gcInterval;
        this.comment = comment;
        this.policy = policy;
    }

    public String getName() {
        return name;
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

    public List<Map<String, Object>> getElements() {
        return elements;
    }

    public Table getTable() {
        return table;
    }

    public String getComment() {
        return comment;
    }

    public int getGcInterval() {
        return gcInterval;
    }

    public SetPolicy getPolicy() {
        return policy;
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
