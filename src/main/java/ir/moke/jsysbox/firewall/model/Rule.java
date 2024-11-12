package ir.moke.jsysbox.firewall.model;

import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.statement.Statement;

public class Rule {
    private Chain chain;
    private int handle;
    private Expression expression;
    private String comment;
    private Statement statement;

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

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
