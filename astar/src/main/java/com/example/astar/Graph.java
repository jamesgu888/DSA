package com.example.astar;

import java.util.*;

/**
 * @author James Gu, Ryan Li, Shamak Gowda
 * @version Feb 12
 * @description Graph class: Stores graph and edges in a HashMap adjacency list
 **/
public class Graph {

    // Create an adjacency list
    private final Map<Integer, List<Edge>> adjList;

    /**
     * Constructor for our amazing graph
     **/
    public Graph() {
        adjList = new HashMap<>();
    }

    /**
     * @param edge
     *             Adds an edge to the graph. Since our roads are undirected, a
     *             reverse edge is also added
     */
    public void addEdge(Edge edge) {// Add the edge from source to destination.
        adjList.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge);// Add the reverse edge
        adjList.computeIfAbsent(edge.getDestination(), k -> new ArrayList<>())
                .add(new Edge(edge.getDestination(), edge.getSource(), edge.getWeight()));
    }

    /**
     * @param node
     * @return get the neighbors of the current node
     */
    public List<Edge> getNeighbors(int node) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    /**
     * @return get all the nodes for the map (using the HashMap)
     */
    public Set<Integer> getNodes() {
        return adjList.keySet();
    }
}