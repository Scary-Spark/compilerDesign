package AST;

public class PrintStatement extends ASTNode {
    public ASTNode expression;

    public PrintStatement(ASTNode expression, int line) {
        this.expression = expression;
        this.line = line;
    }
}