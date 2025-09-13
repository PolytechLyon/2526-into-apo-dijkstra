package com.example.trains;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.commons.support.scanning.ClasspathScanner;
import org.junit.platform.commons.support.scanning.DefaultClasspathScanner;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DijkstraTest {

    private final ClasspathScanner classpathScanner = new DefaultClasspathScanner(
            ClassLoaderUtils::getDefaultClassLoader,
            ReflectionUtils::tryToLoadClass
    );

    private final Map<String, Double> DISTANCES_FROM_GRENOBLE = Map.of(
            "Lyon", 1.40,
            "Paris", 3.30,
            "Dijon", 3.00,
            "Valence", 1.00,
            "Montpellier", 3.10,
            "Bordeaux", 5.80,
            "Toulouse", 5.85,
            "Narbonne", 4.10
    );

    private final Map<String, Set<String>> EDGES = Map.of(
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

    @Test
    void testCalculateDistancesFromGrenoble() {
        String output = withSwappedOutput(() -> this.callMain("Grenoble"));
        String[] lines = output.split("\n");
        Map<String, Double> distances = readDistances(lines);
        DISTANCES_FROM_GRENOBLE.forEach((key, value) -> {
            assertNotNull(distances.get(key), "No distance for %s".formatted(key));
            assertEquals(value.doubleValue(), distances.get(key));
        });
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
        return this.classpathScanner.scanForClassesInPackage("", filter).stream().findFirst();
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
