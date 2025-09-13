package com.example.trains;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.commons.support.scanning.ClasspathScanner;
import org.junit.platform.commons.support.scanning.DefaultClasspathScanner;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DijkstraTest {

    private static final ClasspathScanner classpathScanner = new DefaultClasspathScanner(
            ClassLoaderUtils::getDefaultClassLoader,
            ReflectionUtils::tryToLoadClass
    );

    private static final Map<String, Set<String>> EDGES = Map.of(
            "Lyon", Set.of("Paris", "Grenoble", "Dijon", "Valence", "Montpellier"),
            "Paris", Set.of("Lyon", "Montpellier", "Bordeaux", "Toulouse"),
            "Grenoble", Set.of("Lyon", "Valence"),
            "Dijon", Set.of("Lyon"),
            "Valence", Set.of("Lyon", "Grenoble"),
            "Montpellier", Set.of("Lyon", "Paris", "Narbonne"),
            "Bordeaux", Set.of("Paris"),
            "Toulouse", Set.of("Paris", "Narbonne"),
            "Narbonne", Set.of("Montpellier", "Toulouse")
    );

    private static final Map<String, Map<String, Double>> DISTANCES = Map.of(
            "Lyon", Map.of(
                    "Paris", 1.90,
                    "Grenoble", 1.40,
                    "Dijon", 1.60,
                    "Valence", 0.60,
                    "Montpellier", 1.70,
                    "Bordeaux", 4.40,
                    "Toulouse", 4.45,
                    "Narbonne", 2.70
            ),
            "Paris", Map.of(
                    "Lyon", 1.80,
                    "Grenoble", 3.20,
                    "Dijon", 3.40,
                    "Valence", 2.40,
                    "Montpellier", 3.25,
                    "Bordeaux", 2.50,
                    "Toulouse", 6.00,
                    "Narbonne", 4.25
            ),
            "Grenoble", Map.of(
                    "Lyon", 1.40,
                    "Paris", 3.30,
                    "Dijon", 3.00,
                    "Valence", 1.00,
                    "Montpellier", 3.10,
                    "Bordeaux", 5.80,
                    "Toulouse", 5.85,
                    "Narbonne", 4.10
            ),
            "Dijon", Map.of(
                    "Lyon", 2.00,
                    "Paris", 3.90,
                    "Grenoble", 3.40,
                    "Valence", 2.60,
                    "Montpellier", 3.70,
                    "Bordeaux", 6.40,
                    "Toulouse", 6.45,
                    "Narbonne", 4.70
            ),
            "Valence", Map.of(
                    "Lyon", 0.60,
                    "Paris", 2.50,
                    "Grenoble", 1.00,
                    "Dijon", 2.20,
                    "Montpellier", 2.30,
                    "Bordeaux", 5.00,
                    "Toulouse", 5.05,
                    "Narbonne", 3.30
            ),
            "Montpellier", Map.of(
                    "Lyon", 1.60,
                    "Paris", 3.50,
                    "Grenoble", 3.00,
                    "Dijon", 3.20,
                    "Valence", 2.20,
                    "Bordeaux", 6.00,
                    "Toulouse", 2.75,
                    "Narbonne", 1.00
            ),
            "Bordeaux", Map.of(
                    "Lyon", 4.30,
                    "Paris", 2.50,
                    "Grenoble", 5.70,
                    "Dijon", 5.90,
                    "Valence", 4.90,
                    "Montpellier", 5.75,
                    "Toulouse", 8.50,
                    "Narbonne", 6.75
            ),
            "Toulouse", Map.of(
                    "Lyon", 4.35,
                    "Paris", 6.25,
                    "Grenoble", 5.75,
                    "Dijon", 5.95,
                    "Valence", 4.95,
                    "Montpellier", 2.75,
                    "Bordeaux", 8.75,
                    "Narbonne", 1.75
            ),
            "Narbonne", Map.of(
                    "Lyon", 2.60,
                    "Paris", 4.50,
                    "Grenoble", 4.00,
                    "Dijon", 4.20,
                    "Valence", 3.20,
                    "Montpellier", 1.00,
                    "Bordeaux", 7.00,
                    "Toulouse", 1.75
            )
    );

    private final static Map<String, Map<String, List<String>>> PATHS = Map.of(
            "Lyon", Map.of(
                    "Paris", List.of("Lyon"),
                    "Grenoble", List.of("Lyon"),
                    "Dijon", List.of("Lyon"),
                    "Valence", List.of("Lyon"),
                    "Montpellier", List.of("Lyon"),
                    "Bordeaux", List.of("Paris", "Lyon"),
                    "Toulouse", List.of("Narbonne", "Montpellier", "Lyon"),
                    "Narbonne", List.of("Montpellier", "Lyon")
            ),
            "Paris", Map.of(
                    "Lyon", List.of("Paris"),
                    "Grenoble", List.of("Lyon", "Paris"),
                    "Dijon", List.of("Lyon", "Paris"),
                    "Valence", List.of("Lyon", "Paris"),
                    "Montpellier", List.of("Paris"),
                    "Bordeaux", List.of("Paris"),
                    "Toulouse", List.of("Narbonne", "Montpellier", "Paris"),
                    "Narbonne", List.of("Montpellier", "Paris")
            ),
            "Grenoble", Map.of(
                    "Lyon", List.of("Grenoble"),
                    "Paris", List.of("Lyon", "Grenoble"),
                    "Dijon", List.of("Lyon", "Grenoble"),
                    "Valence", List.of("Grenoble"),
                    "Montpellier", List.of("Lyon", "Grenoble"),
                    "Bordeaux", List.of("Paris", "Lyon", "Grenoble"),
                    "Toulouse", List.of("Narbonne", "Montpellier", "Lyon", "Grenoble"),
                    "Narbonne", List.of("Montpellier", "Lyon", "Grenoble")
            ),
            "Dijon", Map.of(
                    "Lyon", List.of("Dijon"),
                    "Paris", List.of("Lyon", "Dijon"),
                    "Grenoble", List.of("Lyon", "Dijon"),
                    "Valence", List.of("Lyon", "Dijon"),
                    "Montpellier", List.of("Lyon", "Dijon"),
                    "Bordeaux", List.of("Paris", "Lyon", "Dijon"),
                    "Toulouse", List.of("Narbonne", "Montpellier", "Lyon", "Dijon"),
                    "Narbonne", List.of("Montpellier", "Lyon", "Dijon")
            ),
            "Valence", Map.of(
                    "Lyon", List.of("Valence"),
                    "Paris", List.of("Lyon", "Valence"),
                    "Grenoble", List.of("Valence"),
                    "Dijon", List.of("Lyon", "Valence"),
                    "Montpellier", List.of("Lyon", "Valence"),
                    "Bordeaux", List.of("Paris", "Lyon", "Valence"),
                    "Toulouse", List.of("Narbonne", "Montpellier", "Lyon", "Valence"),
                    "Narbonne", List.of("Montpellier", "Lyon", "Valence")
            ),
            "Montpellier", Map.of(
                    "Lyon", List.of("Montpellier"),
                    "Paris", List.of("Montpellier"),
                    "Grenoble", List.of("Lyon", "Montpellier"),
                    "Dijon", List.of("Lyon", "Montpellier"),
                    "Valence", List.of("Lyon", "Montpellier"),
                    "Bordeaux", List.of("Paris", "Montpellier"),
                    "Toulouse", List.of("Narbonne", "Montpellier"),
                    "Narbonne", List.of("Montpellier")
            ),
            "Bordeaux", Map.of(
                    "Lyon", List.of("Paris", "Bordeaux"),
                    "Paris", List.of("Bordeaux"),
                    "Grenoble", List.of("Lyon", "Paris", "Bordeaux"),
                    "Dijon", List.of("Lyon", "Paris", "Bordeaux"),
                    "Valence", List.of("Lyon", "Paris", "Bordeaux"),
                    "Montpellier", List.of("Paris", "Bordeaux"),
                    "Toulouse", List.of("Narbonne", "Montpellier", "Paris", "Bordeaux"),
                    "Narbonne", List.of("Montpellier", "Paris", "Bordeaux")
            ),
            "Toulouse", Map.of(
                    "Lyon", List.of("Montpellier", "Narbonne", "Toulouse"),
                    "Paris", List.of("Montpellier", "Narbonne", "Toulouse"),
                    "Grenoble", List.of("Lyon", "Montpellier", "Narbonne", "Toulouse"),
                    "Dijon", List.of("Lyon", "Montpellier", "Narbonne", "Toulouse"),
                    "Valence", List.of("Lyon", "Montpellier", "Narbonne", "Toulouse"),
                    "Montpellier", List.of("Narbonne", "Toulouse"),
                    "Bordeaux", List.of("Paris", "Montpellier", "Narbonne", "Toulouse"),
                    "Narbonne", List.of("Toulouse")
            ),
            "Narbonne", Map.of(
                    "Lyon", List.of("Montpellier", "Narbonne"),
                    "Paris", List.of("Montpellier", "Narbonne"),
                    "Grenoble", List.of("Lyon", "Montpellier", "Narbonne"),
                    "Dijon", List.of("Lyon", "Montpellier", "Narbonne"),
                    "Valence", List.of("Lyon", "Montpellier", "Narbonne"),
                    "Montpellier", List.of("Narbonne"),
                    "Bordeaux", List.of("Paris", "Montpellier", "Narbonne"),
                    "Toulouse", List.of("Narbonne")
            )
    );


    @Test
    public void testEntryPointClassName() {
        var entryPoint = getEntryPoint().orElseThrow();
        assertEquals("com.example.trains.Application", entryPoint.getName());
    }

    @Test
    void testPrintEdgesWhenNoArgs() {
        String output = withSwappedOutput(this::callMain);
        String[] lines = output.split("\n");
        assertTrue(lines.length != 0, "Empty output.");
        Map<String, Set<String>> edges = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String source = parts[0].trim();
                String target = parts[1].trim();
                edges.computeIfAbsent(source, ignore -> new HashSet<>()).add(target);
            }
        }
        EDGES.forEach((key, value) -> {
            Set<String> targets = edges.get(key);
            assertNotNull(targets, "No edge target for vertex %s, %d expected".formatted(key, value.size()));
            assertEquals(value, targets, "Edge targets mismatch for vertex %s".formatted(key));
        });
    }

    @Test
    void testReadGrenobleAsFirstArgument() {
        String output = withSwappedOutput(() -> this.callMain("Grenoble"));
        String[] lines = output.split("\n");
        assertTrue(lines.length != 0, "Empty output.");
        assertTrue(lines[0].toLowerCase().contains("grenoble"), "Should print out city name passed in argument.");
    }

    @ParameterizedTest
    @MethodSource
    void testCalculateDistances(String vertex) {
        String output = withSwappedOutput(() -> this.callMain(vertex));
        String[] lines = output.split("\n");
        Map<String, Double> distances = readDistances(lines);
        DISTANCES.get(vertex).forEach((key, value) -> {
            assertNotNull(distances.get(key), "No distance for %s".formatted(key));
            assertEquals(value.doubleValue(), distances.get(key));
        });
    }

    @ParameterizedTest
    @MethodSource
    void testCalculatePaths(String vertex) {
        String output = withSwappedOutput(() -> this.callMain(vertex));
        String[] lines = output.split("\n");
        Map<String, List<String>> paths = readPaths(lines);
        PATHS.get(vertex).forEach((key, value) -> {
            assertNotNull(paths.get(key), "No distance for %s".formatted(key));
            assertEquals(value, paths.get(key));
        });
    }

    static Set<String> testCalculateDistances() {
        return DISTANCES.keySet();
    }

    static Set<String> testCalculatePaths() {
        return PATHS.keySet();
    }

    private Map<String, Double> readDistances(String[] lines) {
        Map<String, Double> distances = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String destination = parts[0].trim();
                double distance = Double.parseDouble(parts[parts.length - 1].trim());
                distances.put(destination, distance);
            }
        }
        return distances;
    }

    private Map<String, List<String>> readPaths(String[] lines) {
        Map<String, List<String>> paths = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 2) {
                String destination = parts[0].trim();
                List<String> path = Stream.of(parts)
                        .skip(1)
                        .limit(parts.length - 2)
                        .map(String::trim)
                        .toList();
                paths.put(destination, path);
            }
        }
        return paths;
    }

    private void callMain(String... args) {
        try {
            getEntryPoint()
                    .flatMap(this::getMainMethod)
                    .orElseThrow()
                    .invoke(null, (Object) args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private String withSwappedOutput(Runnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        try {
            runnable.run();
        } finally {
            System.setOut(originalOut);
        }
        return bos.toString();
    }

    private Optional<Class<?>> getEntryPoint() {
        ClassFilter filter = ClassFilter.of(this::hasMainMethod);
        return classpathScanner.scanForClassesInPackage("", filter).stream().findFirst();
    }

    private boolean hasMainMethod(Class<?> klass) {
        return getMainMethod(klass)
                .isPresent();
    }

    private Optional<Method> getMainMethod(Class<?> klass) {
        return ReflectionUtils
                .findMethod(klass, "main", String[].class)
                .filter(m -> Modifier.isStatic(m.getModifiers()));
    }
}
