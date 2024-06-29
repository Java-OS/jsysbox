package ir.moke.jsysbox.firewall.model;

public class Nftable {
    private Metainfo metainfo;
    private Table table;
    private Set set;
    private Chain chain;
    private Rule rule;

    public Metainfo getMetainfo() {
        return metainfo;
    }

    public void setMetainfo(Metainfo metainfo) {
        this.metainfo = metainfo;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
