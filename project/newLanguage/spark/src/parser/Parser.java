package parser;

import AST.*;
import lexicalAnalysis.Token;
import lexicalAnalysis.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        if (pos >= tokens.size()) return new Token(TokenType.EOF, "EOF", tokens.get(tokens.size() - 1).line);
        return tokens.get(pos);
    }

    private Token advance() {
        Token c = peek();
        pos++;
        return c;
    }

    private boolean match(TokenType type) {
        if (peek().type == type) {
            advance();
            return true;
        }
        return false;
    }

    private void expect(TokenType type, String msg) {
        if (!match(type)) {
            System.err.println("Syntax Error at line " + peek().line + ": " + msg + ", found '" + peek().value + "'");
        }
    }

    public Program parseProgram() {
        List<ASTNode> statements = new ArrayList<>();
        while (peek().type != TokenType.EOF) {
            ASTNode stmt = parseStatement();
            if (stmt != null) statements.add(stmt);
        }
        return new Program(statements);
    }

    private ASTNode parseStatement() {
        Token token = peek();
        if (token.type == TokenType.KEYWORD) {
            switch (token.value) {
                case "input" -> {
                    advance(); // consume input
                    Token idToken = peek();
                    if (idToken.type != TokenType.IDENTIFIER) {
                        System.err.println("Syntax Error at line " + token.line + ": expected identifier after 'input', found '" + idToken.value + "'");
                        advance();
                        return null;
                    }
                    if (!idToken.value.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                        System.err.println("Syntax Error at line " + idToken.line + ": invalid identifier '" + idToken.value + "'");
                        advance();
                        return null;
                    }
                    advance(); // consume identifier
                    ASTNode value = null;
                    if (peek().type == TokenType.OPERATOR && peek().value.equals("=")) {
                        advance();
                        value = parseExpression();
                    }
                    expect(TokenType.DELIMITER, "expected ';' after statement");
                    return new InputStatement(idToken.value, value, token.line);
                }
                case "print" -> {
                    advance(); // consume print
                    ASTNode expr = parseExpression();
                    expect(TokenType.DELIMITER, "expected ';' after print statement");
                    return new PrintStatement(expr, token.line);
                }
            }
        }
        return parseExpressionStatement();
    }

    private ASTNode parseExpressionStatement() {
        ASTNode expr = parseExpression();
        expect(TokenType.DELIMITER, "expected ';' after expression");
        return expr;
    }

    private ASTNode parseExpression() {
        ASTNode node = parseTerm();
        while (peek().type == TokenType.OPERATOR && (peek().value.equals("+") || peek().value.equals("-"))) {
            Token op = advance();
            ASTNode right = parseTerm();
            node = new BinaryOpNode(node, op.value, right, op.line);
        }
        return node;
    }

    private ASTNode parseTerm() {
        ASTNode node = parseFactor();
        while (peek().type == TokenType.OPERATOR && (peek().value.equals("*") || peek().value.equals("/"))) {
            Token op = advance();
            ASTNode right = parseFactor();
            node = new BinaryOpNode(node, op.value, right, op.line);
        }
        return node;
    }

    private ASTNode parseFactor() {
        Token token = peek();
        switch (token.type) {
            case NUMBER -> {
                if (!token.value.matches("\\d+(\\.\\d+)?|\\.\\d+")) {
                    System.err.println("Syntax Error at line " + token.line + ": invalid number '" + token.value + "'");
                    advance();
                    return null;
                }
                advance();
                return new NumberNode(token.value, token.line);
            }
            case STRING -> {
                advance();
                return new StringNode(token.value, token.line);
            }
            case IDENTIFIER -> {
                advance();
                if (!token.value.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    System.err.println("Syntax Error at line " + token.line + ": invalid identifier '" + token.value + "'");
                    return null;
                }
                return new IdentifierNode(token.value, token.line);
            }
            case DELIMITER -> {
                if (token.value.equals("(")) {
                    advance();
                    ASTNode node = parseExpression();
                    expect(TokenType.DELIMITER, "expected ')' after expression");
                    return node;
                }
            }
        }
        System.err.println("Syntax Error at line " + token.line + ": unexpected token '" + token.value + "'");
        advance();
        return null;
    }
}