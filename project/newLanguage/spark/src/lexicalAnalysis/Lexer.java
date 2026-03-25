package lexicalAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lexer {
    private static final Set<String> keywords = Set.of(
            "input", "const", "print", "read"
    );

    private final String input;
    private int pos = 0;
    private int line = 1;

    public Lexer(String input) {
        this.input = input;
    }

    private char peek() {
        if (pos >= input.length()) return '\0';
        return input.charAt(pos);
    }

    private char peekNext() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private char advance() {
        char c = peek();
        pos++;
        if (c == '\n') line++;
        return c;
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(peek())) advance();
    }

    private void skipComment() {
        if (peek() == '/' && peekNext() == '/') {
            while (peek() != '\n' && peek() != '\0') advance();
        } else if (peek() == '/' && peekNext() == '*') {
            int startLine = line;
            advance();
            advance();
            while (true) {
                if (peek() == '\0') {
                    System.err.println("Error: Unclosed multi-line comment starting at line " + startLine);
                    return;
                }
                if (peek() == '*' && peekNext() == '/') {
                    advance();
                    advance();
                    break;
                }
                advance();
            }
        }
    }

    private Token tokenizeIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;

        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            sb.append(advance());
        }

        String word = sb.toString();
        if (keywords.contains(word)) {
            return new Token(TokenType.KEYWORD, word, startLine);
        } else {
            return new Token(TokenType.IDENTIFIER, word, startLine);
        }
    }

    private Token tokenizeNumber() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;
        boolean hasDot = false;

        if (peek() == '.') { // handle leading dot numbers like .5
            hasDot = true;
            sb.append(advance());
            if (!Character.isDigit(peek())) {
                System.err.println("Error: Invalid number format at line " + startLine);
                return null;
            }
        }

        while (Character.isDigit(peek()) || peek() == '.') {
            if (peek() == '.') {
                if (hasDot) {
                    System.err.println("Error: Invalid number format at line " + startLine);
                    break;
                }
                hasDot = true;
            }
            sb.append(advance());
        }

        // Check for trailing invalid identifier chars like 12a
        if (Character.isLetter(peek())) {
            System.err.println("Error: Invalid character in number at line " + startLine);
            while (Character.isLetterOrDigit(peek())) advance();
        }

        return new Token(TokenType.NUMBER, sb.toString(), startLine);
    }

    private Token tokenizeString() {
        StringBuilder sb = new StringBuilder();
        int startLine = line;
        advance(); // skip opening "

        while (peek() != '"' && peek() != '\0') {
            if (peek() == '\n') {
                System.err.println("Error: Unclosed string starting at line " + startLine);
                break;
            }
            sb.append(advance());
        }

        if (peek() == '"') advance(); // skip closing "

        return new Token(TokenType.STRING, sb.toString(), startLine);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            skipWhitespace();

            if (peek() == '/' && (peekNext() == '/' || peekNext() == '*')) {
                skipComment();
                continue;
            }

            char c = peek();

            if (Character.isLetter(c) || c == '_') {
                tokens.add(tokenizeIdentifierOrKeyword());
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                Token numberToken = tokenizeNumber();
                if (numberToken != null) tokens.add(numberToken);
                continue;
            }

            if (c == '"') {
                tokens.add(tokenizeString());
                continue;
            }

            if ("+-*/=".indexOf(c) != -1) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c), line));
                advance();
                continue;
            }

            if (";,(){}".indexOf(c) != -1) {
                tokens.add(new Token(TokenType.DELIMITER, String.valueOf(c), line));
                advance();
                continue;
            }

            if (c != '\0') {
                System.err.println("Unexpected character: '" + c + "' at line " + line);
            }
            advance();
        }

        tokens.add(new Token(TokenType.EOF, "EOF", line));
        return tokens;
    }
}