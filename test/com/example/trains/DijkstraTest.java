package com.example.trains;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.commons.support.scanning.ClasspathScanner;
import org.junit.platform.commons.support.scanning.DefaultClasspathScanner;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DijkstraTest {

    @Test
    void testRunJava() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        ClasspathScanner scanner = new DefaultClasspathScanner(
                ClassLoaderUtils::getDefaultClassLoader,
                ReflectionUtils::tryToLoadClass
        );

        ClassFilter filter = ClassFilter.of(this::hasMainMethod);

        var classes = scanner.scanForClassesInPackage("", filter);

        classes.stream().findFirst()
                .flatMap(this::getMainMethod)
                .ifPresent(m -> {
                    try {
                        m.invoke(null, (Object) new String[] {});
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });

        assertEquals("Hello World!\n", bos.toString());

        System.setOut(originalOut);
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
