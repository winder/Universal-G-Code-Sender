/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service for handling lookups. Objects may be registered to be used in other parts of the application.
 *
 * @author Joacim Breiler
 */
public class LookupService {
    private static final Logger LOGGER = Logger.getLogger(LookupService.class.getName());
    private static final LinkedHashSet<Object> registry = new LinkedHashSet<>();
    private static volatile boolean isInitialized;

    public static void initialize() {
        if (isInitialized) {
            return;
        }

        synchronized (LookupService.class) {
            if (isInitialized) {
                return;
            }
            LOGGER.info("Initializing lookup service");
            try {
                GUIBackend backend = new GUIBackend();
                Settings settings = SettingsFactory.loadSettings();
                backend.applySettings(settings);
                register(backend);
                register(settings);
                register(new JogService(backend));
                register(new RunFromService(backend));
                isInitialized = true;
            } catch (Exception ex) {
                Logger.getLogger(LookupService.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }
    }

    public static synchronized void discoverProviders(Class<?> reference) {
        // ClassGraph cannot enumerate classes through NetBeans' ProxyClassLoader,
        // so scan the JAR/directory backing the reference class directly. Loading
        // must still go through the module's own classloader, otherwise dependent
        // classes (e.g. MigLayout) won't resolve.
        URL codeSource = reference.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader classLoader = reference.getClassLoader();
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo().enableAnnotationInfo()
                .overrideClasspath(codeSource)
                .acceptPackages(reference.getPackageName())
                .scan()) {
            List<String> classNames = scanResult.getClassesWithAnnotation(LookupServiceProvider.class.getName()).getNames();
            LOGGER.info("discoverProviders registered " + classNames.size() + " @LookupServiceProvider classes from " + reference.getPackageName());
            classNames.stream()
                    .map(name -> loadClass(name, classLoader))
                    .sorted(Comparator.comparingInt(LookupService::positionOf))
                    .forEach(LookupService::createAndRegister);
        }
    }

    private static Class<?> loadClass(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load lookup service: " + name, e);
        }
    }

    public static void register(Object object) {
        Class<?> clazz = object.getClass();
        if (registry.stream().anyMatch(clazz::isInstance)) {
            return;
        }
        registry.add(object);
    }

    public static <T> T lookup(Class<T> clazz) {
        initialize();
        synchronized (LookupService.class) {
            return lookupAll(clazz).stream().findFirst().orElseGet(() -> createAndRegister(clazz));
        }
    }

    public static <T> Optional<T> lookupOptional(Class<T> clazz) {
        initialize();
        synchronized (LookupService.class) {
            return lookupAll(clazz).stream().findFirst();
        }
    }

    public static synchronized <T> List<T> lookupAll(Class<T> clazz) {
        return registry.stream().filter(clazz::isInstance).map(clazz::cast).toList();
    }

    private static int positionOf(Class<?> type) {
        LookupServiceProvider annotation = type.getAnnotation(LookupServiceProvider.class);
        return annotation != null ? annotation.position() : 0;
    }

    public static synchronized <T> T createAndRegister(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            register(instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create service: " + type.getName(), e);
        }
    }

    public static synchronized <T> void remove(Class<T> clazz) {
        registry.removeIf(clazz::isInstance);
    }
}