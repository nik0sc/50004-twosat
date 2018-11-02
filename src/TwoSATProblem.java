import java.util.*;

public class TwoSATProblem {
    private Map<Integer, Set<Integer>> digraph;
    private int numLiterals;
    private int numImplications;
    private Map<Integer, List<Integer>> sccSolution;
    private TarjanSccFinder tarjan;

    TwoSATProblem() {
        digraph = new HashMap<>();
        numLiterals = 0;
        numImplications = 0;
        sccSolution = null;
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
        if (sccSolution == null) {
            tarjan = new TarjanSccFinder(digraph);
            sccSolution = tarjan.findSccs();
        }
    }

    public Collection<List<Integer>> getSccSolution() {
        return sccSolution.values();
    }

    /**
     * Determine problem satisfiability with Tarjan's algorithm
     * @return Satisfiability
     */
    public boolean isSatisfiable() {
        solve();

        // Search each strongly connected component for a literal and its inverse
        for (List<Integer> component: getSccSolution()) {
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

        // Sccs to skip
        Set<Integer> skipSccLowlink = new HashSet<>();

        // pad my list up to 1 extra for 1-based indexing
        for (int i = 0; i <= numLiterals; i++) {
            assignment.add(null);
        }

        for (Map.Entry<Integer, List<Integer>> scc: tarjan.findSccs().entrySet()) {
            // Skip this scc if already assigned
            if (assignment.get(Math.abs(scc.getValue().get(0))) != null) {
                continue;
            }

            for (int literal: scc.getValue()) {
                if (literal < 0) {
                    assignment.set(-literal, false);
                } else if (literal > 0) {
                    assignment.set(literal, true);
                } else {
                    System.err.println("Literal 0?");
                }
            }
        }

//        // Iterates in reverse topological order
//        for (Map.Entry<Integer, Integer> entry: tarjan.getVertexLowLink().entrySet()) {
//            // Skip this scc if already assigned
//            if (skipSccLowlink.contains(entry.getValue())) {
//                continue;
//            }
//
//            int literal = entry.getKey();
//
//            // If this literal is already assigned, skip the entire scc
//            if (assignment.get(Math.abs(literal)) != null) {
//                skipSccLowlink.add(entry.getValue());
//                continue;
//            }
//
//            if (literal < 0) {
//                assignment.set(-literal, false);
//            } else if (literal > 0) {
//                assignment.set(literal, true);
//            } else {
//                System.err.println("Literal 0?");
//            }
//
//        }

        assignment.remove(0);

        return assignment;
    }
}
