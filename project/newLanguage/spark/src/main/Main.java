package main;

import AST.Program;
import executor.Executor;
import lexicalAnalysis.Lexer;
import lexicalAnalysis.Token;
import parser.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String fileName = "src/spark/t.spark";
        if (!fileName.endsWith(".spark")) {
            System.out.println("Invalid file name");
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder code = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                code.append(line).append("\n");
            }

            Lexer lexer = new Lexer(code.toString());
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            Program program = parser.parseProgram();

            Executor executor = new Executor();
            executor.debuggingMode = true;
            executor.execute(program, tokens);

        } catch (Exception e) {
            System.out.println("Error reading file: " + fileName);
            e.printStackTrace();
        }
    }
}