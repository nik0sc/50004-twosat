import java.io.*;
import java.util.*;

public class Main {
    static String inputFile = "SampleFormula2.cnf";

    public static void main(String[] args) {
        Scanner scanner;

        try {
            scanner = new Scanner(new File(inputFile));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + inputFile);
            return;
        }

        TwoSATProblem problem = null;

        try {
            problem = CNFParser.readFrom(scanner);
        } catch (NumberFormatException e) {
            System.out.println("Malformed cnf file");
            return;
        }

        if (problem == null) {
            System.out.println("Parse failed");
            return;
        }

        Map<Integer, Set<Integer>> digraph = problem.getDigraph();

        System.out.println(digraph.toString());
        System.out.println("Break here to inspect graph");

        System.out.println("Strongly connected components: ");
        problem.solve();
        System.out.println(problem.getSccSolution().toString());
        System.out.println("Break here to inspect components");

        System.out.println("FORMULA " + (problem.isSatisfiable() ? "SATISFIABLE" : "UNSATISFIABLE"));


        scanner.close();
    }
}
