package com.example.trains;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.commons.support.scanning.ClasspathScanner;
import org.junit.platform.commons.support.scanning.DefaultClasspathScanner;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DijkstraTest {

    private final ClasspathScanner classpathScanner = new DefaultClasspathScanner(
            ClassLoaderUtils::getDefaultClassLoader,
            ReflectionUtils::tryToLoadClass
    );

    @Test
    public void testEntryPointClassName() {
        var entryPoint = getEntryPoint().orElseThrow();
        assertEquals("com.example.trains.Application", entryPoint.getName());
    }

    @Test
    void testCalculateDistancesWithNoArguments() {
        String output = withSwappedOutput(this::callMain);
        String[] lines = output.split("\n");
        Pattern destinationPattern = Pattern.compile("^(.*) <-");
        Pattern distancePattern = Pattern.compile("(\\([0-9]*(\\.[0-9]*)?\\))");
        Map<String, Double> distances = new HashMap<>();
        for (String line : lines) {
            try {
                String destination = destinationPattern.matcher(line).group();
                double distance = Double.parseDouble(distancePattern.matcher(destination).group());
                distances.put(destination, distance);
            } catch (IllegalStateException _) {}
        }
        assertEquals(Map.of(
                "Lyon", 1.40,
                "Paris", 3.30,
                "Grenoble", 0.00,
                "Dijon", 3.00,
                "Valence", 1.00,
                "Montpellier", 3.10,
                "Bordeaux", 5.00,
                "Toulouse", 5.85,
                "Narbonne", 4.10
        ), distances);
    }

    private void callMain(String... args) {
        try {
            getEntryPoint()
                    .flatMap(this::getMainMethod)
                    .orElseThrow()
                    .invoke(args);
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
        } catch (RuntimeException e) {
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
