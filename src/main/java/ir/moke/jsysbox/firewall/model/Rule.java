package ir.moke.jsysbox.firewall.model;

import java.util.List;
import java.util.Map;

public class Rule {
    private Chain chain;
    private int handle;
    private List<Map<String, Object>> expr;
    private String comment;

    public Rule() {
    }

    public Rule(Chain chain, List<Map<String, Object>> expr, String comment, int handle) {
        this.chain = chain;
        this.handle = handle;
        this.expr = expr;
        this.comment = comment;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public List<Map<String, Object>> getExpr() {
        return expr;
    }

    public void setExpr(List<Map<String, Object>> expr) {
        this.expr = expr;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "chain=" + chain +
                ", handle=" + handle +
                ", expr=" + expr +
                ", comment='" + comment + '\'' +
                '}';
    }
}
