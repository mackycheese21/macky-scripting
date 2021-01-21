package macky.scripting;

import org.apache.commons.text.StringEscapeUtils;
import org.derive4j.Data;

import java.util.function.Function;

@Data
public abstract class TokenType {

    public interface Cases<X> {
        X identifier(String identifier);

        X integer(int integer);

        X number(double number);

        X string(String string);

        X leftParen();

        X rightParen();

        X comma();

        X leftBrace();

        X rightBrace();

        X leftBracket();

        X rightBracket();

        X binaryOperation(BinaryOperation binaryOperation);

        X semicolon();

        X equalsSign();

        X exclamationMark();

        X dot();
    }

    public abstract <X> X match(Cases<X> cases);

    @Override
    public String toString() {
        return TokenTypes.caseOf(this)
                .identifier(Function.identity())
                .integer(integer -> "i(" + integer + ")")
                .number(number -> "n(" + number + ")")
                .string(string -> "\"" + StringEscapeUtils.escapeJava(string) + "\"")
                .leftParen_("(")
                .rightParen_(")")
                .comma_(",")
                .leftBrace_("{")
                .rightBrace_("}")
                .leftBracket_("[")
                .rightBracket_("]")
                .binaryOperation(BinaryOperation::toString)
                .semicolon_(";")
                .equalsSign_("=")
                .exclamationMark_("!")
                .dot_(".");
    }

    public String getName() {
        return TokenTypes.caseOf(this)
                .identifier_("identifier")
                .integer_("integer")
                .number_("number")
                .string_("string")
                .leftParen_("left paren")
                .rightParen_("right paren")
                .comma_("comma")
                .leftBrace_("left brace")
                .rightBrace_("right brace")
                .leftBracket_("left bracket")
                .rightBracket_("right bracket")
                .binaryOperation(operation -> "binop[" + operation.tokenName() + "]")
                .semicolon_("semicolon")
                .equalsSign_("equals sign")
                .exclamationMark_("exclamation mark")
                .dot_("dot");
    }
}
