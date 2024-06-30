package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Expr {
    private Match match;
    private Object accept;
    private Object drop;
    private Object queue;
    private Object cu;
    private Object ret;
    private Object jump;
    private Object go;

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Object getAccept() {
        return accept;
    }

    public void setAccept(Object accept) {
        this.accept = accept;
    }

    public Object getDrop() {
        return drop;
    }

    public void setDrop(Object drop) {
        this.drop = drop;
    }

    public Object getQueue() {
        return queue;
    }

    public void setQueue(Object queue) {
        this.queue = queue;
    }

    @JsonProperty("continue")
    public Object getContinue() {
        return cu;
    }

    public void setContinue(Object cu) {
        this.cu = cu;
    }

    @JsonProperty("return")
    public Object getReturn() {
        return ret;
    }

    public void setReturn(Object ret) {
        this.ret = ret;
    }

    @JsonProperty("jump")
    public Object getJump() {
        return jump;
    }

    public void setJump(Object jump) {
        this.jump = jump;
    }

    @JsonProperty("goto")
    public Object getGoto() {
        return go;
    }

    public void setGoto(Object go) {
        this.go = go;
    }
}
