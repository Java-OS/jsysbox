package ir.moke.jsysbox.firewall.model;

public class Table {
    private AddressFamily family;
    private String name;
    private int handle;

    public Table() {
    }

    public Table(AddressFamily family, String name) {
        this.family = family;
        this.name = name;
    }

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

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }
}
