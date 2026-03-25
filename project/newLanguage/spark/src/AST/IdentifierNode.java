package AST;

public class IdentifierNode extends ASTNode {
    public String name;

    public IdentifierNode(String name, int line) {
        this.name = name;
        this.line = line;
    }
}
