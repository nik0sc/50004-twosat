import java.util.*;

// An implementation of Tarjan's algorithm can be found here:
// https://github.com/williamfiset/Algorithms/blob/master/com/williamfiset/algorithms/graphtheory/TarjanSccSolverAdjacencyList.java


public class TarjanSccFinder {
    // The input graph must include in its keys ALL vertices,
    // including those with outdegree 0 (not leading anywhere)
    private Map<Integer, Set<Integer>> graph;

    // Actually, there is a Stack<> type but that is deprecated
    // Holds the vertices being explored in the current cycle
    private Deque<Integer> stack;

    // Fiset prefers to use an int[] but we have negative vertices
    // Map vertices to Tarjan's ids
    // This also serves as the "seen" set for the DFS
    private Map<Integer, Integer> vertexToId;

    // Low-link value for each vertex
    // This eventually contains the information about which components are
    // strongly connected.
    private Map<Integer, Integer> vertexLowLink;

    // Fiset also prefers a boolean[] but again, negative vertices
    // ArrayDeque (the concrete class used for stack) does provide a
    // contains() method BUT since the backing data structure is an array,
    // membership test runs in O(n) time
    // I suspect this is not that big a deal, but we'll just run with it
    private Map<Integer, Boolean> vertexOnStack;

    // Next available id for assignment
    private int nextId;

    private Collection<List<Integer>> solution;

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

    public Collection<List<Integer>> findSccs() {
        if (solution != null) {
            return solution;
        }

        for (int vertex: graph.keySet()) {
            if (vertexToId.get(vertex) == -1) {
                dfs(vertex);
            }
        }

        // When we're here, we've built the vertexLowLink map
        // Now we need to "reverse" it and put the values into solution

        Map<Integer, List<Integer>> lowLinkReversed = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry: vertexLowLink.entrySet()) {
            if (!lowLinkReversed.containsKey(entry.getValue())) {
                lowLinkReversed.put(entry.getValue(), new ArrayList<>());
            }

            lowLinkReversed.get(entry.getValue()).add(entry.getKey());
        }

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

        for (int to: graph.get(vertex)) {
            // Recursively explore unseen vertices
            if (vertexToId.get(to) == -1) {
                dfs(to);
            }

            // Saw this vertex before - it's part of this cycle. Mark it as such
            if (vertexOnStack.get(to)) {
                int newLowLink = Math.min(vertexLowLink.get(vertex), vertexLowLink.get(to));
                vertexLowLink.put(vertex, newLowLink);
            }
        }

        // Bottomed out in dfs - did we hit the root node?
        // .equals() - autoboxed int primitive in Map
        if (vertexToId.get(vertex).equals(vertexLowLink.get(vertex))) {
            while (stack.peek() != null) {
                int stackHeadVertex = stack.pop();
                vertexOnStack.put(vertex, false);
                vertexLowLink.put(stackHeadVertex, vertexToId.get(vertex));
                if (stackHeadVertex == vertex) {
                    break;
                }
            }
        }
    }
}
