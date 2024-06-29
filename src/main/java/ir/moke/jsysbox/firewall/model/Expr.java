package ir.moke.jsysbox.firewall.model;

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

    public Object getCu() {
        return cu;
    }

    public void setCu(Object cu) {
        this.cu = cu;
    }

    public Object getRet() {
        return ret;
    }

    public void setRet(Object ret) {
        this.ret = ret;
    }

    public Object getJump() {
        return jump;
    }

    public void setJump(Object jump) {
        this.jump = jump;
    }

    public Object getGo() {
        return go;
    }

    public void setGo(Object go) {
        this.go = go;
    }
}
