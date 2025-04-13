package com.willwinder.universalgcodesender.fx.actions;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ActionRegistry {
    private static ActionRegistry instance;
    private static final Map<String, Action> actions = new ConcurrentHashMap<>();

    public ActionRegistry() {
        loadActionsFromAnnotation();
        loadActionsOfClass();
    }

    private static void loadActionsOfClass() {

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo() // Enables annotation and class scanning
                .scan()) {

            List<Class<?>> classesImplementing = scanResult
                    .getClassesImplementing(Action.class)
                    .loadClasses();

            // Print found classes
            for (Class<?> classInfo : classesImplementing) {
                if (Modifier.isAbstract(classInfo.getModifiers()) || classInfo.equals(ActionAdapter.class)) {
                    continue;
                }

                try {
                    Constructor<? extends Action> declaredConstructor = ((Class<? extends Action>) classInfo).getDeclaredConstructor();

                    Action action = declaredConstructor.newInstance();
                    actions.put(action.getId(), action);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void loadActionsFromAnnotation() {
        String annotationName = com.willwinder.universalgcodesender.actions.Action.class.getCanonicalName();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo() // Enables annotation and class scanning
                .scan()) {

            List<Class<?>> annotatedClasses = scanResult
                    .getClassesWithAnnotation(annotationName)
                    .loadClasses();

            // Print found classes
            for (Class<?> classInfo : annotatedClasses) {
                if (javax.swing.Action.class.isAssignableFrom(classInfo)) {
                    com.willwinder.universalgcodesender.actions.Action annotation = classInfo.getAnnotation(com.willwinder.universalgcodesender.actions.Action.class);
                    ActionAdapter actionAdapter = new ActionAdapter((Class<? extends javax.swing.Action>) classInfo);
                    actions.put(classInfo.getCanonicalName(), actionAdapter);
                }
            }
        }
    }


    public static ActionRegistry getInstance() {
        if (instance == null) {
            instance = new ActionRegistry();
        }
        return instance;
    }

    public Collection<Action> getActions() {
        return actions.values();
    }

    public Optional<Action> getAction(String id) {
        return Optional.ofNullable(actions.get(id));
    }
}
