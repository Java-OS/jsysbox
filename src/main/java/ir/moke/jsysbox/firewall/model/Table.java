package ir.moke.jsysbox.firewall.model;

public class Table {
    private TableType family;
    private String name;
    private int handle;

    public Table() {
    }

    public Table(TableType family, String name) {
        this.family = family;
        this.name = name;
    }

    public TableType getFamily() {
        return family;
    }

    public void setFamily(TableType family) {
        this.family = family;
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
                "family=" + family +
                ", name='" + name + '\'' +
                ", handle=" + handle +
                '}';
    }
}
