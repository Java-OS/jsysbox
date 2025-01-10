package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.moke.jsysbox.firewall.config.deserializer.ChainDeserializer;

import java.util.Objects;

@JsonDeserialize(using = ChainDeserializer.class)
public class Chain {
    private Table table;
    private String name;
    private int handle;
    private ChainType type;
    private ChainHook hook;
    private Integer priority;
    private ChainPolicy policy;

    public Chain() {
    }

    public Chain(Table table, String name, int handle) {
        this.table = table;
        this.name = name;
        this.handle = handle;
    }

    public Chain(Table table, String name, int handle, ChainType type, ChainHook hook, Integer priority, ChainPolicy policy) {
        this.table = table;
        this.name = name;
        this.handle = handle;
        this.type = type;
        this.hook = hook;
        this.priority = priority;
        this.policy = policy;
    }

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

    public Integer getPriority() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chain chain = (Chain) o;
        return handle == chain.handle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }
}
