package java;

public class Spark {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Spark <fileName.spark>");
            return;
        }

        String fileName = args[0];
        if (!fileName.matches("[a-zA-Z0-9_]+\\.spark")) {
            System.out.println("Invalid file name");
            return;
        }

        Executor.execute(fileName);
    }
}
