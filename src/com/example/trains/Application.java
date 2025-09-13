package com.example.trains;

public class Application {
    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.readFrom("input.txt");
        graph.toDotFormat("graph");
        if (args.length == 0) {
            graph.printEdges(System.out);
        } else {
            String start = args[0];
            System.out.printf("Calculating distances from %s.\n", start);
            graph.distancesFrom(start);
            graph.printDistances(System.out);
        }
    }
}
