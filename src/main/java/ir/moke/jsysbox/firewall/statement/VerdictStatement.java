package ir.moke.jsysbox.firewall.statement;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.model.Chain;

public class VerdictStatement implements Statement {
    private final VerdictStatement.Type type;
    private String chainName;

    public VerdictStatement(VerdictStatement.Type type) {
        this.type = type;
    }

    public VerdictStatement(VerdictStatement.Type type, String chainName) {
        this.type = type;
        this.chainName = chainName;
    }

    public VerdictStatement(VerdictStatement.Type type, Chain chain) {
        this.type = type;
        this.chainName = chain.getName();
    }

    public VerdictStatement.Type getType() {
        return type;
    }

    public String getChainName() {
        return chainName;
    }

    @Override
    public String toString() {
        String stt = type.name().toLowerCase();
        if (type.equals(VerdictStatement.Type.GOTO) || type.equals(VerdictStatement.Type.JUMP)) {
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
