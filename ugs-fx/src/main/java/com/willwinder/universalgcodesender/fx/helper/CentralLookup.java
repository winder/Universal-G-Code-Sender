package com.willwinder.universalgcodesender.fx.helper;

import java.util.LinkedHashSet;
import java.util.Optional;

public class CentralLookup {

    private static final LinkedHashSet<Object> registry = new LinkedHashSet<>();

    public static void register(Object object) {
        if (lookup(object.getClass()).isPresent()) {
            return;
        }

        registry.add(object);
    }

    public static <T> Optional<T> lookup(Class<T> clazz) {
        return registry.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }
}
