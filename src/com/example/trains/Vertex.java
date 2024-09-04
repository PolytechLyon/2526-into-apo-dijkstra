package com.example.trains;

import java.util.Arrays;

public final class Vertex {

    private final String name;
    private Edge[] edges = {};
    private double distance;
    private boolean visited;
    private Vertex previous;

    public Vertex(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Edge[] getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        this.edges = Arrays.copyOf(this.edges, this.edges.length + 1);
        this.edges[this.edges.length - 1] = edge;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Vertex getPrevious() {
        return previous;
    }

    public void setPrevious(Vertex previous) {
        this.previous = previous;
    }
}
