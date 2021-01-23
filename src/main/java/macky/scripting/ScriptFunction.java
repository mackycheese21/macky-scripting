package macky.scripting;

import java.math.BigDecimal;
import java.util.List;

public abstract class ScriptFunction {

    public abstract Object call(List<Object> params);

    @Override
    public String toString() {
        return "<function>";
    }

    public interface Numeric {
        Object call(BigDecimal a, BigDecimal b);
    }

    public interface Binary {
        Object call(Object a, Object b);

        static Binary from(Numeric numeric) {
            return (a, b) -> numeric.call(ScriptObjects.getNumber(a), ScriptObjects.getNumber(b));
        }
    }

    static <T> void minArgCount(List<T> params, int count) {
        if(params.size() < count) throw new ScriptException("invalid arg count");
    }

    static <T> void maxArgCount(List<T> params, int count) {
        if(params.size() > count) throw new ScriptException("invalid arg count");
    }

    static <T> void argCount(List<T> params, int count) {
        minArgCount(params, count);
        maxArgCount(params, count);
    }

    static ScriptFunction from(Binary binary) {
        return new ScriptFunction() {
            @Override
            public Object call(List<Object> params) {
                argCount(params, 2);
                return binary.call(params.get(0), params.get(1));
            }
        };
    }

}
