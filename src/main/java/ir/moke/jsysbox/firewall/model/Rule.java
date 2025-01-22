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
    private List<Expression> expressions;
    private String comment;
    private List<Statement> statements;

    public Rule() {
    }

    public Rule(Chain chain, List<Expression> expressions, List<Statement> statements, String comment, int handle) {
        this.chain = chain;
        this.handle = handle;
        this.expressions = expressions;
        this.comment = comment;
        this.statements = statements;
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

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "chain=" + chain +
                ", handle=" + handle +
                ", expr=" + expressions +
                ", comment='" + comment + '\'' +
                '}';
    }
}
