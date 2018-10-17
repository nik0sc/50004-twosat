import java.util.Scanner;

// A parser for CNF files
public class CNFParser {
    private enum State {
        PREAMBLE, CLAUSES, CLAUSES_INCLAUSE
    }

    public static TwoSATProblem readFrom(Scanner scanner) {
        State state = State.PREAMBLE;
        int variables = 0;
        int clauses = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            switch (line.charAt(0)) {
                case 'p':
                    String[] spls = line.split(" ");
                    if (!spls[1].equals("cnf")) {
                        System.err.println("Unrecognized format: " + spls[1]);
                        return null;
                    }
                    variables = Integer.parseInt(spls[2]);
                    clauses = Integer.parseInt(spls[3]);
                    state = State.CLAUSES;
                    // can't break out of the while loop from inside here
                    break;
                case 'c':
                    // ignore line
                    continue;
                default:
                    System.err.println("Unrecognized preamble: " + line);
                    return null;
            }

            if (state == State.CLAUSES) {
                break;
            }

        }

        // 0 is a sentinel value
        int[] currentClause = {0, 0};
        TwoSATProblem problem = new TwoSATProblem();

        while (scanner.hasNextInt()) {
            switch (state) {
                case CLAUSES:
                    currentClause[0] = scanner.nextInt();
                    state = State.CLAUSES_INCLAUSE;
                    break;
                case CLAUSES_INCLAUSE:
                    currentClause[1] = scanner.nextInt();

                    // Make sure the clause is over!
                    if (scanner.nextInt() != 0) {
                        System.err.println("Clause is too long or unterminated");
                        return null;
                    }

                    problem.insertCNFClause(currentClause[0], currentClause[1]);
                    // zeroize current clause before moving on
                    currentClause[0] = currentClause[1] = 0;
                    state = State.CLAUSES;
                    break;
                default:
                    System.err.println("Acceptor state?");
                    return null;
            }
        }

        // check the graph
        System.out.println("Number of variables in preamble: " + variables + " vs seen: " + problem.getNumLiterals());
        System.out.println("Number of clauses in preamble: " + clauses + " vs seen: " + problem.getNumImplications() / 2);

        return problem;
    }
}
