package ir.moke.jsysbox.firewall.statement;

import ir.moke.jsysbox.JSysboxException;

public class VerdictStatement implements Statement {
    private final Type type;
    private String chainName;

    public VerdictStatement(Type type) {
        this.type = type;
    }

    public VerdictStatement(Type type, String chainName) {
        this.type = type;
        this.chainName = chainName;
    }

    public Type getType() {
        return type;
    }

    public String getChainName() {
        return chainName;
    }

    @Override
    public String toString() {
        String stt = type.name().toLowerCase();
        if (type.equals(Type.GOTO) || type.equals(Type.JUMP)) {
            if (chainName == null || chainName.isEmpty()) throw new JSysboxException("target chain could not be empty");
            return stt + " " + chainName;
        }
        return stt;
    }

    public enum Type {
        ACCEPT,
        DROP,
        QUEUE,
        CONTINUE,
        RETURN,
        JUMP,
        GOTO
    }
}
