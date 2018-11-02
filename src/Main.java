import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("50.004 TwoSAT Solver CI01-6");
            System.err.println("usage: java Main path/to/formula.cnf");
            return;
        }

        String inputFile = args[0];
        Scanner scanner;

        try {
            scanner = new Scanner(new File(inputFile));
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + inputFile);
            return;
        }

        TwoSATProblem problem = null;

        try {
            problem = CNFParser.readFrom(scanner);
        } catch (NumberFormatException e) {
            System.err.println("Malformed cnf file");
            return;
        }

        if (problem == null) {
            System.err.println("Parse failed");
            return;
        }

        boolean isSat = problem.isSatisfiable();
        System.out.println("FORMULA " + (isSat ? "" : "UN") + "SATISFIABLE");

        if (isSat) {
            problem.findSolution().forEach((x) -> System.out.print(x ? "1 " : "0 "));
        }

        scanner.close();
    }
}
