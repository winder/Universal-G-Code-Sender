/*
    Copyright 2023-2024 Will Winder

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

import static com.willwinder.ugs.platform.surfacescanner.Utils.createCommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessorList;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.utils.AutoLevelArcMode;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.Exceptions;

import java.util.List;

/**
 * A mesh leveler manager that handles all required command processors
 * to be able to do a mesh leveling
 */
public class MeshLevelManager {
    private final SurfaceScanner surfaceScanner;
    private final BackendAPI backend;
    private CommandProcessorList commandProcessorList;
    private List<Position> appliedPoints = List.of();
    private double appliedZSurface = Double.NaN;
    private double appliedStepResolution = Double.NaN;
    private double appliedArcSliceLength = Double.NaN;
    private AutoLevelArcMode appliedArcMode;

    public MeshLevelManager(SurfaceScanner surfaceScanner, BackendAPI backend) {
        this.surfaceScanner = surfaceScanner;
        this.backend = backend;
    }

    public void update() {
        AutoLevelSettings autoLevelSettings = backend.getSettings().getAutoLevelSettings();
        boolean apply = surfaceScanner.isValid() && autoLevelSettings.getApplyToGcode();
        List<Position> points = apply ? surfaceScanner.getScannedPoints() : List.of();
        if (sameAppliedSurface(apply, points, autoLevelSettings)) {
            return;
        }

        CommandProcessorList previous = commandProcessorList;
        CommandProcessorList next = null;
        try {
            if (apply) {
                next = createCommandProcessor(autoLevelSettings, surfaceScanner);
            }
            if (previous == null && next == null) {
                remember(apply, points, autoLevelSettings);
                return;
            }
            backend.replaceCommandProcessor(previous, next);
            commandProcessorList = next;
            remember(apply, points, autoLevelSettings);
        } catch (Exception ex) {
            commandProcessorList = null;
            forgetAppliedSurface();
            GUIHelpers.displayErrorDialog(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Installs the surface processor again after the gcode parser has been reset.
     * Sampled points are left in place.
     */
    public void reapply() {
        CommandProcessorList previous = commandProcessorList;
        commandProcessorList = null;
        forgetAppliedSurface();
        if (previous != null) {
            try {
                backend.removeCommandProcessor(previous);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        update();
    }

    public void clear() {
        // Remove previously active command processors
        if (commandProcessorList != null) {
            try {
                backend.removeCommandProcessor(commandProcessorList);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            commandProcessorList = null;
        }
        forgetAppliedSurface();
    }

    private boolean sameAppliedSurface(boolean apply, List<Position> points, AutoLevelSettings settings) {
        if (!apply) {
            return commandProcessorList == null;
        }
        return commandProcessorList != null
                && appliedPoints.equals(points)
                && appliedZSurface == settings.getZSurface()
                && appliedStepResolution == settings.getStepResolution()
                && appliedArcSliceLength == settings.getAutoLevelArcSliceLength()
                && appliedArcMode == settings.getArcMode();
    }

    private void remember(boolean apply, List<Position> points, AutoLevelSettings settings) {
        if (!apply) {
            forgetAppliedSurface();
            return;
        }
        appliedPoints = points;
        appliedZSurface = settings.getZSurface();
        appliedStepResolution = settings.getStepResolution();
        appliedArcSliceLength = settings.getAutoLevelArcSliceLength();
        appliedArcMode = settings.getArcMode();
    }

    private void forgetAppliedSurface() {
        appliedPoints = List.of();
        appliedZSurface = Double.NaN;
        appliedStepResolution = Double.NaN;
        appliedArcSliceLength = Double.NaN;
        appliedArcMode = null;
    }
}
