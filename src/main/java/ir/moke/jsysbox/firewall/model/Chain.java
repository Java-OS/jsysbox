package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.moke.jsysbox.firewall.TableDeserializer;

public class Chain {
    @JsonDeserialize(using = TableDeserializer.class)
    private Table table;
    private String name;
    private int handle;
    private ChainType type;
    private ChainHook hook;
    @JsonProperty("prio")
    private int priority;
    private ChainPolicy policy;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
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

    public ChainType getType() {
        return type;
    }

    public ChainHook getHook() {
        return hook;
    }

    public int getPriority() {
        return priority;
    }

    public ChainPolicy getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
        return "Chain{" +
                "table=" + table.toString() +
                ", name='" + name + '\'' +
                ", handle=" + handle +
                ", type=" + type +
                ", hook=" + hook +
                ", priority=" + priority +
                ", policy=" + policy +
                '}';
    }
}
