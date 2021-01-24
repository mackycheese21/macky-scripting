package macky.scripting;

import macky.scripting.gen.GrammarLexer;
import macky.scripting.gen.GrammarParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class Tests {

    void e(Scope scope, String code) {
        GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(code));
        GrammarParser parser = new GrammarParser(new CommonTokenStream(lexer));
        GrammarParser.Expression_listContext ctx = parser.expression_list();
        Object result = null;
        System.out.println(">>> " + code);
        try {
            for (GrammarParser.ExpressionContext exprctx : ctx.expression()) {
                result = scope.evaluate(AntlrVisitor.INSTANCE.visit(exprctx));
            }
            System.out.println();
        } catch (ScriptException scriptException) {
            System.err.println("error: " + scriptException.getMessage());
            result = null;
        }
        if (result != null) {
            System.out.println(result);
            System.out.println();
        }
    }

    int fib(int n) {
        if (n == 1) return 1;
        else if (n == 2) return 2;
        else return fib(n - 1) + fib(n - 2);
    }

    @Test
    public void test() {
        Scope scope = new Scope();
        scope.set("println", new ScriptFunction() {
            @Override
            public Object call(List<Object> params) {
                argCount(params, 1);
                System.out.println(params.get(0));
                return null;
            }
        });
        scope.set("print", new ScriptFunction() {
            @Override
            public Object call(List<Object> params) {
                argCount(params, 1);
                System.out.print(params.get(0));
                return null;
            }
        });
        scope.set("math", new ScriptingMap.Builder()
                .put("cos", new ScriptFunction() {
                    @Override
                    public Object call(List<Object> params) {
                        argCount(params, 1);
                        return BigDecimal.valueOf(Math.cos(ScriptObjects.getNumber(params.get(0)).doubleValue()));
                    }
                })
                .put("sin", new ScriptFunction() {
                    @Override
                    public Object call(List<Object> params) {
                        argCount(params, 1);
                        return BigDecimal.valueOf(Math.sin(ScriptObjects.getNumber(params.get(0)).doubleValue()));
                    }
                })
                .put("tan", new ScriptFunction() {
                    @Override
                    public Object call(List<Object> params) {
                        argCount(params, 1);
                        return BigDecimal.valueOf(Math.tan(ScriptObjects.getNumber(params.get(0)).doubleValue()));
                    }
                })
                .put("exp", new ScriptFunction() {
                    @Override
                    public Object call(List<Object> params) {
                        argCount(params, 1);
                        return BigDecimal.valueOf(Math.exp(ScriptObjects.getNumber(params.get(0)).doubleValue()));
                    }
                })
                .put("pow", new ScriptFunction() {
                    @Override
                    public Object call(List<Object> params) {
                        argCount(params, 2);
                        return BigDecimal.valueOf(Math.pow(ScriptObjects.getNumber(params.get(0)).doubleValue(), ScriptObjects.getNumber(params.get(1)).doubleValue()));
                    }
                })
                .put("e", BigDecimal.valueOf(Math.E))
                .put("pi", BigDecimal.valueOf(Math.PI))
                .getData());
        e(scope, "let fib = function(n) { if n == 1 { 1 } else { if n == 2 { 2 } else { fib(n - 1) + fib(n - 2) } } };");
        long start, end;
        start = System.currentTimeMillis();
        e(scope, "for let i = 1; i <= 30; i += 1 { print(fib(i)) print(\" \") }");
        end = System.currentTimeMillis();
        System.out.println();
        long script = end - start;
        start = System.currentTimeMillis();
        for (int i = 1; i <= 30; i += 1) {
            System.out.print(fib(i));
            System.out.print(" ");
        }
        end = System.currentTimeMillis();
        long java = end - start;
        System.out.println();
        System.out.println("script : " + script);
        System.out.println("java   : " + java);
    }
}
