package ir.moke.jsysbox.firewall.model;

public class Chain {
    private TableType family;
    private String table;
    private String name;
    private int handle;
    private ChainType type;
    private ChainHook hook;
    private int prio;
    private ChainPolicy policy;

    public TableType getFamily() {
        return family;
    }

    public void setFamily(TableType family) {
        this.family = family;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
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

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public ChainType getType() {
        return type;
    }

    public void setType(ChainType type) {
        this.type = type;
    }

    public ChainHook getHook() {
        return hook;
    }

    public void setHook(ChainHook hook) {
        this.hook = hook;
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }

    public ChainPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(ChainPolicy policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        return "Chain{" +
                "family='" + family + '\'' +
                ", table='" + table + '\'' +
                ", name='" + name + '\'' +
                ", handle=" + handle +
                ", type=" + type +
                ", hook=" + hook +
                ", prio=" + prio +
                ", policy=" + policy +
                '}';
    }
}
