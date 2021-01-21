package macky.scripting;

import java.util.List;

public class Test {
    void e(ScriptContext context, String code) {
        System.out.println(">>> " + code);
        List<Token> tokens = Token.parse(code);
        while(tokens.size() > 0) {
            AstNode astNode = AstNode.parseExpression(tokens);
            System.out.println("[    " + astNode.toCode() + "    ]");
            System.out.println(context.evaluate(astNode).printFormat());
        }
    }
    @org.junit.jupiter.api.Test
    void test() {
        ScriptContext c = ScriptContext.createNew();
        e(c, "let i = 3");
        e(c, "let j = 4");
        e(c, "let k = 5");
        e(c, "println(i+j*k) println(i*j+k) println(i*(j+k)) println((i+j)*k)");
    }
}
