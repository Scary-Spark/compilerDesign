package lexicalAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer {
    public static boolean debuggingMode = true;

    public static List<String> reservedKeywords = Arrays.asList(
            "input",
            "read",
            "const",
            "printn",
            "print",
            "if",
            "elif",
            "else",
            "switch",
            "case",
            "break",
            "continue",
            "function",
            "return",
            "for",
            "while",
            "true",
            "false"
    );

    public static List<Character> reservedSymbols = Arrays.asList(
            ';',
            ',',
            '(',
            ')',
            '{',
            '}',
            '[',
            ']'
    );

    public static List<String> reservedOperators = Arrays.asList(
            "==",
            "!=",
            "<=",
            ">=",
            "++",
            "--",
            "+",
            "-",
            "*",
            "/",
            "%",
            "=",
            "<",
            ">"
    );

    public static String keywordPatternCreator() {
        StringBuilder pattern = new StringBuilder("\\b(");
        boolean start = true;
        for (String kw : reservedKeywords) {
            if (!start) pattern.append("|");
            pattern.append(kw);
            start = false;
        }
        pattern.append(")\\b");
        return pattern.toString();
    }

    public static String symbolPatternCreator() {
        StringBuilder pattern = new StringBuilder("[");
        for (char c : reservedSymbols) {
            if ("[](){}".contains("" + c)) pattern.append("\\");
            pattern.append(c);
        }
        pattern.append("]");
        return pattern.toString();
    }

    public static boolean isOperatorStart(String line, int i) {
        for (String op : reservedOperators) {
            if (i + op.length() <= line.length() && line.substring(i, i + op.length()).equals(op))
                return true;
        }
        return false;
    }

    public static List<Token> lexer(String line, int lineNumber) {
        List<Token> tokens = new ArrayList<>();

        Pattern keywordPattern = Pattern.compile(keywordPatternCreator());
        Pattern numberPattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?([eE][-+]?[0-9]+)?");
        Pattern identifierPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
        Pattern symbolPattern = Pattern.compile(symbolPatternCreator());

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '"') {
                int start = i + 1;
                int end = line.indexOf('"', start);
                if (end == -1) {
                    System.err.println("Error: Unmatched quotes at line " + lineNumber);
                    break;
                }
                tokens.add(new Token(Token.TokenType.STRING, line.substring(start, end), lineNumber));
                i = end + 1;
                continue;
            }

            String s = "" + c;
            if (symbolPattern.matcher(s).matches()) {
                tokens.add(new Token(Token.TokenType.SYMBOL, s, lineNumber));
                i++;
                continue;
            }

            boolean matchedOp = false;
            for (String op : reservedOperators) {
                if (i + op.length() <= line.length() && line.substring(i, i + op.length()).equals(op)) {
                    tokens.add(new Token(Token.TokenType.OPERATOR, op, lineNumber));
                    i += op.length();
                    matchedOp = true;
                    break;
                }
            }
            if (matchedOp) continue;

            int start = i;
            while (i < line.length() && !Character.isWhitespace(line.charAt(i))
                    && !symbolPattern.matcher("" + line.charAt(i)).matches()
                    && !isOperatorStart(line, i)) {
                i++;
            }
            String word = line.substring(start, i);

            if (keywordPattern.matcher(word).matches())
                tokens.add(new Token(Token.TokenType.KEYWORD, word, lineNumber));
            else if (numberPattern.matcher(word).matches())
                tokens.add(new Token(Token.TokenType.NUMBER, word, lineNumber));
            else if (identifierPattern.matcher(word).matches())
                tokens.add(new Token(Token.TokenType.IDENTIFIER, word, lineNumber));
            else {
                tokens.add(new Token(Token.TokenType.UNKNOWN, word, lineNumber));
                System.err.println("Error: Unknown token '" + word + "' at line " + lineNumber);
            }
        }
        return tokens;
    }
}
