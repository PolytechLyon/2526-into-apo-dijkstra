package com.example.trains;

public class Application {
    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.readFrom("input.txt");
        graph.toDotFormat("graph");
        graph.distancesFrom("Grenoble");
        graph.printDistances(System.out);
    }
}
