package macky.scripting;

import org.apache.commons.text.StringEscapeUtils;
import org.derive4j.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public abstract class AstNode {

    public interface Cases<X> {
        X nil();

        X integer(int integer);

        X number(double number);

        X bool(boolean bool);

        X string(String string);

        X variableAccess(String name);

        X assign(AstNode reference, AstNode value);

        X call(AstNode function, List<AstNode> arguments);

        X return_(AstNode returnValue);

        X functionDeclaration(List<String> argumentNames, Optional<String> variadicName, List<AstNode> body);

        X tableInitialization(List<TableInitializerEntry> tableInitializers);

        X tableIndex(AstNode table, AstNode key);

        X define(String name, AstNode value);
    }

    public abstract <X> X match(Cases<X> cases);

    @Override
    public String toString() {
        return AstNodes.caseOf(this)
                .nil_("nil")
                .integer(integer -> "i(" + integer + ")")
                .number(number -> "n(" + number + ")")
                .bool(String::valueOf)
                .string(string -> "\"" + StringEscapeUtils.escapeJava(string) + "\"")
                .variableAccess(name -> "var(" + name + ")")
                .assign((reference, value) -> "assign(" + value + " -> " + reference + ")")
                .call((function, arguments) -> "call(" + function + " on " + arguments.stream().map(AstNode::toString).collect(Collectors.joining(", ")) + ")")
                .return_(returnValue -> "return(" + returnValue + ")")
                .functionDeclaration((argumentNames, variadicName, body) -> "function(" + String.join(", ", argumentNames) + ".." + variadicName.map(v -> "variadic(" + v + ")").orElse("none") + ": " + body.stream().map(AstNode::toString).collect(Collectors.joining(" ")) + ")")
                .tableInitialization(tableInitializerEntries -> "table(" + tableInitializerEntries.stream().map(entry -> TableInitializerEntries.caseOf(entry).map((key, value) -> "map(" + key + " = " + value).list(value -> "list(" + value + ")")).collect(Collectors.joining(", ")) + ")")
                .tableIndex((table, key) -> "index(" + table + "[" + key + "])")
                .define((name, value) -> "define(" + name + " = " + value + ")");
    }

    private String formatArgs(List<String> argumentNames, Optional<String> variadicName) {
        if(argumentNames.size() == 0) {
            return variadicName.map(s -> "." + s).orElse("");
        } else {
            return variadicName.map(s -> String.join(", ", argumentNames) + ", " + s).orElseGet(() -> String.join(", ", argumentNames));
        }
    }

    public String toCode() {
        return AstNodes.caseOf(this)
                .nil_("nil")
                .integer(String::valueOf)
                .number(String::valueOf)
                .bool(String::valueOf)
                .string(string -> "\"" + StringEscapeUtils.escapeJava(string) + "\"")
                .variableAccess(String::valueOf)
                .assign((reference, value) -> reference.toCode() + " = " + value.toCode())
                .call((function, arguments) -> function.toCode() + "(" + arguments.stream().map(AstNode::toCode).collect(Collectors.joining(", ")) + ")")
                .return_(returnValue -> "return " + returnValue)
                .functionDeclaration((argumentNames, variadicName, body) -> "function(" + formatArgs(argumentNames, variadicName) + ") { " + body.stream().map(AstNode::toCode).collect(Collectors.joining(" ")) + " }")
                .tableInitialization(entries -> "{ " + entries.stream().map(entry -> TableInitializerEntries.caseOf(entry).map((key, value) -> key.toCode() + " = " + value.toCode()).list(AstNode::toCode)).collect(Collectors.joining(", ")) + " }")
                .tableIndex((table, key) -> table.toCode() + "[" + key.toCode() + "]")
                .define((name, value) -> "define " + name + " = " + value.toCode());
    }

    public void set(ScriptContext context, ScriptObject value) {
        AstNodes.caseOf(this)
                .variableAccess(name -> {
                    context.set(name, value);
                    return 0;
                })
                .tableIndex((table, key) -> {
                    context.evaluate(table).table().put(context.evaluate(key), value);
                    return 0;
                })
                .otherwise(() -> {
                    throw new ScriptException("expected reference");
                });
    }

    private static ScriptException UNEXPECT(TokenType tokenType) {
        return new ScriptException("unexpected " + tokenType.getName());
    }

    private static void EXPECT(List<Token> tokens, Function<TokenType, Boolean> cases) {
        TokenType tokenType = tokens.remove(0).getTokenType();
        if (!cases.apply(tokenType)) {
            throw UNEXPECT(tokenType);
        }
    }

    private static boolean consumeIfPresent(List<Token> tokens, Function<TokenType, Boolean> function) {
        if (function.apply(tokens.get(0).getTokenType())) {
            return true;
        } else {
            return false;
        }
    }

    private static List<AstNode> parseBracketedCode(List<Token> tokens) {
        EXPECT(tokens, TokenTypes.cases().leftBrace_(true).otherwise_(false));
        List<AstNode> nodes = new ArrayList<>();
        while (tokens.get(0).getTokenType() != TokenTypes.rightBrace()) {
            nodes.add(parseExpression(tokens));
        }
        tokens.remove(0);
        return nodes;
    }

    private static TableInitializerEntry parseTableInitializerEntry(List<Token> tokens) {
        return TokenTypes.caseOf(tokens.get(0).getTokenType())
                .identifier(identifier -> {
                    tokens.remove(0);
                    if (tokens.get(0).getTokenType() == TokenTypes.equalsSign()) {
                        tokens.remove(0);
                        AstNode value = parseExpression(tokens);
                        return TableInitializerEntries.map(AstNodes.string(identifier), value);
                    } else {
                        return TableInitializerEntries.list(AstNodes.variableAccess(identifier));
                    }
                })
                .leftBracket(() -> {
                    tokens.remove(0);
                    AstNode key = parseExpression(tokens);
                    EXPECT(tokens, TokenTypes.rightBracket()::equals);
                    EXPECT(tokens, TokenTypes.equalsSign()::equals);
                    AstNode value = parseExpression(tokens);
                    return TableInitializerEntries.map(key, value);
                })
                .otherwise(() -> TableInitializerEntries.list(parseExpression(tokens)));
    }

    private static AstNode parseValue(List<Token> tokens) {
        return TokenTypes.caseOf(tokens.remove(0).getTokenType())
                .identifier(identifier -> {
                    if (identifier.equals("nil")) {
                        return AstNodes.nil();
                    } else if (identifier.equals("return")) {
                        return AstNodes.return_(parseExpression(tokens));
                    } else if (identifier.equals("true")) {
                        return AstNodes.bool(true);
                    } else if (identifier.equals("false")) {
                        return AstNodes.bool(false);
                    } else if (identifier.equals("function")) {
                        EXPECT(tokens, TokenTypes.leftParen()::equals);
                        List<String> argNames = new ArrayList<>();
                        if (tokens.get(0).getTokenType() == TokenTypes.rightParen()) {
                            tokens.remove(0);
                        } else {
                            while (true) {
                                TokenType t = tokens.remove(0).getTokenType();
                                argNames.add(TokenTypes.getIdentifier(t).orElseThrow(() -> UNEXPECT(t)));
                                if (tokens.get(0).getTokenType() == TokenTypes.rightParen()) {
                                    tokens.remove(0);
                                    break;
                                } else {
                                    EXPECT(tokens, TokenTypes.cases().comma_(true).semicolon_(true).otherwise_(false));
                                }
                            }
                        }
                        return AstNodes.functionDeclaration(argNames, Optional.empty(), parseBracketedCode(tokens));
                    } else if (identifier.equals("define")) {
                        EXPECT(tokens, TokenTypes.equalsSign()::equals);
                        return AstNodes.define(identifier, parseExpression(tokens));
                    } else {
                        return AstNodes.variableAccess(identifier);
                    }
                })
                .integer(AstNodes::integer)
                .number(AstNodes::number)
                .string(AstNodes::string)
                .leftParen(() -> {
                    AstNode node = parseExpression(tokens);
                    EXPECT(tokens, TokenTypes.rightParen()::equals);
                    return node;
                })
                .leftBrace(() -> {
                    List<TableInitializerEntry> entries = new ArrayList<>();
                    if (!consumeIfPresent(tokens, TokenTypes.rightBrace()::equals)) {
                        while (true) {
                            entries.add(parseTableInitializerEntry(tokens));
                            if (consumeIfPresent(tokens, TokenTypes.rightBrace()::equals)) {
                                break;
                            } else {
                                EXPECT(tokens, TokenTypes.cases().comma_(true).semicolon_(true).otherwise_(false));
                            }
                        }
                    }
                    return AstNodes.tableInitialization(entries);
                })
                .otherwise(() -> {
                    throw UNEXPECT(tokens.get(0).getTokenType());
                });
    }

    private static AstNode parsePrimary(List<Token> tokens) {
        AstNode node = parseValue(tokens);
        while (tokens.size() > 0) {
            if(consumeIfPresent(tokens, TokenTypes.dot()::equals)) {
                node = AstNodes.tableIndex(node, AstNodes.string(TokenTypes.getIdentifier(tokens.remove(0).getTokenType()).orElseThrow(() -> new ScriptException("expected identifier"))));
            } else if (consumeIfPresent(tokens, TokenTypes.leftBracket()::equals)) {
                node = AstNodes.tableIndex(node, parseExpression(tokens));
                EXPECT(tokens, TokenTypes.rightBracket()::equals);
            } else if (consumeIfPresent(tokens, TokenTypes.leftParen()::equals)) {
                List<AstNode> arguments = new ArrayList<>();
                if (!consumeIfPresent(tokens, TokenTypes.rightParen()::equals)) {
                    while (true) {
                        arguments.add(parseExpression(tokens));
                        if (consumeIfPresent(tokens, TokenTypes.rightBracket()::equals)) {
                            break;
                        } else {
                            EXPECT(tokens, TokenTypes.cases().comma_(true).semicolon_(true).otherwise_(false));
                        }
                    }
                }
                node = AstNodes.call(node, arguments);
            } else {
                break;
            }
        }
        return node;
    }

    private static AstNode parseMultiplicative(List<Token> tokens) {
        AstNode node = parsePrimary(tokens);
        while (tokens.size() > 0) {
            TokenType t = tokens.get(0).getTokenType();
            Optional<MultiplicativeOperation> opt = TokenTypes.getMultiplicativeOperation(t);
            if (opt.isPresent()) {
                tokens.remove(0);
                node = AstNodes.call(AstNodes.variableAccess(MultiplicativeOperations.caseOf(opt.get()).mul_("mul").div_("mul")), List.of(node, parsePrimary(tokens)));
            } else {
                break;
            }
        }
        return node;
    }

    private static AstNode parseAdditive(List<Token> tokens) {
        AstNode node = parseMultiplicative(tokens);
        while (tokens.size() > 0) {
            TokenType t = tokens.get(0).getTokenType();
            Optional<AdditiveOperation> opt = TokenTypes.getAdditiveOperation(t);
            if (opt.isPresent()) {
                tokens.remove(0);
                node = AstNodes.call(AstNodes.variableAccess(AdditiveOperations.caseOf(opt.get()).add_("add").sub_("sub")), List.of(node, parseMultiplicative(tokens)));
            } else {
                break;
            }
        }
        return node;
    }

    public static AstNode parseExpression(List<Token> tokens) {
        try {
            return parseAdditive(tokens);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            throw new ScriptException("unexpected eof");
        }
    }

}