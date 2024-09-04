package fr.polytech.dijkstra;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Directed graph format convertor.
 * This class converts from text format to dot format graphs.
 */
public class Convertor {

    /**
     * Maximum accepted number of vertices
     */
    private static final int MAX_VERTICES_NUMBER = 64;

    /**
     * Main method.
     *
     * @param args  program arguments, being source file name
     */
    public static void main(String[] args) {
        String filename = args.length != 0 ? args[0] : "input.txt";
        convert(filename);
    }

    /**
     * Convert a file with the name {@code sourceFilename} to dot format.
     *
     * @param sourceFilename    source filename
     */
    public static void convert(String sourceFilename) {
        // Destination filename is the same as input filename with extension replaced with "dot".
        String targetFilename = sourceFilename.split("\\.")[0].concat(".dot");
        // open target file for writing
        try (PrintStream output = new PrintStream(targetFilename)) {
            // Path to source file
            Path path = Path.of(sourceFilename);
            // Read all source file lines at once
            List<String> lines = Files.readAllLines(path);
            // Flag indicating whether it is the vertices (first part of the file) that are being read
            boolean readingVertices = true;
            // Vertices array
            String[] vertices = new String[MAX_VERTICES_NUMBER];
            // Current index
            int index = 0;
            // print out dot file header
            output.printf("digraph {%n");
            // for each line in the source file
            for (String line : lines) {
                if (line.isBlank()) {
                    // a blank line delimits the vertices part
                    readingVertices = false;
                } else if (readingVertices) {
                    // while still reading vertices, add a vertex to the vertices array
                    vertices[index++] = line;
                } else {
                    // if all vertices are read, analyse the line to extract 3 comma-separated elements
                    String[] params = line.split(",");
                    if (params.length < 3) {
                        // if the line doesn't contain 3 comma-separated elements, throw an error
                        throw new RuntimeException("Illegible line %s".formatted(line));
                    }
                    // first element is source index
                    int sourceIndex = parseInt(params[0].trim());
                    // second element is target index
                    int targetIndex = parseInt(params[1].trim());
                    // third element is weight
                    double weight = parseDouble(params[2].trim());
                    String source = vertices[sourceIndex];
                    String target = vertices[targetIndex];
                    // print out the directed edge line
                    output.printf("\"%s\" -> \"%s\" [label=%.2f arrowhead=normal]%n", source, target, weight);
                }
            }
            // print out dot file footer
            output.printf("}%n");
        } catch (IOException e) {
            // catch and wrap checked exceptions
            throw new RuntimeException(e);
        }
    }
}
