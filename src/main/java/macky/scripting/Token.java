package macky.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Token {

    private final TokenType tokenType;
    private final int start;
    private final int end;

    public Token(TokenType tokenType, int start, int end) {
        this.tokenType = tokenType;
        this.start = start;
        this.end = end;
    }

    public Token(TokenType tokenType, int start) {
        this(tokenType, start, start + 1);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return tokenType.toString() + "[" + start + "," + end + "]";
    }

    private static class Reader {
        String code;
        int index = 0;

        Reader(String code) {
            this.code = code;
        }

        Optional<Character> nextOpt() {
            if (index >= code.length()) {
                return Optional.empty();
            } else {
                return Optional.of(code.charAt(index++));
            }
        }

        char next() {
            return nextOpt().orElseThrow(() -> new ScriptException("unexpected eof"));
        }

        boolean has() {
            return index < code.length();
        }
    }

    public static List<Token> parse(String code) {
        Reader reader = new Reader(code);
        List<Token> tokens = new ArrayList<>();
        while (reader.has()) {
            int start = reader.index;
            char ch = reader.next();
            if(Character.isWhitespace(ch)) continue;
            if(ch == '(') {
                tokens.add(new Token(TokenTypes.leftParen(), start));
            } else if(ch == ')') {
                tokens.add(new Token(TokenTypes.rightParen(), start));
            } else if (ch == ',') {
                tokens.add(new Token(TokenTypes.comma(), start));
            } else if (ch == '{') {
                tokens.add(new Token(TokenTypes.leftBrace(), start));
            } else if (ch == '}') {
                tokens.add(new Token(TokenTypes.rightBrace(), start));
            } else if (ch == '[') {
                tokens.add(new Token(TokenTypes.leftBracket(), start));
            } else if (ch == ']') {
                tokens.add(new Token(TokenTypes.rightBracket(), start));
            } else if (ch == '+') {
                tokens.add(new Token(TokenTypes.additive(AdditiveOperations.add()), start));
            } else if (ch == '-') {
                tokens.add(new Token(TokenTypes.additive(AdditiveOperations.sub()), start));
            } else if (ch == '*') {
                tokens.add(new Token(TokenTypes.multiplicative(MultiplicativeOperations.mul()), start));
            } else if (ch == '/') {
                tokens.add(new Token(TokenTypes.multiplicative(MultiplicativeOperations.div()), start));
            } else if (ch == ';') {
                tokens.add(new Token(TokenTypes.semicolon(), start));
            } else if (ch == '=') {
                tokens.add(new Token(TokenTypes.equalsSign(), start));
            } else if (ch == '!') {
                tokens.add(new Token(TokenTypes.exclamationMark(), start));
            } else if (ch == '.') {
                tokens.add(new Token(TokenTypes.dot(), start));
            } else {
                StringBuilder buffer = new StringBuilder(ch + "");
                Optional<Character> nextChOpt = reader.nextOpt();
                while(nextChOpt.isPresent()) {
                    char nextCh = nextChOpt.get();
                    if(Character.isWhitespace(nextCh) || "(),{}[]+-*/;=!.\"".contains("" + nextCh)) {
                        reader.index --;
                        break;
                    }
                    buffer.append(nextCh);
                    nextChOpt = reader.nextOpt();
                }
                String bufferStr = buffer.toString();
                try {
                    tokens.add(new Token(TokenTypes.integer(Integer.parseInt(bufferStr)), start, reader.index));
                } catch (NumberFormatException ignored) {
                    try {
                        tokens.add(new Token(TokenTypes.number(Double.parseDouble(bufferStr)), start, reader.index));
                    } catch (NumberFormatException ignored1) {
                        tokens.add(new Token(TokenTypes.identifier(bufferStr), start, reader.index));
                    }
                }
            }
        }
        return tokens;
    }
}
