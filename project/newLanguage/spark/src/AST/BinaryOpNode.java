package AST;

public class BinaryOpNode extends ASTNode {
    public ASTNode left;
    public String operator;
    public ASTNode right;

    public BinaryOpNode(ASTNode left, String operator, ASTNode right, int line) {
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.line = line;
    }
}