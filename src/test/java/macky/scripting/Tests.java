package macky.scripting;

import macky.scripting.gen.GrammarLexer;
import macky.scripting.gen.GrammarParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

public class Tests {

    void e(Scope scope, String code) {
        GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(code));
        GrammarParser parser = new GrammarParser(new CommonTokenStream(lexer));
        GrammarParser.Expression_listContext ctx = parser.expression_list();
        Object result = null;
        System.out.print(">>> " + code);
        try {
            for (GrammarParser.ExpressionContext exprctx : ctx.expression()) {
                result = scope.evaluate(AntlrVisitor.INSTANCE.visit(exprctx));
            }
            System.out.println();
        }catch (ScriptException scriptException) {
            System.out.println();
            System.err.println("error: " + scriptException.getMessage());
            result = null;
        }
        if (result != null) {
            System.out.println(result);
            System.out.println();
        }
    }

    @Test
    public void test() {
        Scope scope = new Scope();
        e(scope, "let x = map { \"i\" = 5; \"increment\" = function(this) { this.i += 1; }};");
        e(scope, "x");
        e(scope, "x.increment");
        e(scope, "x.i");
        e(scope, "x:increment()");
        e(scope, "x.i");
        e(scope, "x.increment()");
        e(scope, "x.i");
        e(scope, "x.increment(x)");
        e(scope, "x.i");

        e(scope, "let make_counter = function() { let i = 0; function() { i += 1; i } }");
        e(scope, "a = make_counter()");
        e(scope, "b = make_counter()");
        e(scope, "a()");
        e(scope, "b()");
        e(scope, "a()");
        e(scope, "b()");
        e(scope, "a()");
        e(scope, "b()");
        e(scope, "a()");
        e(scope, "b()");
    }
}
