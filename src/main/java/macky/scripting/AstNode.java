package macky.scripting;

import org.derive4j.Data;

import java.util.List;

@Data
public abstract class AstNode {
    public interface Cases<X> {
        X nil();
        X integer(int integer);
        X number(double number);
        X string(String string);
        X variableAccess(String name);
        X assign(AstNode reference, AstNode value);
        X call(AstNode function, List<AstNode> arguments);
    }
    public abstract <X> X match(Cases<X> cases);

    public void set(ScriptContext context, ScriptObject value) {
        AstNodes.caseOf(this).variableAccess(name -> {
            context.set(name, value);
            return 0;
        }).otherwiseEmpty().orElseThrow(() -> new ScriptException("expected reference"));
    }

}