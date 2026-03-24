package lexicalAnalysis;

public class Token {
    public TokenType type;
    public String value;
    public int line;

    public Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public void print() {
        String typeName = type.toString();
        System.out.print("<" + typeName + ", " + value + ", " + line + "> ");
    }

    public enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        KEYWORD,
        OPERATOR,
        SYMBOL,
        UNKNOWN
    }
}
