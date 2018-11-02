import java.util.*;

public class TwoSATProblem {
    private Map<Integer, Set<Integer>> digraph;
    private int numLiterals;
    private int numImplications;
    private TarjanSccFinder tarjan;

    TwoSATProblem() {
        digraph = new HashMap<>();
        numLiterals = 0;
        numImplications = 0;
        tarjan = null;
    }

    public Map<Integer, Set<Integer>> getDigraph() {
        return digraph;
    }

    public int getNumLiterals() {
        return numLiterals;
    }

    public int getNumImplications() {
        return numImplications;
    }

    /**
     * Insert a CNF 2-clause into the problem formula, converting into the double implication form.
     * Takes care of bookkeeping like making sure all vertices (including those with outdegree 0) are represented in
     * the graph's outer Map keys.
     * @param a
     * @param b
     */
    public void insertCNFClause(int a, int b) {
        // Forget TarjanSccFinder!
        tarjan = null;

        if (a == 0 || b == 0) {
            System.err.println("Cannot insert 0 into KB");
            return;
        }

        // (a OR b) is equivalent to either of (NOT a -> b) or (NOT b -> a)
        int[][] implications = {{-a, b}, {-b, a}};

        // Insert each implication into the digraph
        for (int[] impl: implications) {
            if (digraph.containsKey(impl[0])) {
                digraph.get(impl[0]).add(impl[1]);
            } else {
                // Unseen literal
                Set<Integer> adjacents = new HashSet<>();
                adjacents.add(impl[1]);
                digraph.put(impl[0], adjacents);
                numLiterals++;
            }
            // Other side of implication must also be in the keys
            if (!digraph.containsKey(impl[1])) {
                digraph.put(impl[1], new HashSet<>());
            }
            numImplications++;
        }
    }

    public void solve() {
        if (tarjan == null) {
            tarjan = new TarjanSccFinder(digraph);
            tarjan.findSccs();
        }
    }

    public List<Set<Integer>> getSccSolution() {
        return tarjan.findSccs();
    }

    /**
     * Determine problem satisfiability with Tarjan's algorithm
     * @return Satisfiability
     */
    public boolean isSatisfiable() {
        solve();

        // Search each strongly connected component for a literal and its inverse
        for (Set<Integer> component: tarjan.findSccs()) {
            for (int key: component) {
                if (component.contains(-key)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Find a solution to the 2SAT problem
     * @return
     */
    public List<Boolean> findSolution() {
        // Don't waste time barking up the wrong tree
        if (!isSatisfiable()) {
            return null;
        }

        List<Boolean> assignment = new ArrayList<>();

        // pad my list up to 1 extra for 1-based indexing
        for (int i = 0; i <= numLiterals; i++) {
            assignment.add(null);
        }

        for (Set<Integer> scc: tarjan.findSccs()) {
            // Skip this scc if already assigned
            if (assignment.get(Math.abs(scc.iterator().next())) != null) {
                continue;
            }

            for (int literal: scc) {
                if (literal < 0) {
                    assignment.set(-literal, false);
                } else if (literal > 0) {
                    assignment.set(literal, true);
                } else {
                    System.err.println("Literal 0?");
                }
            }
        }

        assignment.remove(0);

        return assignment;
    }
}
