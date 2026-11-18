/*
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
package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.util.lookup.ServiceProvider;

/**
 * Session-scoped AutoLeveler state that outlives the {@link AutoLevelerTopComponent}
 * window. Sampled points and the mesh-level command processors stay available while
 * the panel is closed so gcode correction keeps working.
 */
@ServiceProvider(service = AutoLevelerSession.class)
public class AutoLevelerSession {
    private SurfaceScanner scanner;
    private MeshLevelManager meshLevelManager;

    public synchronized void initialize(BackendAPI backend) {
        if (scanner != null) {
            return;
        }
        scanner = new SurfaceScanner(backend);
        meshLevelManager = new MeshLevelManager(scanner, backend);
        // Keep mesh leveling in sync even when the AutoLeveler window is closed.
        scanner.addListener(meshLevelManager::update);
    }

    public synchronized SurfaceScanner getScanner() {
        return scanner;
    }

    public synchronized MeshLevelManager getMeshLevelManager() {
        return meshLevelManager;
    }

    /**
     * True when Apply to Gcode is enabled and the sampled surface is valid.
     */
    public synchronized boolean isCorrectionActive(BackendAPI backend) {
        if (scanner == null || backend == null) {
            return false;
        }
        return backend.getSettings().getAutoLevelSettings().getApplyToGcode() && scanner.isValid();
    }
}
