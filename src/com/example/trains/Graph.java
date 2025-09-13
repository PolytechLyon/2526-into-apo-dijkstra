package com.example.trains;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;

public class Graph {
    private Vertex[] vertices;

    public void readFrom(String filename) {
        Path path = Path.of(filename);
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.vertices = lines
                .stream()
                .takeWhile(not(String::isBlank))
                .map(Vertex::new)
                .toArray(Vertex[]::new);
        lines
                .stream()
                .skip(vertices.length + 1)
                .filter(not(String::isBlank))
                .forEach(this::processEdgeLine);
    }

    private void processEdgeLine(String line) {
        String[] params = line.split(",");
        if (params.length >= 3) {
            try {
                int source = parseInt(params[0].trim());
                int target = parseInt(params[1].trim());
                double weight = parseDouble(params[2].trim());
                if (this.vertices.length > source && this.vertices.length > target) {
                    this.vertices[source].addEdge(new Edge(this.vertices[target], weight));
                }
            } catch (NumberFormatException ignore) {}
        }
    }

    public void toDotFormat(PrintStream output) {
        if (this.vertices == null) {
            return;
        }
        output.printf("digraph {%n");
        for (Vertex vertex :  this.vertices) {
            for (Edge edge : vertex.getEdges()) {
                output.printf("\"%s\" -> \"%s\" [w=%f]%n", vertex.getName(), edge.target().getName(), edge.cost());
            }
        }
        output.printf("}%n");
        output.flush();
    }

    public void printDistances(PrintStream output) {
        for (Vertex vertex :  this.vertices) {
            Vertex current = vertex;
            while (current != null) {
                output.print(current.getName());
                current = current.getPrevious();
                output.print(current == null ? " " : " <- ");
            }
            output.printf("(%.2f) %n", vertex.getDistance());
        }
    }

    public void printEdges(PrintStream output) {
        for (Vertex vertex :  this.vertices) {
            for (Edge edge: vertex.getEdges()) {
                output.printf("%s -> %s\n", vertex.getName(), edge.target().getName());
            }
        }
    }

    public void toDotFormat(String filename) {
        String filenameWithExtension = filename.endsWith(".dot") ?
                filename : filename.concat(".dot");
        try (PrintStream ps = new PrintStream(filenameWithExtension)){
            this.toDotFormat(ps);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void distancesFrom(Vertex start) {
        for (Vertex vertex : this.vertices) {
            vertex.setVisited(false);
            vertex.setDistance(Double.POSITIVE_INFINITY);
            vertex.setPrevious(null);
        }
        start.setDistance(0);

        Vertex current = start;
        while (current != null && current.getDistance() != POSITIVE_INFINITY) {
            Vertex neighbor = current;
            stream(current.getEdges())
                    .filter(e -> !e.target().isVisited())
                    .forEach(e -> this.updateShortestDistance(neighbor, e));
            current.setVisited(true);
            current = stream(this.vertices)
                    .filter(not(Vertex::isVisited))
                    .min(comparing(Vertex::getDistance))
                    .orElse(null);
        }
    }

    public void distancesFrom(String start) {
        Vertex vertex = stream(this.vertices)
                .filter(v -> start.equalsIgnoreCase(v.getName()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No such vertex with name %s".formatted(start)));
        this.distancesFrom(vertex);
    }

    private void updateShortestDistance(Vertex neighbor, Edge edge) {
        double newDistance = neighbor.getDistance() + edge.cost();
        if (newDistance < edge.target().getDistance()) {
            edge.target().setDistance(newDistance);
            edge.target().setPrevious(neighbor);
        }
    }
}
