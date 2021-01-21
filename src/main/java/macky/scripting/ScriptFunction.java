package macky.scripting;

import java.util.List;

@FunctionalInterface
public interface ScriptFunction {

    void call(List<ScriptObject> arguments);

    default ScriptObject evaluateReturnValue(List<ScriptObject> arguments) {
        try {
            call(arguments);
        } catch (ScriptReturnException e) {
            return e.getValue();
        }
        return ScriptObjects.nil();
    }

    interface Unary {
        void call(ScriptObject argument);
    }

    interface Binary {
        void call(ScriptObject arg1, ScriptObject arg2);
    }

    interface NumericBinary {
        Number call(int a, int b);
        Number call(int a, double b);
        Number call(double a, double b);
    }

    static void minCount(int count, List<ScriptObject> arguments) {
        if (arguments.size() < count) {
            throw new ScriptException("too few arguments");
        }
    }

    static void maxCount(int count, List<ScriptObject> arguments) {
        if (arguments.size() > count) {
            throw new ScriptException("too many arguments");
        }
    }

    static ScriptFunction from(Unary unary) {
        return arguments -> {
            minCount(1, arguments);
            maxCount(1, arguments);
            unary.call(arguments.get(0));
        };
    }

    static ScriptFunction from(Binary binary) {
        return arguments -> {
            minCount(2, arguments);
            maxCount(2, arguments);
            binary.call(arguments.get(0), arguments.get(1));
        };
    }

    static ScriptFunction from(NumericBinary numericBinary) {
        return arguments -> {
            minCount(2, arguments);
            maxCount(2, arguments);
            throw new ScriptReturnException(ScriptObjects.caseOf(arguments.get(0))
                    .integer(a -> ScriptObjects.caseOf(arguments.get(1))
                            .integer(b -> ScriptObject.from(numericBinary.call(a, b)))
                            .number(b -> ScriptObject.from(numericBinary.call(a, b)))
                            .otherwise(() -> {
                                throw new ScriptException("expected numeric");
                            })
                    )
                    .number(a -> ScriptObjects.caseOf(arguments.get(1))
                            .integer(b -> ScriptObject.from(numericBinary.call(b, a)))
                            .number(b -> ScriptObject.from(numericBinary.call(a, b)))
                            .otherwise(() -> {
                                throw new ScriptException("expected numeric");
                            })
                    )
                    .otherwise(() -> {
                        throw new ScriptException("expected numeric");
                    })
            );
        };
    }
}
