package macky.scripting;

import org.derive4j.Data;

@Data
public abstract class BinaryOperation {

    public interface Cases<X> {
        X plus();
        X minus();
        X multiply();
        X divide();
        X modulo();
        X le();
        X leq();
        X ge();
        X geq();
        X and();
        X or();
        X assign();
        X plusAssign();
        X minusAssign();
        X multiplyAssign();
        X divideAssign();
        X moduloAssign();
        X eqeq();
    }

    public abstract <X> X match(Cases<X> cases);

    @Override
    public String toString() {
        return BinaryOperations.caseOf(this)
                .plus_("plus")
                .minus_("minus")
                .multiply_("multiply")
                .divide_("divide")
                .modulo_("modulo")
                .le_("le")
                .leq_("leq")
                .ge_("ge")
                .geq_("geq")
                .and_("and")
                .or_("or")
                .assign_("assign")
                .plusAssign_("plusAssign")
                .minusAssign_("minusAssign")
                .multiplyAssign_("multiplyAssign")
                .divideAssign_("divideAssign")
                .moduloAssign_("moduloAssign")
                .eqeq_("eqeq");
    }

    public String tokenName() {
        return BinaryOperations.caseOf(this)
                .plus_("+")
                .minus_("-")
                .multiply_("*")
                .divide_("/")
                .modulo_("%")
                .le_("<")
                .leq_("<=")
                .ge_(">")
                .geq_(">=")
                .and_("&&")
                .or_("||")
                .assign_("=")
                .plusAssign_("+=")
                .minusAssign_("-=")
                .multiplyAssign_("*=")
                .divideAssign_("/=")
                .moduloAssign_("%=")
                .eqeq_("==");
    }

    public boolean isAdditive() {
        return BinaryOperations.caseOf(this)
                .plus_(true)
                .minus_(true)
                .otherwise_(false);
    }

    public boolean isMultiplicative() {
        return BinaryOperations.caseOf(this)
                .multiply_(true)
                .divide_(true)
                .modulo_(true)
                .otherwise_(false);
    }

    public static final ScriptFunction PLUS = ScriptFunction.from(new ScriptFunction.NumericBinary() {
        @Override
        public Number call(int a, int b) {
            return a + b;
        }

        @Override
        public Number call(int a, double b) {
            return a + b;
        }

        @Override
        public Number call(double a, double b) {
            return a + b;
        }
    });

    public static final ScriptFunction MULTIPLY = ScriptFunction.from(new ScriptFunction.NumericBinary() {
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
    });

    public ScriptFunction getFunction() {
        return BinaryOperations.caseOf(this)
                .plus_(PLUS)
                .multiply_(MULTIPLY)
                .otherwise(() -> {
                    throw new ScriptException("unsupported operation " + this);
                });
    }

}
