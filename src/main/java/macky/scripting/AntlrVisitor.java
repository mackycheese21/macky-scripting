package macky.scripting;

import macky.scripting.gen.GrammarBaseVisitor;
import macky.scripting.gen.GrammarParser;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class AntlrVisitor extends GrammarBaseVisitor<Expression> {

    private AntlrVisitor() {

    }

    public static final AntlrVisitor INSTANCE = new AntlrVisitor();

    @Override
    public Expression visitParen(GrammarParser.ParenContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Expression visitBoolLiteral(GrammarParser.BoolLiteralContext ctx) {
        return Expressions.boolLiteral(ctx.getText().equals("true"));
    }

    @Override
    public Expression visitBreak(GrammarParser.BreakContext ctx) {
        return Expressions.breakStatement();
    }

    @Override
    public Expression visitDeclFunc(GrammarParser.DeclFuncContext ctx) {
        return Expressions.declFunc(ctx.parametre_header().identifier().stream().map(GrammarParser.IdentifierContext::getText).collect(Collectors.toList()), visit(ctx.bracketed_expr()));
    }

    @Override
    public Expression visitIf(GrammarParser.IfContext ctx) {
        return Expressions.ifExpr(visit(ctx.expression()), visit(ctx.bracketed_expr(0)), ctx.bracketed_expr().size() == 2 ? Optional.of(visit(ctx.bracketed_expr(1))) : Optional.empty());
    }

    @Override
    public Expression visitWhile(GrammarParser.WhileContext ctx) {
        return Expressions.whileExpr(visit(ctx.expression()), visit(ctx.bracketed_expr()));
    }

    @Override
    public Expression visitFor(GrammarParser.ForContext ctx) {
        return Expressions.forExpr(visit(ctx.expression(0)), visit(ctx.expression(1)), visit(ctx.expression(2)), visit(ctx.bracketed_expr()));
    }

    @Override
    public Expression visitReturn(GrammarParser.ReturnContext ctx) {
        return Expressions.returnExpr(ctx.expression() == null ? Optional.empty() : Optional.of(visit(ctx.expression())));
    }

    @Override
    public Expression visitLoop(GrammarParser.LoopContext ctx) {
        return Expressions.loop(visit(ctx.bracketed_expr()));
    }

    @Override
    public Expression visitMapInit(GrammarParser.MapInitContext ctx) {
        return Expressions.mapInit(ctx.map_init_entry().stream().map(entry_ctx -> new AbstractMap.SimpleEntry<>(visit(entry_ctx.expression(0)), visit(entry_ctx.expression(1)))).collect(Collectors.toList()));
    }

    @Override
    public Expression visitListInit(GrammarParser.ListInitContext ctx) {
        return Expressions.listInit(ctx.expression().stream().map(this::visit).collect(Collectors.toList()));
    }

    @Override
    public Expression visitNop(GrammarParser.NopContext ctx) {
        return Expressions.nopExpr();
    }

    @Override
    public Expression visitDeclVar(GrammarParser.DeclVarContext ctx) {
        return Expressions.declVar(ctx.identifier().getText(), visit(ctx.expression()));
    }

    @Override
    public Expression visitStringExpr(GrammarParser.StringExprContext ctx) {
        return Expressions.string(ctx.string().getText().substring(1, ctx.string().getText().length() - 1));
    }

    @Override
    public Expression visitNumberExpr(GrammarParser.NumberExprContext ctx) {
        return Expressions.number(new BigDecimal(ctx.getText()));
    }

    @Override
    public Expression visitBracketed_expr(GrammarParser.Bracketed_exprContext ctx) {
        return Expressions.bracketed(ctx.expression_list().expression().stream().map(this::visit).collect(Collectors.toList()));
    }

    @Override
    public Expression visitIdentifier(GrammarParser.IdentifierContext ctx) {
        return Expressions.accessVar(ctx.getText());
    }

    @Override
    public Expression visitCallFunc(GrammarParser.CallFuncContext ctx) {
        return Expressions.callFunc(visit(ctx.expression()), ctx.parametre_list().expression().stream().map(this::visit).collect(Collectors.toList()));
    }

    @Override
    public Expression visitAccessValueSimple(GrammarParser.AccessValueSimpleContext ctx) {
        return Expressions.access(visit(ctx.expression()), Expressions.string(ctx.identifier().getText()), false);
    }

    @Override
    public Expression visitAccessValueComplex(GrammarParser.AccessValueComplexContext ctx) {
        return Expressions.access(visit(ctx.expression(0)), visit(ctx.expression(1)), false);
    }

    @Override
    public Expression visitAccessMethodSimple(GrammarParser.AccessMethodSimpleContext ctx) {
        return Expressions.access(visit(ctx.expression()), Expressions.string(ctx.identifier().getText()), true);
    }

    @Override
    public Expression visitAccessMethodComplex(GrammarParser.AccessMethodComplexContext ctx) {
        return Expressions.access(visit(ctx.expression(0)), visit(ctx.expression(1)), true);
    }

    @Override
    public Expression visitUnaryNegate(GrammarParser.UnaryNegateContext ctx) {
        return Expressions.unaryNegate(visit(ctx.expression()));
    }

    @Override
    public Expression visitOpMul(GrammarParser.OpMulContext ctx) {
        BasicOperator basicOperator = switch (ctx.multiplicative_operator().getText()) {
            case "*" -> BasicOperator.MUL;
            case "/" -> BasicOperator.DIV;
            case "%" -> BasicOperator.MOD;
            default -> throw new ScriptException("unknown mul operator " + ctx.multiplicative_operator().getText());
        };
        return Expressions.operator(visit(ctx.expression(0)), Operators.basic(basicOperator), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitOpAdd(GrammarParser.OpAddContext ctx) {
        BasicOperator basicOperator = switch (ctx.additive_operator().getText()) {
            case "+" -> BasicOperator.ADD;
            case "-" -> BasicOperator.SUB;
            default -> throw new ScriptException("unknown add operator " + ctx.additive_operator().getText());
        };
        return Expressions.operator(visit(ctx.expression(0)), Operators.basic(basicOperator), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitOpRel(GrammarParser.OpRelContext ctx) {
        BasicOperator basicOperator = switch (ctx.relational_operator().getText()) {
            case "<" -> BasicOperator.LT;
            case "<=" -> BasicOperator.LE;
            case ">" -> BasicOperator.GT;
            case ">=" -> BasicOperator.GE;
            default -> throw new ScriptException("unknown rel operator " + ctx.relational_operator().getText());
        };
        return Expressions.operator(visit(ctx.expression(0)), Operators.basic(basicOperator), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitOpEq(GrammarParser.OpEqContext ctx) {
        BasicOperator basicOperator = switch (ctx.equality_operator().getText()) {
            case "==" -> BasicOperator.EQUALS;
            case "!=" -> BasicOperator.NOT_EQUALS;
            default -> throw new ScriptException("unknown eq operator " + ctx.equality_operator().getText());
        };
        return Expressions.operator(visit(ctx.expression(0)), Operators.basic(basicOperator), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitOpLogicalAnd(GrammarParser.OpLogicalAndContext ctx) {
        return Expressions.operator(visit(ctx.expression(0)), Operators.basic(BasicOperator.AND), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitOpLogicalOr(GrammarParser.OpLogicalOrContext ctx) {
        return Expressions.operator(visit(ctx.expression(0)), Operators.basic(BasicOperator.OR), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitOpAssignment(GrammarParser.OpAssignmentContext ctx) {
        return Expressions.operator(visit(ctx.expression(0)), Operators.assignment(Optional.ofNullable(switch (ctx.assignment_operator().getText()) {
            case "=" -> null;
            case "+=" -> BasicOperator.ADD;
            case "-=" -> BasicOperator.SUB;
            case "*=" -> BasicOperator.MUL;
            case "/=" -> BasicOperator.DIV;
            case "%=" -> BasicOperator.MOD;
            case "&=" -> BasicOperator.AND;
            case "|=" -> BasicOperator.OR;
            default -> throw new ScriptException("unknown assignment operator " + ctx.assignment_operator().getText());
        })), visit(ctx.expression(1)));
    }
}
