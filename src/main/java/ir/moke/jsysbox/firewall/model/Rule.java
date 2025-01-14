package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.moke.jsysbox.firewall.config.deserializer.RuleDeserializer;
import ir.moke.jsysbox.firewall.config.serializer.RuleSerializer;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.statement.Statement;

import java.io.Serializable;
import java.util.List;

@JsonDeserialize(using = RuleDeserializer.class)
@JsonSerialize(using = RuleSerializer.class)
public class Rule implements Serializable {
    private Chain chain;
    private int handle;
    private List<Expression> expr;
    private String comment;
    private Statement statement;

    public Rule() {
    }

    public Rule(Chain chain, List<Expression> expr, Statement statement, String comment, int handle) {
        this.chain = chain;
        this.handle = handle;
        this.expr = expr;
        this.comment = comment;
        this.statement = statement;
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

    public List<Expression> getExpr() {
        return expr;
    }

    public void setExpr(List<Expression> expr) {
        this.expr = expr;
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
