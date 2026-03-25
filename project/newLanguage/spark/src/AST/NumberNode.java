package AST;

public class NumberNode extends ASTNode {
    public String value;

    public NumberNode(String value, int line) {
        this.value = value;
        this.line = line;
    }
}
