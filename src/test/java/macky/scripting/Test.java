package macky.scripting;

import java.util.List;

public class Test {
    @org.junit.jupiter.api.Test
    void test() {
        AstNode a = AstNodes.assign(AstNodes.variableAccess("i"), AstNodes.call(AstNodes.variableAccess("add"), List.of(AstNodes.integer(5), AstNodes.integer(3))));
        AstNode b = AstNodes.call(AstNodes.variableAccess("println"), List.of(AstNodes.variableAccess("i")));
        AstNode c = AstNodes.assign(AstNodes.variableAccess("i"), AstNodes.call(AstNodes.variableAccess("add"), List.of(AstNodes.number(10), AstNodes.variableAccess("i"))));
        AstNode d = AstNodes.call(AstNodes.variableAccess("println"), List.of(AstNodes.variableAccess("i")));
        ScriptContext scriptContext = ScriptContext.createNew();
        scriptContext.evaluate(a);
        scriptContext.evaluate(b);
        scriptContext.evaluate(c);
        scriptContext.evaluate(d);

        List<Token> tokens = Token.parse("(a + b) * c");
        System.out.println();
        tokens.forEach(System.out::println);

        AstNode node = AstNode.parseExpression(tokens);
        System.out.println();
        System.out.println(node.toCode());

        System.out.println();
        tokens.forEach(System.out::println);
    }
}
