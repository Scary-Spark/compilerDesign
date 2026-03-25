package AST;

public class InputStatement extends ASTNode {
    public String identifier;
    public ASTNode value;

    public InputStatement(String identifier, ASTNode value, int line) {
        this.identifier = identifier;
        this.value = value;
        this.line = line;
    }
}
