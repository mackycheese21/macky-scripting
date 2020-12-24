package macky.scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScriptContext {

    private final HashMap<String, ScriptObject> locals;
    private final Optional<ScriptContext> parent;

    private ScriptContext(Optional<ScriptContext> parent) {
        locals = new HashMap<>();
        this.parent = parent;
    }

    public ScriptObject get(String name) throws ScriptException {
        if (locals.containsKey(name)) {
            return locals.get(name);
        } else if (parent.isPresent()) {
            return parent.get().get(name);
        } else {
            throw new ScriptException("unknown identifier " + name);
        }
    }

    public boolean has(String name) {
        return locals.containsKey(name) || parent.map(parent -> parent.has(name)).orElse(false);
    }

    public void set(String name, ScriptObject value) {
        if (!locals.containsKey(name)) {
            if (parent.isPresent()) {
                if (parent.get().has(name)) {
                    parent.get().set(name, value);
                    return;
                }
            }
        }
        locals.put(name, value);
    }

    public ScriptObject evaluate(AstNode astNode) {
        return AstNodes.cases()
                .nil(ScriptObjects::nil)
                .integer(ScriptObjects::integer)
                .number(ScriptObjects::number)
                .string(ScriptObjects::string)
                .variableAccess(this::get)
                .assign((reference, value) -> {
                    ScriptObject rhs = evaluate(value);
                    reference.set(this, rhs);
                    return ScriptObjects.nil();
                })
                .call((function, arguments) -> {
                    ScriptObject functionObject = evaluate(function);
                    List<ScriptObject> args = arguments.stream().map(this::evaluate).collect(Collectors.toList());
                    return functionObject.function().call(args);
                }).apply(astNode);
    }

    public static ScriptContext createNew() {
        ScriptContext scriptContext = new ScriptContext(Optional.empty());
        scriptContext.set("println", ScriptObjects.function(ScriptFunction.from(argument -> {
            System.out.println(argument.printFormat());
            return ScriptObjects.nil();
        })));
        scriptContext.set("add", ScriptObjects.function(ScriptFunction.from((a, b) -> ScriptObjects.caseOf(a)
                .integer(a1 -> ScriptObjects.caseOf(b)
                        .integer(b1 -> ScriptObjects.integer(a1 + b1))
                        .number(b1 -> ScriptObjects.number(a1 + b1))
                        .otherwiseEmpty()
                        .orElseThrow(() -> new ScriptException("expected numeric"))
                )
                .number(a1 -> ScriptObjects.caseOf(b)
                        .integer(b1 -> ScriptObjects.number(a1 + b1))
                        .number(b1 -> ScriptObjects.number(a1 + b1))
                        .otherwiseEmpty()
                        .orElseThrow(() -> new ScriptException("expected numeric"))
                )
                .otherwiseEmpty()
                .orElseThrow(() -> new ScriptException("expected numeric")))));
        return scriptContext;
    }

}
