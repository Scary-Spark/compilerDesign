package executor;

import lexicalAnalysis.Lexer;
import lexicalAnalysis.Token;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Executor {
    public static boolean debuggingMode = true;
    public static List<Token> tokens = new ArrayList<>();

    public static void execute(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder code = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                code.append(line).append("\n");
            }

            Lexer lexer = new Lexer(code.toString());
            tokens = lexer.tokenize();

            if (debuggingMode) {
                System.out.println("========== TOKENS ==========");

                int previousLine = -1;

                for (Token token : tokens) {
                    int currentLine = token.line;
                    if (previousLine != -1 && currentLine != previousLine) {
                        System.out.println();
                    }

                    System.out.println(token);
                    previousLine = currentLine;
                }

                System.out.println("============================");
            }

        } catch (Exception e) {
            System.out.println("Can't open file: " + fileName);
        }
    }
}
