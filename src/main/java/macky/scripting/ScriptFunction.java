package macky.scripting;

import java.util.List;

@FunctionalInterface
public interface ScriptFunction {
    ScriptObject call(List<ScriptObject> arguments);
    interface Unary {
        ScriptObject call(ScriptObject argument);
    }
    interface Binary {
        ScriptObject call(ScriptObject arg1, ScriptObject arg2);
    }
    static void minCount(int count, List<ScriptObject> arguments) {
        if(arguments.size() < count) {
            throw new ScriptException("too few arguments");
        }
    }
    static void maxCount(int count, List<ScriptObject> arguments) {
        if(arguments.size() > count) {
            throw new ScriptException("too many arguments");
        }
    }
    static ScriptFunction from(Unary unary) {
        return arguments -> {
            minCount(1, arguments);
            maxCount(1, arguments);
            return unary.call(arguments.get(0));
        };
    }
    static ScriptFunction from(Binary binary) {
        return arguments -> {
            minCount(2, arguments);
            maxCount(2, arguments);
            return binary.call(arguments.get(0), arguments.get(1));
        };
    }
}
