package executor;

import AST.Stmt;
import lexicalAnalysis.Lexer;
import lexicalAnalysis.Token;
import parser.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lexicalAnalysis.Lexer.debuggingMode;

public class Executor {
    static List<Token> allTokens = new ArrayList<>();

    public static void execute(String fileName) {
        boolean blockComment = false;
        int blockStartLine = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (blockComment) {
                    if (line.contains("*/")) {
                        blockComment = false;
                        line = line.substring(line.indexOf("*/") + 2);
                    } else continue;
                }

                int startBlockPos = line.indexOf("/*");
                if (startBlockPos != -1) {
                    int endBlockPos = line.indexOf("*/", startBlockPos + 2);
                    if (endBlockPos != -1) {
                        line = line.substring(0, startBlockPos) + line.substring(endBlockPos + 2);
                        startBlockPos = line.indexOf("/*");
                    } else {
                        blockComment = true;
                        blockStartLine = lineNumber;
                        line = line.substring(0, startBlockPos);
                    }
                }

                int singleComment = line.indexOf("//");
                if (singleComment != -1)
                    line = line.substring(0, singleComment);

                if (line.trim().isEmpty()) continue;

                List<Token> tokens = Lexer.lexer(line, lineNumber);
                allTokens.addAll(tokens);


                if (Lexer.debuggingMode) {
                    System.out.print(lineNumber + ": ");
                    for (Token t : tokens)
                        t.print();
                    System.out.println();
                }
            }

            if (blockComment) System.err.println("Error: Unclosed block comment starting at line " + blockStartLine);


        } catch (Exception e) {
            System.out.println("Can't open file: " + fileName);
        }

        if (debuggingMode) {
            System.out.println("\n\n=======Parser=======");
        }

        Parser parser = new Parser(allTokens);
        List<Stmt> program = parser.parseProgram(); // new method that collects statements
        Map<String, Object> context = new HashMap<>();

        for (Stmt stmt : program) {
            stmt.execute(context);
        }
    }
}