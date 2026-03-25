package AST;

public class StringNode extends ASTNode {
    public String value;

    public StringNode(String value, int line) {
        this.value = value;
        this.line = line;
    }
}