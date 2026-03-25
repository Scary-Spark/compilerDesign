package executor;

import AST.*;
import lexicalAnalysis.Token;
import semantic.SemanticAnalyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Executor {
    private final Map<String, Object> runtimeSymbols = new HashMap<>();
    public boolean debuggingMode = true;

    public void execute(Program program, List<Token> tokens) {
        // --- Debug tokens ---
        if (debuggingMode && tokens != null) {
            System.out.println("========== TOKENS ==========");
            int prev = -1;
            for (Token t : tokens) {
                if (prev != -1 && t.line != prev) System.out.println();
                System.out.println(t);
                prev = t.line;
            }
            System.out.println("============================\n");
        }

        // --- Semantic Analysis ---
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);

        // --- Debug AST ---
        if (debuggingMode) {
            System.out.println("========== AST ==========");
            printAST(program, 0);
            System.out.println("=========================\n");
        }

        // --- Execution ---
        System.out.println("========== Execution ==========");
        for (ASTNode stmt : program.statements) {
            executeNode(stmt);
        }
        System.out.println("==============================");
    }

    private void printAST(ASTNode node, int indent) {
        String prefix = "  ".repeat(indent);
        if (node == null) return;

        if (node instanceof Program p) {
            System.out.println(prefix + "Program");
            for (ASTNode s : p.statements) printAST(s, indent + 1);
        } else if (node instanceof InputStatement i) {
            System.out.println(prefix + "InputStatement -> " + i.identifier);
            if (i.value != null) printAST(i.value, indent + 1);
        } else if (node instanceof PrintStatement p) {
            System.out.println(prefix + "PrintStatement");
            printAST(p.expression, indent + 1);
        } else if (node instanceof NumberNode n) {
            System.out.println(prefix + "Number -> " + n.value);
        } else if (node instanceof StringNode s) {
            System.out.println(prefix + "String -> " + s.value);
        } else if (node instanceof IdentifierNode id) {
            System.out.println(prefix + "Identifier -> " + id.name);
        } else if (node instanceof BinaryOpNode b) {
            System.out.println(prefix + "BinaryOp -> " + b.operator);
            printAST(b.left, indent + 1);
            printAST(b.right, indent + 1);
        }
    }

    private Object executeNode(ASTNode node) {
        if (node == null) return null;

        if (node instanceof InputStatement input) {
            Object value = input.value != null ? executeNode(input.value) : null;
            runtimeSymbols.put(input.identifier, value);
            System.out.println("Input " + input.identifier + " = " + value);
            return value;
        }

        if (node instanceof PrintStatement p) {
            Object value = executeNode(p.expression);
            System.out.println(value);
            return value;
        }

        if (node instanceof NumberNode n) {
            return n.value.contains(".") ? Double.parseDouble(n.value) : Integer.parseInt(n.value);
        }

        if (node instanceof StringNode s) {
            return s.value;
        }

        if (node instanceof IdentifierNode id) {
            if (!runtimeSymbols.containsKey(id.name)) {
                System.err.println("Runtime Error: Undefined variable '" + id.name + "' at line " + id.line);
                return null;
            }
            return runtimeSymbols.get(id.name);
        }

        if (node instanceof BinaryOpNode b) {
            Object left = executeNode(b.left);
            Object right = executeNode(b.right);

            if (left instanceof Number l && right instanceof Number r) {
                return switch (b.operator) {
                    case "+" -> l.doubleValue() + r.doubleValue();
                    case "-" -> l.doubleValue() - r.doubleValue();
                    case "*" -> l.doubleValue() * r.doubleValue();
                    case "/" -> {
                        if (r.doubleValue() == 0) {
                            System.err.println("Runtime Error: Division by zero at line " + b.line);
                            yield null;
                        } else yield l.doubleValue() / r.doubleValue();
                    }
                    default -> {
                        System.err.println("Runtime Error: Unsupported operation '" + b.operator + "' at line " + b.line);
                        yield null;
                    }
                };
            } else {
                System.err.println("Runtime Error: Unsupported operation '" + b.operator + "' at line " + b.line);
                return null;
            }
        }
        return null;
    }
}