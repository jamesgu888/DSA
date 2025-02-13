package com.example.astar;

/**
 * @author James Gu, Ryan Li, Shamak Gowda
 * @version Feb 12
 * @description Edge Class: Makes a directed edge
 **/
public class Edge {

    // define start node of the edge
    private final int source;

    // define end node of the edge
    private final int destination;

    // weight for Dijkstra's
    private final int weight;

    /**
     * Edge Constructor
     **/
    public Edge(int source, int destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    // getter methods

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }
}