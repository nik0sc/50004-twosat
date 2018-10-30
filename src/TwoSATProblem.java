import java.util.*;

public class TwoSATProblem {
    private Map<Integer, Set<Integer>> digraph;
    private int numLiterals;
    private int numImplications;
    private Collection<List<Integer>> sccSolution;

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
            sccSolution = new TarjanSccFinder(digraph).findSccs();
        }
    }

    public Collection<List<Integer>> getSccSolution() {
        return sccSolution;
    }

    public boolean isSatisfiable() {
        solve();

        for (List<Integer> component: sccSolution) {
            for (int key: component) {
                if (component.contains(-key)) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<Boolean> findSolution() {
        return null;
    }
}
