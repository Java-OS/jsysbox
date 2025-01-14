package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.moke.jsysbox.firewall.config.deserializer.SetDeserializer;
import ir.moke.jsysbox.firewall.config.serializer.SetSerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = SetDeserializer.class)
@JsonSerialize(using = SetSerializer.class)
public class Set implements Serializable {
    private List<String> elements = new ArrayList<>();
    private List<FlagType> flags = new ArrayList<>();
    private Table table;
    private String name;
    private SetType type;
    private int handle;
    private Integer size;
    private Integer timeout;
    private Integer gcInterval;
    private String comment;
    private SetPolicy policy;

    public Set() {
    }

    public Set(List<String> elements, List<FlagType> flags, Table table, String name, SetType type, int handle, Integer size, Integer timeout, Integer gcInterval, SetPolicy policy, String comment) {
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

    public Integer getSize() {
        return size;
    }

    public List<FlagType> getFlags() {
        return flags;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public List<String> getElements() {
        return elements;
    }

    public Table getTable() {
        return table;
    }

    public String getComment() {
        return comment;
    }

    public Integer getGcInterval() {
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
