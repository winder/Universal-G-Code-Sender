/*
    Copyright 2025 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.ActionAdapter;
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
import java.util.stream.Collectors;

/**
 * Scans for all actions in the application and makes them available in this
 * registry
 */
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
                    @SuppressWarnings("unchecked")
                    Constructor<? extends Action> declaredConstructor = ((Class<? extends Action>) classInfo).getDeclaredConstructor();
                    Action action = declaredConstructor.newInstance();
                    actions.put(action.getId(), action);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    // Ignore
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
                    @SuppressWarnings("unchecked")
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

    public void registerAction(Action action) {
        actions.put(action.getId(), action);
    }

    public List<Action> getAllActionsOfClass(Class<? extends Action> clazz) {
        return actions
                .values()
                .stream()
                .filter(action -> clazz.isAssignableFrom(action.getClass()))
                .collect(Collectors.toList());
    }
}
