package ir.moke.jsysbox.firewall.model;

import java.util.List;

public class Set {
    private AddressFamily family;
    private String name;
    private String table;
    private SetType type;
    private int handle;
    private int size;
    private List<FlagType> flags;
    private int timeout;
    private List<Elem> elem;

    public AddressFamily getFamily() {
        return family;
    }

    public void setFamily(AddressFamily family) {
        this.family = family;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public SetType getType() {
        return type;
    }

    public void setType(SetType type) {
        this.type = type;
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<FlagType> getFlags() {
        return flags;
    }

    public void setFlags(List<FlagType> flags) {
        this.flags = flags;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<Elem> getElem() {
        return elem;
    }

    public void setElem(List<Elem> elem) {
        this.elem = elem;
    }
}
