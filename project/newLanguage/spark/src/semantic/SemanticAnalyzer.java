package semantic;

import AST.*;

import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {
    private final Map<String, String> symbolTable = new HashMap<>(); // variable -> type

    public void analyze(Program program) {
        for (ASTNode stmt : program.statements) {
            analyzeNode(stmt);
        }
    }

    private void analyzeNode(ASTNode node) {
        if (node == null) return;

        if (node instanceof InputStatement input) {
            // Analyze the value expression first
            String type = null;
            if (input.value != null) {
                type = analyzeExpression(input.value);
            }
            // Register variable with type
            symbolTable.put(input.identifier, type != null ? type : "unknown");
        } else if (node instanceof BinaryOpNode b) {
            analyzeExpression(b);
        }
    }

    private String analyzeExpression(ASTNode expr) {
        if (expr instanceof NumberNode) return "number";
        if (expr instanceof StringNode) return "string";
        if (expr instanceof IdentifierNode id) {
            if (!symbolTable.containsKey(id.name)) {
                System.err.println("Semantic Error: Undefined variable '" + id.name + "' at line " + id.line);
                return "error";
            }
            return symbolTable.get(id.name);
        }
        if (expr instanceof BinaryOpNode b) {
            String leftType = analyzeExpression(b.left);
            String rightType = analyzeExpression(b.right);

            if (!leftType.equals(rightType)) {
                System.err.println("Semantic Error: Type mismatch in operation '" + b.operator + "' at line " + b.line);
                return "error";
            }
            return leftType;
        }
        return "unknown";
    }

    public Map<String, String> getSymbolTable() {
        return symbolTable;
    }
}