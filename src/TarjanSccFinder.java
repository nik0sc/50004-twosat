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
    Strongly connected component vertices all hvae the same low-link value
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

    private Collection<List<Integer>> solution;

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
        nextId = 0;

        // Populate vertexToId with sentinels and vertexOnStack with false
        // vertexToId and vertexOnStack should contain even the (unseen) inverses
        for (int vertex: graph.keySet()) {
            vertexToId.put(vertex, -1);
//            vertexLowLink.put(vertex, -1);
            vertexOnStack.put(vertex, false);
        }
    }

    /**
     * Run Tarjan's algorithm.
     * @return a Collection of List<Integer>s where each list is a strongly connected component
     */
    public Collection<List<Integer>> findSccs() {
        // Prefer the existing solution
        if (solution != null) {
            return solution;
        }

        // Perform dfs on each unvisited vertex
        // This on its own has complexity O(V+E)
        for (int vertex: graph.keySet()) {
            if (vertexToId.get(vertex) == -1) {
                dfs(vertex);
            }
        }

        // When we're here, we've built the vertexLowLink map
        // Now we need to "reverse" it and put the values into solution
        // Eg. {1->2, 2->3, 3->2} reverses to {2->[1,3], 3->[2]}
        // If there are N entries in vertexLowLink then I think this is O(N)
        Map<Integer, List<Integer>> lowLinkReversed = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry: vertexLowLink.entrySet()) {
            if (!lowLinkReversed.containsKey(entry.getValue())) {
                lowLinkReversed.put(entry.getValue(), new ArrayList<>());
            }

            // Pick out the lowLinkReversed value using vertexLowLink value as the key
            // Then add the vertexLowLink key into the lowLinkReversed value (a List)
            lowLinkReversed.get(entry.getValue()).add(entry.getKey());
        }

        // Don't really care about the low-link values
        solution = lowLinkReversed.values();
        return solution;
    }

    private void dfs(int vertex) {
        // Remember this vertex
        stack.push(vertex);
        vertexOnStack.put(vertex, true);
        vertexToId.put(vertex, nextId);
        vertexLowLink.put(vertex, nextId);
        nextId++;

        // For each vertex "to" reachable from the current vertex...
        for (int to: graph.get(vertex)) {
            // Recursively explore unseen vertices
            if (vertexToId.get(to) == -1) {
                dfs(to);
            }

            //
            // If we're here, we saw this "to" vertex before - it's part of this cycle.
            // Update its low-link value to the lowest value in the edge
            if (vertexOnStack.get(to)) {
                int newLowLink = Math.min(vertexLowLink.get(vertex), vertexLowLink.get(to));
                vertexLowLink.put(vertex, newLowLink);
            }
        }

        // Bottomed out in dfs - did we hit the root node?
        // Check if the id of this vertex is the same as its low-link value
        // .equals() - autoboxed int primitive in Map
        if (vertexToId.get(vertex).equals(vertexLowLink.get(vertex))) {
            // This is the root node for this branch
            // Start popping off the vertex stack and marking each one with the same low-link value
            while (stack.peek() != null) {
                int stackHeadVertex = stack.pop();
                vertexOnStack.put(stackHeadVertex, false);
                vertexLowLink.put(stackHeadVertex, vertexToId.get(vertex));
                if (stackHeadVertex == vertex) {
                    break; // out of "while (stack.peek() != null)"
                }
            }
        }
        // When execution reaches here, we've exhausted the reachable vertices from this one.
        // Go back up the call stack and look at the previously visited vertex.
    }
}
