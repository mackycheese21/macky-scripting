package macky.scripting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scope {

    public static boolean TRACE = false;

    private final Scope parent;
    private final Map<String, Object> vars = new HashMap<>();

    public static final ScriptException BREAK = new ScriptException("break");

    public Scope() {
        parent = null;
    }

    public Scope(Scope parent) {
        this.parent = parent;
    }


    public void let(String name, Object value) {
        vars.put(name, value);
    }

    public boolean has(String name) {
        if (vars.containsKey(name)) return true;
        else if (parent != null) return parent.has(name);
        else return false;
    }

    public void set(String name, Object value) {
        if (vars.containsKey(name)) {
            vars.put(name, value);
        } else if (parent != null && parent.has(name)) {
            parent.set(name, value);
        } else {
            vars.put(name, value);
        }
    }

    public Object get(String name) throws ScriptException {
        if (vars.containsKey(name)) return vars.get(name);
        else if (parent != null) return parent.get(name);
        else throw new ScriptException("unknown variable " + name);
    }

    public Scope push() {
        return new Scope(this);
    }

    public Object evaluate(Expression expression) {
        Object debug = Expressions.caseOf(expression)
                .boolLiteral(b -> (Object) b)
                .breakStatement(() -> {
                    throw new ScriptException("break statements are not yet supported");
                })
                .declFunc((funcParamNames, funcBody) -> {
                    return new ScriptFunction() {
                        @Override
                        public Object call(List<Object> params) {
                            ScriptFunction.argCount(params, funcParamNames.size());
                            Scope scope = push();
                            for (int i = 0; i < funcParamNames.size(); i++) {
                                scope.let(funcParamNames.get(i), params.get(i));
                            }
                            try {
                                return scope.evaluate(funcBody);
                            } catch (ScriptReturnException scriptReturnException) {
                                return scriptReturnException.getReturnValue();
                            }
                        }
                    };
                })
                .ifExpr((ifCondition, ifBody, ifElse) -> {
                    if (ScriptObjects.asBoolean(evaluate(ifCondition))) {
                        return push().evaluate(ifBody);
                    } else return ifElse.map(value -> push().evaluate(value)).orElse(null);
                })
                .whileExpr((whileCondition, whileBody) -> {
                    Object last = null;
                    while (ScriptObjects.asBoolean(evaluate(whileCondition))) {
                        last = push().evaluate(whileBody);
                    }
                    return last;
                })
                .forExpr((forInit, forCondition, forUpdate, forBody) -> {
                    evaluate(forInit);
                    Object last = null;
                    while (ScriptObjects.asBoolean(evaluate(forCondition))) {
                        last = push().evaluate(forBody);
                        evaluate(forUpdate);
                    }
                    return last;
                })
                .returnExpr(expr -> {
                    if (expr.isPresent()) {
                        throw new ScriptReturnException(evaluate(expr.get()));
                    }
                    else throw new ScriptReturnException(null);
                })
                .loop(loopBody -> {
                    while (true) {
                        push().evaluate(loopBody);
                    }
                })
                .declVar((declName, declValue) -> {
                    Object value = evaluate(declValue);
                    let(declName, value);
                    return value;
                })
                .mapInit(list -> {
                    Map<Object, Object> res = new HashMap<>();
                    for (Map.Entry<Expression, Expression> entry : list) {
                        res.put(evaluate(entry.getKey()), evaluate(entry.getValue()));
                    }
                    return res;
                })
                .listInit(list -> {
                    List<Object> res = new ArrayList<>();
                    for (Expression entry : list) {
                        res.add(evaluate(entry));
                    }
                    return res;
                })
                .nopExpr(() -> null)
                .string(string -> string)
                .number(number -> number)
                .bracketed(exprs -> {
                    Object last = null;
                    Scope scope = push();
                    for (Expression expr : exprs) {
                        last = scope.evaluate(expr);
                    }
                    return last;
                })
                .accessVar(this::get)
                .callFunc((callFunc, params) -> {
                    List<Object> params1 = new ArrayList<>();
                    for (Expression param : params) {
                        params1.add(evaluate(param));
                    }
                    return ScriptObjects.getFunction(evaluate(callFunc)).call(params1);
                })
                .access((keyOwner, key, method) -> ScriptObjects.access(evaluate(keyOwner), evaluate(key), method))
                .unaryNegate(expr -> {
                    Object val = evaluate(expr);
                    if (val instanceof BigDecimal) {
                        return ((BigDecimal) val).negate();
                    } else {
                        throw new ScriptException("cannot negate a non-number");
                    }
                })
                .operator((left, operator, right) -> Operators.caseOf(operator)
                        .basic(basicOperator -> basicOperator.getOp().call(this, evaluate(left), right))
                        .assignment(basicOperator -> Expressions.caseOf(left)
                                .accessVar(varName -> {
                                    Object result;
                                    if (basicOperator.isPresent())
                                        result = basicOperator.get().getOp().call(this, get(varName), right);
                                    else result = evaluate(right);
                                    set(varName, result);
                                    return result;
                                })
                                .access((keyOwner, key, method) -> {
                                    if(method) {
                                        throw new ScriptException("cannot assign to a method access");
                                    }
                                    Object owner = evaluate(keyOwner);
                                    Object realKey = evaluate(key);
                                    Object result;
                                    if (basicOperator.isPresent())
                                        result = basicOperator.get().getOp().call(this, ScriptObjects.access(owner, realKey, false), right);
                                    else result = evaluate(right);
                                    ScriptObjects.assign(owner, realKey, result);
                                    return result;
                                })
                                .otherwise(() -> {
                                    throw new ScriptException("cannot assign to non-reference");
                                })
                        )
                )
                ;
        if(TRACE) {
            System.out.println("evaluated `" + expression.toCode() + "` => " + debug);
        }
        return debug;
    }

}
