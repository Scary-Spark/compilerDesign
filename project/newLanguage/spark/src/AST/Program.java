package AST;

import java.util.List;

public class Program extends ASTNode {
    public List<ASTNode> statements;

    public Program(List<ASTNode> statements) {
        this.statements = statements;
    }
}
