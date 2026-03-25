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
        if (pos >= input.length())
            return '\0';
        return input.charAt(pos);
    }

    private char peekNext() {
        if (pos + 1 >= input.length())
            return '\0';
        return input.charAt(pos + 1);
    }

    private char advance() {
        char c = peek();
        pos++;
        if (c == '\n') line++;
        return c;
    }

    // FIX 1: Better whitespace skipping (handles Windows \r\n)
    private void skipWhitespace() {
        while (true) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else {
                break;
            }
        }
    }

    // FIX 2: Safer comment skipping
    private void skipComment() {
        // Single line comment
        if (peek() == '/' && peekNext() == '/') {
            while (peek() != '\n' && peek() != '\0') {
                advance();
            }
        }
        // Multi-line comment
        else if (peek() == '/' && peekNext() == '*') {
            advance(); // /
            advance(); // *

            while (true) {
                if (peek() == '\0') break;

                if (peek() == '*' && peekNext() == '/') {
                    advance(); // *
                    advance(); // /
                    break;
                }

                advance();
            }
        }
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            skipWhitespace();

            // Skip comments
            if (peek() == '/' && (peekNext() == '/' || peekNext() == '*')) {
                skipComment();
                continue;
            }

            char c = peek();

            // Identifier or Keyword
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isLetterOrDigit(peek())) {
                    sb.append(advance());
                }

                String word = sb.toString();

                if (keywords.contains(word)) {
                    tokens.add(new Token(TokenType.KEYWORD, word, line));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word, line));
                }

                continue;
            }

            // Number (int or float)
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isDigit(peek()) || peek() == '.') {
                    sb.append(advance());
                }
                tokens.add(new Token(TokenType.NUMBER, sb.toString(), line));
                continue;
            }

            // String
            if (c == '"') {
                advance();
                StringBuilder sb = new StringBuilder();

                while (peek() != '"' && peek() != '\0') {
                    sb.append(advance());
                }

                advance(); // closing "
                tokens.add(new Token(TokenType.STRING, sb.toString(), line));
                continue;
            }

            // Operators
            if ("+-*/=".indexOf(c) != -1) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c), line));
                advance();
                continue;
            }

            // Delimiters
            if (";,(){}".indexOf(c) != -1) {
                tokens.add(new Token(TokenType.DELIMITER, String.valueOf(c), line));
                advance();
                continue;
            }

            // FIX 3: Avoid false unexpected character errors
            if (c != '\0' && !Character.isWhitespace(c)) {
                System.out.println("Unexpected character: '" + c + "' at line " + line);
            }
            advance();
        }

        tokens.add(new Token(TokenType.EOF, "EOF", line));
        return tokens;
    }
}