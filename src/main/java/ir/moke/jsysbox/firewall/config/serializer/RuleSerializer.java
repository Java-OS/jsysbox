package ir.moke.jsysbox.firewall.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.moke.jsysbox.firewall.expression.CtExpression;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.model.Rule;

import java.io.IOException;

public class RuleSerializer extends JsonSerializer<Rule> {

    @Override
    public void serialize(Rule rule, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("rule");
        gen.writeStringField("family", rule.getChain().getTable().getType().getValue());
        gen.writeStringField("table", rule.getChain().getTable().getName());
        gen.writeStringField("chain", rule.getChain().getName());
        gen.writeNumberField("handle", rule.getHandle());
        gen.writeStringField("comment", rule.getComment());

        // expr
        if (rule.getExpr() != null) {
            gen.writeObjectFieldStart("expr");
            for (Expression expression : rule.getExpr()) {
                if (expression instanceof CtExpression) {
                } else {
                    // gen.writeObjectFieldStart("match");
                    // gen.writeStringField("op",expression.matchType());
                    // gen.writeEndObject();
                }
            }
            gen.writeEndObject();
        }

        gen.writeEndObject();
        gen.writeEndObject();
    }
}
