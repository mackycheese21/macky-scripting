package macky.scripting;

import org.derive4j.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public abstract class Expression {

    public interface Visitor<X> {
        X boolLiteral(boolean boolLiteral);

        X breakStatement();

        X declFunc(List<String> funcParamNames, Expression funcBody);

        X ifExpr(Expression ifCondition, Expression ifBody, Optional<Expression> ifElse);

        X whileExpr(Expression whileCondition, Expression whileBody);

        X forExpr(Expression forInit, Expression forCondition, Expression forUpdate, Expression forBody);

        X returnExpr(Optional<Expression> returnExpr);

        X loop(Expression loopExpr);

        X declVar(String declName, Expression declValue);

        X mapInit(List<Map.Entry<Expression, Expression>> mapInitExpr);

        X listInit(List<Expression> listInitExpr);

        X nopExpr();

        X string(String stringExpr);

        X number(BigDecimal numberExpr);

        X bracketed(List<Expression> bracketedExpr);

        X accessVar(String name);

        X callFunc(Expression callFunc, List<Expression> params);

        X access(Expression keyOwner, Expression key, boolean method);

        X unaryNegate(Expression expression);

        X operator(Expression left, Operator operator, Expression right);
    }

    public String toCode() {
        return Expressions.caseOf(this)
                .boolLiteral(String::valueOf)
                .breakStatement_("break")
                .declFunc((funcParamNames, funcBody) -> "function(" + String.join(", ", funcParamNames) + ") " + funcBody.toCode())
                .ifExpr((ifCondition, ifBody, ifElse) -> "if " + ifCondition.toCode() + ifBody.toCode() + ifElse.map(ife -> " else " + ife.toCode()).orElse(""))
                .whileExpr((whileCondition, whileBody) -> "while " + whileCondition.toCode() + " " + whileBody.toCode())
                .forExpr((forInit, forCondition, forUpdate, forBody) -> "for " + forInit.toCode() + "; " + forCondition.toCode() + "; " + forUpdate.toCode() + " " + forBody.toCode())
                .returnExpr(expression -> "return" + expression.map(e -> " " + e.toCode()).orElse(""))
                .loop(expression -> "loop " + expression.toCode())
                .declVar((declName, declValue) -> "let " + declName + " = " + declValue.toCode())
                .mapInit(entries -> "map {" + entries.stream().map(entry -> entry.getKey().toCode() + ": " + entry.getValue().toCode()).collect(Collectors.joining(", ")) + "}")
                .listInit(exprs -> "list [" + exprs.stream().map(Expression::toCode).collect(Collectors.joining(", ")) + "]")
                .nopExpr_(";")
                .string(str -> "\"" + str + "\"")
                .number(BigDecimal::toString)
                .bracketed(exprs -> "{" + exprs.stream().map(Expression::toCode).collect(Collectors.joining(" ")) + "}")
                .accessVar(name -> name)
                .callFunc((callFunc, params) -> callFunc.toCode() + "(" + params.stream().map(Expression::toCode).collect(Collectors.joining(", ")) + ")")
                .access((keyOwner, key, method) -> keyOwner.toCode() + (method ? ":" : "") + "[" + key.toCode() + "]")
                .unaryNegate(expr -> "-(" + expr.toCode() + ")")
                .operator((left, operator, right) -> "(" + left.toCode() + " " + Operators.caseOf(operator)
                        .basic(BasicOperator::getName)
                        .assignment(basicOperator -> basicOperator.map(BasicOperator::getAssignmentName).orElse("=")) + " " + right.toCode() + ")"
                )
                ;
    }

    public abstract <X> X visit(Visitor<X> visitor);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}
