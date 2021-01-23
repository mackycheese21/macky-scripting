package macky.scripting;

import java.math.BigDecimal;

public enum BasicOperator {
    MUL("*", Op.from(ScriptFunction.Binary.from(BigDecimal::multiply))),
    DIV("/", Op.from(ScriptFunction.Binary.from(BigDecimal::divide))),
    MOD("%", Op.from(ScriptFunction.Binary.from(BigDecimal::remainder))),
    ADD("+", Op.from(ScriptFunction.Binary.from(BigDecimal::add))),
    SUB("-", Op.from(ScriptFunction.Binary.from(BigDecimal::subtract))),
    LE("<=", null, Op.from(ScriptFunction.Binary.from((a, b) -> a.compareTo(b) <= 0))),
    LT("<", null, Op.from(ScriptFunction.Binary.from((a, b) -> a.compareTo(b) < 0))),
    GE(">=", null, Op.from(ScriptFunction.Binary.from((a, b) -> a.compareTo(b) >= 0))),
    GT(">", null, Op.from(ScriptFunction.Binary.from((a, b) -> a.compareTo(b) > 0))),
    EQUALS("==", null, Op.from(ScriptFunction.Binary.from(BigDecimal::equals))),
    NOT_EQUALS("!=", null, Op.from(ScriptFunction.Binary.from((a, b) -> !a.equals(b)))),
    AND("&&", "&=", (scope, a, b) -> {
        if(ScriptObjects.asBoolean(a)) {
            return scope.evaluate(b);
        } else {
            return a;
        }
    }),
    OR("||", "|=", (scope, a, b) -> {
        if(ScriptObjects.asBoolean(a)) {
            return a;
        } else {
            return scope.evaluate(b);
        }
    });

    public interface Op {
        Object call(Scope scope, Object a, Expression b);

        static Op from(ScriptFunction.Binary function) {
            return (scope, a, b) -> function.call(a, scope.evaluate(b));
        }
    }

    private final String name;
    private final String assignmentName;
    private final Op op;

    BasicOperator(String name, Op op) {
        this(name, null, op);
    }

    BasicOperator(String name, String assignmentName, Op op) {
        this.name = name;
        this.assignmentName = assignmentName;
        this.op = op;
    }

    public String getName() {
        return name;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public Op getOp() {
        return op;
    }
}
