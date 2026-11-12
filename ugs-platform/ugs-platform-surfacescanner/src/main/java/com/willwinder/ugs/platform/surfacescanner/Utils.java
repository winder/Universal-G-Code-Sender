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

import com.willwinder.universalgcodesender.gcode.processors.AutoLevelProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessorList;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Static utils
 */
public class Utils {

    public static final JFileChooser fileChooser = new JFileChooser();

    /**
     * Checks if there is probed data present and if so asks the user if it is ok to delete.
     * If it is ok to delete, the data will be removed
     *
     * @param surfaceScanner the current surface scanner
     * @return true if there is no probe data
     */
    public static boolean removeProbeData(SurfaceScanner surfaceScanner) {
        if (!surfaceScanner.isValid()) {
            return true;
        }

        if (!shouldEraseProbedData()) {
            return false;
        }

        surfaceScanner.reset();
        return true;
    }

    private static boolean shouldEraseProbedData() {
        int result = JOptionPane.showConfirmDialog(new Frame(),
                Localization.getString("autoleveler.panel.overwrite"),
                Localization.getString("platform.window.autoleveler"),
                JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public static Position getMaxPosition(List<Position> positions) {
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        UnitUtils.Units units = UnitUtils.Units.MM;
        for (Position p : positions) {
            units = p.getUnits();
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }

        return new Position(maxX, maxY, maxZ, units);
    }

    public static Position getMinPosition(List<Position> positions) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        UnitUtils.Units units = UnitUtils.Units.MM;
        for (Position p : positions) {
            units = p.getUnits();
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
        }

        return new Position(minX, minY, minZ, units);
    }

    public static Position getRoundPosition(Position position) {
        double x = Math.round(position.getX() * 1000d) / 1000d;
        double y = Math.round(position.getY() * 1000d) / 1000d;
        double z = Math.round(position.getZ() * 1000d) / 1000d;
        return new Position(x, y, z, position.getUnits());
    }

    public static CommandProcessorList createCommandProcessor(AutoLevelSettings autoLevelSettings, SurfaceScanner surfaceScanner) {
        CommandProcessorList result = new CommandProcessorList();

        // Arc tolerance is the maximum chord error, not a fixed step. A 0.01 mm step turns a
        // normal program into more than a million moves, which is too slow to rebuild when a
        // probe point is added and too fine to stream at the programmed feed. Long lines are
        // still split to a quarter of the probe resolution so Z follows changes in slope.
        // All three happen in one processor so each move is scanned once.
        result.add(new AutoLevelProcessor(
                autoLevelSettings.getAutoLevelArcSliceLength(),
                autoLevelSettings.getStepResolution() / 4,
                autoLevelSettings.getZSurface(),
                surfaceScanner.getScannedPoints(),
                autoLevelSettings.getArcMode()));
        return result;
    }
}
