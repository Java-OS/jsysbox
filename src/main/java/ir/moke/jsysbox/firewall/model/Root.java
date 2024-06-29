package ir.moke.jsysbox.firewall.model;

import java.util.List;

public class Root {
    private List<Nftable> nftables;

    public List<Nftable> getNftables() {
        return nftables;
    }

    public void setNftables(List<Nftable> nftables) {
        this.nftables = nftables;
    }
}
