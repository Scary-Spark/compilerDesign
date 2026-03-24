package main;

import executor.Executor;

public class Spark {
    public static void main(String[] args) {
        // Correct relative path
        String fileName = "src/spark/t.spark";

        if (!fileName.endsWith(".spark")) {
            System.out.println("Invalid file name");
            return;
        }

        Executor.execute(fileName);
    }
}