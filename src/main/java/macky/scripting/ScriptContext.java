package macky.scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

    public void let(String name, ScriptObject value) {
        locals.put(name, value);
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
                .bool(ScriptObjects::bool)
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
                    return functionObject.function().evaluateReturnValue(args);
                })
                .return_(returnValue -> {
                    throw new ScriptReturnException(evaluate(returnValue));
                })
                .functionDeclaration((argumentNames, variadicName, body) -> ScriptObjects.function(arguments -> {
                    ScriptContext ctx = new ScriptContext(Optional.of(this));
                    ScriptFunction.minCount(argumentNames.size(), arguments);
                    for (int i = 0; i < argumentNames.size(); i++) {
                        ctx.let(argumentNames.get(i), arguments.get(i));
                    }
                    if (variadicName.isPresent()) {
                        ctx.let(variadicName.get(), ScriptObjects.table(new ScriptTable(arguments.stream().skip(argumentNames.size()).collect(Collectors.toList()))));
                    } else {
                        ScriptFunction.maxCount(argumentNames.size(), arguments);
                    }
                    body.forEach(this::evaluate);
                }))
                .tableInitialization(tableInitializers -> {
                    ScriptTable table = new ScriptTable();
                    AtomicInteger i = new AtomicInteger();
                    for (TableInitializerEntry entry : tableInitializers) {
                        TableInitializerEntries.caseOf(entry)
                                .map((key, value) -> {
                                    table.put(evaluate(key), evaluate(value));
                                    return 0;
                                })
                                .list(value -> {
                                    table.put(ScriptObjects.integer(i.getAndIncrement()), evaluate(value));
                                    return 0;
                                });
                    }
                    return ScriptObjects.table(table);
                })
                .tableIndex((table, key) -> {
                    ScriptObject scriptTable = evaluate(table);
                    ScriptObject scriptKey = evaluate(key);
                    return scriptTable.table().get(scriptKey).orElse(ScriptObjects.nil());
                })
                .let((name, value) -> {
                    let(name, evaluate(value));
                    return ScriptObjects.nil();
                })
                .binaryOperation(binaryOperationCall -> {
                    binaryOperationCall.getOperation().getFunction().call(List.of(evaluate(binaryOperationCall.getLeft()), evaluate(binaryOperationCall.getRight())));
                    throw new ScriptException("expected a return from builtin binary operation");
                })
                .apply(astNode);
    }

    public static ScriptContext createNew() {
        ScriptContext scriptContext = new ScriptContext(Optional.empty());
        scriptContext.set("println", ScriptObjects.function(ScriptFunction.from(argument -> System.out.println(argument.printFormat()))));
        scriptContext.set("multiply", ScriptObjects.function(ScriptFunction.from(new ScriptFunction.NumericBinary() {
            @Override
            public Number call(int a, int b) {
                return a * b;
            }

            @Override
            public Number call(int a, double b) {
                return a * b;
            }

            @Override
            public Number call(double a, double b) {
                return a * b;
            }
        })));
        scriptContext.set("negate", ScriptObjects.function(ScriptFunction.from(argument -> {
            throw new ScriptReturnException(ScriptObjects.caseOf(argument)
                    .integer(a -> ScriptObjects.integer(-a))
                    .number(a -> ScriptObjects.number(-a))
                    .otherwise(() -> {
                        throw new ScriptException("expected numeric");
                    }));
        })));
        scriptContext.set("not", ScriptObjects.function(ScriptFunction.from(argument -> {
            throw new ScriptReturnException(ScriptObjects.bool(!argument.bool()));
        })));
        return scriptContext;
    }

}
