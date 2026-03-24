package parser;

import lexicalAnalysis.Token;
import lexicalAnalysis.Token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static lexicalAnalysis.Lexer.debuggingMode;

public class Parser {
    private List<Token> tokens;
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

    public void parse() {
        while (cursor < tokens.size()) {
            parseStatement();
        }
    }

    private void parseInput() {
        consume();
        Token var = consume();

        if (var == null || var.type != TokenType.IDENTIFIER) {
            System.err.println("Error: Expected identifier after input at line " + var.line);
            return;
        }

        if (matchSymbol("[")) {
            consume();
            if (!matchSymbol("]")) {
                System.err.println("Error: Expected ] for array at line " + var.line);
            }
            consume();

            if (matchOperator("=")) {
                consume();
                List<String> values = new ArrayList<>();

                while (!matchSymbol(";")) {
                    Token val = consume();
                    if (val.type == TokenType.STRING || val.type == TokenType.NUMBER) {
                        values.add(val.value);
                    }
                    if (matchSymbol(",")) consume();
                }

                if (debuggingMode) {
                    System.out.println("Parsed Input Array: " + var.value + " = " + values);
                }
            }
        } else if (matchOperator("=")) {
            consume();
            parseExpression();
        }

        expectSemicolon("input");
        if (debuggingMode)
            System.out.println("Parsed Input: " + var.value);
    }

    private void parseConst() {
        consume();
        Token var = consume();

        if (var.type != TokenType.IDENTIFIER) {
            System.err.println("Error: Expected identifier after const at line " + var.line);
            return;
        }

        if (!matchOperator("=")) {
            System.err.println("Error: Expected = after const at line " + var.line);
            return;
        }

        consume();
        parseExpression();
        expectSemicolon("const");

        if (debuggingMode)
            System.out.println("Parsed Const: " + var.value);
    }

    private void parseRead() {
        consume();
        Token var = consume();

        if (var.type != TokenType.IDENTIFIER)
            System.err.println("Error: Expected identifier after read at line " + var.line);

        expectSemicolon("read");

        if (debuggingMode)
            System.out.println("Parsed Read: " + var.value);
    }

    private void parseAssignment() {
        Token var = consume();

        if (matchOperator("++") || matchOperator("--")) {
            Token op = consume();
            expectSemicolon("increment/decrement");

            if (debuggingMode)
                System.out.println("Parsed " + op.value + " for " + var.value);
            return;
        }

        if (!matchOperator("=")) {
            System.err.println("Error: Expected '=' in assignment at line " + var.line);
            synchronize();
            return;
        }

        consume();
        parseExpression();
        expectSemicolon("assignment");

        if (debuggingMode)
            System.out.println("Parsed Assignment: " + var.value);
    }

    private void synchronize() {
        while (peek() != null) {
            if (matchSymbol(";")) {
                consume();
                return;
            }
            consume();
        }
    }

    private void parsePrint(boolean newLine) {
        consume();
        parseExpression();
        expectSemicolon("print");

        if (debuggingMode) {
            System.out.println("Parsed Print" + (newLine ? "n" : ""));
        }
    }

    private void parseIf() {
        consume();
        expectSymbol("(", "if");
        parseExpression();
        expectSymbol(")", "if");
        expectSymbol("{", "if");

        parseBlock();

        while (matchKeyword("elif")) {
            parseElif();
        }

        if (matchKeyword("else")) {
            parseElse();
        }
    }

    private void parseElif() {
        consume();
        expectSymbol("(", "elif");
        parseExpression();
        expectSymbol(")", "elif");
        expectSymbol("{", "elif");
        parseBlock();
    }

    private void parseElse() {
        consume();
        expectSymbol("{", "else");
        parseBlock();
    }

    private void parseWhile() {
        consume();
        expectSymbol("(", "while");
        parseExpression();
        expectSymbol(")", "while");
        expectSymbol("{", "while");

        parseBlock();
        if (debuggingMode)
            System.out.println("Parsed While Loop");
    }

    private void parseFunction() {
        consume();
        Token name = consume();

        if (name.type != TokenType.IDENTIFIER) {
            System.err.println("Error: Expected function name at line " + name.line);
            return;
        }

        expectSymbol("(", "function");

        List<String> args = new ArrayList<>();
        while (!matchSymbol(")")) {
            Token arg = consume();
            if (arg.type == TokenType.IDENTIFIER) {
                args.add(arg.value);
            }
            if (matchSymbol(",")) consume();
        }

        consume();
        expectSymbol("{", "function");

        parseBlock();
        if (debuggingMode)
            System.out.println("Parsed Function: " + name.value + " args=" + args);
    }

    private void parseReturn() {
        consume();
        parseExpression();
        expectSemicolon("return");
        if (debuggingMode)
            System.out.println("Parsed Return");
    }

    private void parseBlock() {
        while (!matchSymbol("}") && peek() != null) {
            parseStatement();
        }
        expectSymbol("}", "block");
    }

    private void parseExpression() {
        parseEquality();
    }

    private void parseEquality() {
        parseComparison();
        while (matchOperator("==") || matchOperator("!=")) {
            consume();
            parseComparison();
        }
    }

    private void parseComparison() {
        parseTerm();
        while (matchOperator(">") || matchOperator("<") ||
                matchOperator(">=") || matchOperator("<=")) {
            consume();
            parseTerm();
        }
    }

    private void parseTerm() {
        parseFactor();
        while (matchOperator("+") || matchOperator("-")) {
            consume();
            parseFactor();
        }
    }

    private void parseFactor() {
        parseUnary();
        while (matchOperator("*") || matchOperator("/") || matchOperator("%")) {
            consume();
            parseUnary();
        }
    }

    private void parseUnary() {
        if (matchOperator("-") || matchOperator("!")) {
            consume();
            parseUnary();
        } else {
            parsePrimary();
        }
    }

    private void parsePrimary() {
        Token t = peek();

        if (t.type == TokenType.NUMBER ||
                t.type == TokenType.STRING ||
                (t.type == TokenType.KEYWORD && (t.value.equals("true") || t.value.equals("false")))) {
            consume();
            return;
        }

        if (t.type == TokenType.IDENTIFIER) {
            consume(); // consume function name or variable

            if (matchSymbol("(")) {
                consume(); // (

                if (!matchSymbol(")")) {
                    parseExpression();
                    while (matchSymbol(",")) {
                        consume();
                        parseExpression();
                    }
                }

                expectSymbol(")", "function call");
            }

            return;
        }

        if (matchSymbol("(")) {
            consume();
            parseExpression();
            expectSymbol(")", "expression");
            return;
        }

        System.err.println("Error: Unexpected token in expression: " + t.value + " at line " + t.line);
        consume();
    }

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

    private void parseStatement() {
        Token t = peek();
        if (t == null) return;

        if (t.type == TokenType.KEYWORD) {
            switch (t.value) {
                case "input" -> parseInput();
                case "const" -> parseConst();
                case "read" -> parseRead();
                case "print" -> parsePrint(false);
                case "printn" -> parsePrint(true);
                case "if" -> parseIf();
                case "while" -> parseWhile();
                case "function" -> parseFunction();
                case "return" -> parseReturn();
                default -> {
                    System.err.println("Error: Unknown keyword " + t.value + " at line " + t.line);
                    consume();
                }
            }
        } else if (t.type == TokenType.IDENTIFIER) {
            parseAssignment();
        } else {
            System.err.println("Error: Unexpected token " + t.value + " at line " + t.line);
            consume();
        }
    }
}