package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class AutoLevelerSessionTest {

    @Test
    public void initializeReusesScannerAndKeepsMeshLevelingAcrossCalls() {
        Settings settings = new Settings();
        // Avoid installing mesh processors here; Utils pulls in Swing file choosers.
        settings.getAutoLevelSettings().setApplyToGcode(false);
        BackendAPI backend = backendStub(settings);

        AutoLevelerSession session = new AutoLevelerSession();
        session.initialize(backend);
        SurfaceScanner scanner = session.getScanner();
        MeshLevelManager meshLevelManager = session.getMeshLevelManager();

        scanner.setScannedPoints(List.of(
                new Position(0, 0, 0.1, Units.MM),
                new Position(10, 0, 0.2, Units.MM),
                new Position(0, 10, 0.4, Units.MM)));

        session.initialize(backend);

        assertSame(scanner, session.getScanner());
        assertSame(meshLevelManager, session.getMeshLevelManager());
        assertEquals(3, session.getScanner().getScannedPoints().size());
    }

    private static BackendAPI backendStub(Settings settings) {
        return (BackendAPI) Proxy.newProxyInstance(
                BackendAPI.class.getClassLoader(),
                new Class<?>[]{BackendAPI.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    return switch (name) {
                        case "getSettings" -> settings;
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "toString" -> "BackendAPI-stub";
                        default -> defaultValue(method.getReturnType());
                    };
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == void.class) {
            return null;
        }
        return 0;
    }
}
