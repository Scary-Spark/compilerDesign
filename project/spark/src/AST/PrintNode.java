package AST;

public class PrintNode extends Node {
    public String expression;
    public boolean newLine;
    // true = printn, false = print

    public PrintNode(String expression, boolean newLine, int line) {
        this.expression = expression;
        this.newLine = newLine;
        super.line = line;
    }
}
