package parser;

import AST.Expr;
import AST.Stmt;
import lexicalAnalysis.Token;
import lexicalAnalysis.Token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int cursor = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        if (cursor < tokens.size())
            return tokens.get(cursor);
        return null;
    }

    private Token consume() {
        if (cursor < tokens.size())
            return tokens.get(cursor++);
        return null;
    }

    private boolean matchOperator(String op) {
        Token t = peek();
        return t != null && t.type == TokenType.OPERATOR && t.value.equals(op);
    }

    private boolean matchSymbol(String sym) {
        Token t = peek();
        return t != null && t.type == TokenType.SYMBOL && t.value.equals(sym);
    }

    private boolean matchKeyword(String kw) {
        Token t = peek();
        return t != null && t.type == TokenType.KEYWORD && t.value.equals(kw);
    }

    public List<Stmt> parseProgram() {
        List<Stmt> statements = new ArrayList<>();
        while (cursor < tokens.size()) {
            Stmt stmt = parseStatement();
            if (stmt != null) statements.add(stmt);
        }
        return statements;
    }

    private Stmt parseStatement() {
        Token t = peek();
        if (t == null) return null;

        if (t.type == TokenType.KEYWORD) {
            switch (t.value) {
                case "input" -> {
                    return parseInputStmt();
                }
                case "const" -> {
                    return parseConstStmt();
                }
                case "read" -> {
                    return parseReadStmt();
                }
                case "print" -> {
                    return parsePrint(false);
                }
                case "printn" -> {
                    return parsePrint(true);
                }
                case "if" -> {
                    return parseIfStmt();
                }
                case "while" -> {
                    return parseWhileStmt();
                }
                case "return" -> {
                    return parseReturnStmt();
                }
                default -> {
                    System.err.println("Unknown keyword " + t.value + " at line " + t.line);
                    consume();
                    return null;
                }
            }
        } else if (t.type == TokenType.IDENTIFIER) {
            return parseAssignmentStmt();
        } else {
            System.err.println("Unexpected token " + t.value + " at line " + t.line);
            consume();
            return null;
        }
    }

    // ==================== Statements ====================

    private Stmt parseInputStmt() {
        Token t = consume(); // input
        Token var = consume();

        if (var.type != TokenType.IDENTIFIER) {
            System.err.println("Expected identifier after input at line " + var.line);
            return null;
        }

        Expr expr = null;
        if (matchOperator("=")) {
            consume();
            expr = parseExpression();
        }

        expectSemicolon("input");
        return new Stmt.Input(var.value, expr, var.line);
    }

    private Stmt parseConstStmt() {
        Token t = consume(); // const
        Token var = consume();

        if (var.type != TokenType.IDENTIFIER) {
            System.err.println("Expected identifier after const at line " + var.line);
            return null;
        }

        if (!matchOperator("=")) {
            System.err.println("Expected '=' after const at line " + var.line);
            return null;
        }

        consume(); // =
        Expr expr = parseExpression();
        expectSemicolon("const");

        return new Stmt.Const(var.value, expr, var.line);
    }

    private Stmt parseReadStmt() {
        Token t = consume(); // read
        Token var = consume();

        if (var.type != TokenType.IDENTIFIER) {
            System.err.println("Expected identifier after read at line " + var.line);
            return null;
        }

        expectSemicolon("read");
        return new Stmt.Input(var.value, null, var.line);
    }

    private Stmt parsePrint(boolean newLine) {
        Token t = consume(); // print / printn
        Expr expr = parseExpression();
        expectSemicolon("print");
        return new Stmt.Print(expr, newLine, t.line);
    }

    private Stmt parseAssignmentStmt() {
        Token var = consume(); // variable

        if (matchOperator("=")) {
            consume();
            Expr expr = parseExpression();
            expectSemicolon("assignment");
            return new Stmt.Assignment(var.value, expr, var.line);
        } else if (matchOperator("++") || matchOperator("--")) {
            Token op = consume();
            expectSemicolon("increment/decrement");
            Expr expr = new Expr.Binary(new Expr.Variable(var.value, var.line), op.value.equals("++") ? "+" : "-", new Expr.Literal(1, var.line), var.line);
            return new Stmt.Assignment(var.value, expr, var.line);
        } else {
            System.err.println("Expected assignment operator at line " + var.line);
            synchronize();
            return null;
        }
    }

    private Stmt parseIfStmt() {
        Token t = consume(); // if
        expectSymbol("(", "if");
        Expr condition = parseExpression();
        expectSymbol(")", "if");
        expectSymbol("{", "if");

        List<Stmt> thenBranch = parseBlock();

        List<Stmt> elseBranch = null;
        if (matchKeyword("else")) {
            consume();
            expectSymbol("{", "else");
            elseBranch = parseBlock();
        }

        return new Stmt.If(condition, thenBranch, elseBranch, t.line);
    }

    private Stmt parseWhileStmt() {
        Token t = consume(); // while
        expectSymbol("(", "while");
        Expr condition = parseExpression();
        expectSymbol(")", "while");
        expectSymbol("{", "while");

        List<Stmt> body = parseBlock();
        return new Stmt.While(condition, body, t.line);
    }

    private Stmt parseReturnStmt() {
        Token t = consume(); // return
        Expr expr = parseExpression();
        expectSemicolon("return");
        return new Stmt.Return(expr, t.line);
    }

    private List<Stmt> parseBlock() {
        List<Stmt> statements = new ArrayList<>();
        while (!matchSymbol("}") && peek() != null) {
            Stmt stmt = parseStatement();
            if (stmt != null) statements.add(stmt);
        }
        expectSymbol("}", "block");
        return statements;
    }

    // ==================== Expressions ====================

    private Expr parseExpression() {
        return parseEquality();
    }

    private Expr parseEquality() {
        Expr expr = parseComparison();
        while (matchOperator("==") || matchOperator("!=")) {
            String op = consume().value;
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, op, right, peek() != null ? peek().line : -1);
        }
        return expr;
    }

    private Expr parseComparison() {
        Expr expr = parseTerm();
        while (matchOperator(">") || matchOperator("<") || matchOperator(">=") || matchOperator("<=")) {
            String op = consume().value;
            Expr right = parseTerm();
            expr = new Expr.Binary(expr, op, right, peek() != null ? peek().line : -1);
        }
        return expr;
    }

    private Expr parseTerm() {
        Expr expr = parseFactor();
        while (matchOperator("+") || matchOperator("-")) {
            String op = consume().value;
            Expr right = parseFactor();
            expr = new Expr.Binary(expr, op, right, peek() != null ? peek().line : -1);
        }
        return expr;
    }

    private Expr parseFactor() {
        Expr expr = parseUnary();
        while (matchOperator("*") || matchOperator("/") || matchOperator("%")) {
            String op = consume().value;
            Expr right = parseUnary();
            expr = new Expr.Binary(expr, op, right, peek() != null ? peek().line : -1);
        }
        return expr;
    }

    private Expr parseUnary() {
        if (matchOperator("-") || matchOperator("!")) {
            String op = consume().value;
            Expr right = parseUnary();
            return new Expr.Unary(op, right, peek() != null ? peek().line : -1);
        } else {
            return parsePrimary();
        }
    }

    private Expr parsePrimary() {
        Token t = consume();
        if (t == null) throw new RuntimeException("Unexpected end of input");

        if (t.type == TokenType.NUMBER) return new Expr.Literal(Double.parseDouble(t.value), t.line);
        if (t.type == TokenType.STRING) return new Expr.Literal(t.value, t.line);
        if (t.type == TokenType.KEYWORD && (t.value.equals("true") || t.value.equals("false")))
            return new Expr.Literal(Boolean.parseBoolean(t.value), t.line);

        if (t.type == TokenType.IDENTIFIER) {
            if (matchSymbol("(")) {
                consume(); // (
                List<Expr> args = new ArrayList<>();
                if (!matchSymbol(")")) {
                    args.add(parseExpression());
                    while (matchSymbol(",")) {
                        consume();
                        args.add(parseExpression());
                    }
                }
                expectSymbol(")", "function call");
                return new Expr.Call(null, args, t.line);
            } else {
                return new Expr.Variable(t.value, t.line);
            }
        }

        if (t.value.equals("(")) {
            Expr expr = parseExpression();
            expectSymbol(")", "expression");
            return expr;
        }

        throw new RuntimeException("Unexpected token in expression: " + t.value + " at line " + t.line);
    }

    // ==================== Helpers ====================

    private void expectSemicolon(String where) {
        if (!matchSymbol(";")) {
            Token t = peek();
            int line = (t != null) ? t.line : -1;
            System.err.println("Error: Expected ';' after " + where + " at line " + line);
        } else consume();
    }

    private void expectSymbol(String sym, String where) {
        if (!matchSymbol(sym)) {
            Token t = peek();
            int line = (t != null) ? t.line : -1;
            System.err.println("Error: Expected '" + sym + "' after " + where + " at line " + line);
        } else consume();
    }

    private void synchronize() {
        while (peek() != null && !matchSymbol(";")) consume();
        if (matchSymbol(";")) consume();
    }
}