import java.util.*;

/**
 * Implementation of Tarjan's strongly connected components algorithm
 *
 * A reference implementation can be found here:
 * https://github.com/williamfiset/Algorithms/blob/master/com/williamfiset/algorithms/graphtheory/TarjanSccSolverAdjacencyList.java
 * with accompanying video tutorial:
 * https://www.youtube.com/watch?v=TyWtx7q2D7Y
 */
public class TarjanSccFinder {
    /*
    The input graph must include in its keys ALL vertices,
    including those with outdegree 0 (not leading anywhere)
    */
    private Map<Integer, Set<Integer>> graph;

    /*
    Actually, there is a Stack<> type but that is deprecated
    Holds the vertices being explored in the current cycle
    */
    private Deque<Integer> stack;

    /*
    Fiset prefers to use an int[] but we have negative vertices
    Map vertices to Tarjan's ids
    This also serves as the "seen" set for the DFS. Unseen vertices have a value of -1
    */
    private Map<Integer, Integer> vertexToId;

    /*
    Low-link value for each vertex
    This eventually contains the information about which components are
    strongly connected.
    Strongly connected component vertices all have the same low-link value
    */
    private Map<Integer, Integer> vertexLowLink;

    /*
    Fiset also prefers a boolean[] but again, negative vertices
    ArrayDeque (the concrete class used for stack) does provide a
    contains() method BUT since the backing data structure is an array,
    membership test runs in O(n) time
    I suspect this is not that big a deal, but we'll just run with it
    */
    private Map<Integer, Boolean> vertexOnStack;

    // Next available id for assignment
    private int nextId;

    /*
    The list of SCCs in the order they are discovered by Tarjan's algorithm.
    */
    private List<Set<Integer>> sccPopOrder;

    /**
     * Create a new instance of Tarjan's algorithm-based solver.
     * @param graph The graph to run on (all vertices must be in the keys of the outer Map!!)
     */
    public TarjanSccFinder(Map<Integer, Set<Integer>> graph) {
        this.graph = graph;
        stack = new ArrayDeque<>();
        vertexToId = new HashMap<>();
        vertexOnStack = new HashMap<>();
        vertexLowLink = new HashMap<>();
        sccPopOrder = new ArrayList<>();
        nextId = 0;

        // Populate vertexToId with sentinels and vertexOnStack with false
        // vertexToId and vertexOnStack should contain even the (unseen) inverses
        for (int vertex: graph.keySet()) {
            vertexToId.put(vertex, -1);
            vertexOnStack.put(vertex, false);
        }
    }

    /**
     * Run Tarjan's algorithm. The returned List contains components in reverse topological order
     * (which is useful for assignment later)
     * @return a List of Sets where each set is a strongly connected component
     */
    public List<Set<Integer>> findSccs() {
        // Prefer the existing solution
        if (sccPopOrder.size() != 0) {
            return sccPopOrder;
        }

        // Perform dfs on each unvisited vertex
        // This on its own has complexity O(V+E)
        for (int vertex: graph.keySet()) {
            if (vertexToId.get(vertex) == -1) {
                dfs(vertex);
            }
        }

        return sccPopOrder;
    }

    private void dfs(int vertex) {
        // Remember this vertex
        stack.push(vertex);
        vertexOnStack.put(vertex, true);
        vertexToId.put(vertex, nextId);
        vertexLowLink.put(vertex, nextId);
        nextId++;

        // For each vertex "to" directly adjacent to the current vertex...
        for (int to: graph.get(vertex)) {
            // Recursively explore unseen vertices
            if (vertexToId.get(to) == -1) {
                dfs(to);
            }

            if (vertexOnStack.get(to)) {
                // If we're here, we saw this "to" vertex before - it's part of this cycle.
                // Update its low-link value to the lowest value in the edge
                int newLowLink = Math.min(vertexLowLink.get(vertex), vertexLowLink.get(to));
                vertexLowLink.put(vertex, newLowLink);
            }
        }

        // Bottomed out in dfs - did we hit the root node?
        // Check if the id of this vertex is the same as its low-link value
        // .equals() - autoboxed int primitive in Map
        if (vertexToId.get(vertex).equals(vertexLowLink.get(vertex))) {
            // Start a new SCC
            Set<Integer> currentScc = new HashSet<>();
            sccPopOrder.add(currentScc);

            // This is the root node for this branch
            // Start popping off the vertex stack and marking each one with the same low-link value
            while (stack.peek() != null) {
                int stackHeadVertex = stack.pop();
                currentScc.add(stackHeadVertex);
                vertexOnStack.put(stackHeadVertex, false);

                if (stackHeadVertex == vertex) {
                    break; // out of "while (stack.peek() != null)"
                }
            }
        }
        // When execution reaches here, we've exhausted the reachable vertices from this one.
        // Go back up the call stack and look at the previously visited vertex.
    }
}
