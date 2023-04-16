/*
    Copyright 2017 Will Winder

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

import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.ProbeEvent;
import com.willwinder.universalgcodesender.utils.Settings;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class SurfaceScanner {
    private static final Logger logger = Logger.getLogger(SurfaceScanner.class.getSimpleName());

    private final BackendAPI backend;
    private final Settings.AutoLevelSettings settings;
    private Position[][] probePositionGrid = new Position[0][0];
    private Deque<Position> pendingPositions = new LinkedList<>();

    private Units units = Units.UNKNOWN;
    private Position minXYZ = Position.ZERO;
    private Position maxXYZ = Position.ZERO;
    private Position machineWorkOffset = new Position(Units.MM);

    public SurfaceScanner(BackendAPI backend) {
        this.backend = backend;
        this.settings = backend.getSettings().getAutoLevelSettings();
    }

    /**
     * Provides two points of the scanners bounding box and the number of points to sample in the X/Y directions.
     */
    public void update(final Position corner1, final Position corner2, Units units) {
        if (corner1.getUnits() != corner2.getUnits()) {
            throw new IllegalArgumentException("Provide same unit for both measures.");
        }
        this.units = units;

        double xMin = Math.min(corner1.x, corner2.x);
        double xMax = Math.max(corner1.x, corner2.x);
        double yMin = Math.min(corner1.y, corner2.y);
        double yMax = Math.max(corner1.y, corner2.y);
        double zMin = Math.min(corner1.z, corner2.z);
        double zMax = Math.max(corner1.z, corner2.z);

        Position newMin = new Position(xMin, yMin, zMin, units);
        Position newMax = new Position(xMax, yMax, zMax, units);

        if (newMin.getX() == newMax.getX() || newMin.getY() == newMax.getY() || newMin.getZ() == newMax.getZ()) {
            // If we're 0 in any dimension there is nothing we can do yet.
            return;
        }

        this.minXYZ = newMin;
        this.maxXYZ = newMax;
        double resolution = settings.stepResolution;

        int xAxisPoints = (int) (Math.ceil((xMax - xMin) / resolution)) + 1;
        int yAxisPoints = (int) (Math.ceil((yMax - yMin) / resolution)) + 1;
        this.probePositionGrid = new Position[xAxisPoints][yAxisPoints];

        // Calculate probe locations.
        for (int x = 0; x < xAxisPoints; x++) {
            for (int y = 0; y < yAxisPoints; y++) {
                Position p = new Position(
                        xMin + Math.min(xMax - xMin, x * resolution),
                        yMin + Math.min(yMax - yMin, y * resolution),
                        Double.NaN,
                        units);
                probePositionGrid[x][y] = p;
            }
        }

        // Move along grid in zigzag pattern
        int yIncrement = 1;
        int yIndex = 0;
        pendingPositions = new LinkedList<>();
        for (Position[] columnPositions : probePositionGrid) {
            while (yIndex >= 0 && yIndex < columnPositions.length) {
                Position p = columnPositions[yIndex];
                pendingPositions.add(p);
                yIndex += yIncrement;
            }
            yIncrement = -yIncrement;
            yIndex += yIncrement;
        }

    }

    public void handleEvent(ProbeEvent evt) {
        if (pendingPositions.isEmpty()) return;

        Position probeMachinePosition = evt.getProbePosition();
        if (!Double.isFinite(probeMachinePosition.getZ())) {
            reset();
            throw new RuntimeException("Probe returned invalid position");
        }

        if (probeMachinePosition.getUnits() == Units.UNKNOWN) {
            System.out.println("Unknown units in autoleveler receiving probe. Assuming " + this.units);
        }
        probeMachinePosition = probeMachinePosition.getPositionIn(units);

        Position probePosition = new Position(
                probeMachinePosition.getX() + machineWorkOffset.x,
                probeMachinePosition.getY() + machineWorkOffset.y,
                probeMachinePosition.getZ() + machineWorkOffset.z,
                probeMachinePosition.getUnits());

        logger.log(Level.INFO, "Record ({0}, {1}, {2})",
                new Object[] {probePosition.getX(), probePosition.getY(), probePosition.getZ()});
        probeEvent(probePosition);

        if (!pendingPositions.isEmpty()) {
            probeNextPoint(probePosition.getZ());
        }
    }

    private void reset() {
        update(minXYZ, maxXYZ, units);
    }

    public void probeEvent(final Position p) {
        Position probePosition = pendingPositions.pop();
        if (p.getX() != probePosition.getX() || p.getY() != probePosition.getY()) {
            reset();
            throw new RuntimeException("Unexpected probe location");
        }
        Position settingsOffset = settings.autoLevelProbeOffset.getPositionIn(units);

        probePosition.setX(probePosition.getX() + settingsOffset.getX());
        probePosition.setY(probePosition.getY() + settingsOffset.getY());
        probePosition.setZ(p.getZ() + settingsOffset.getZ());
    }

    /**
     * Begin a scan the surface {@link #handleEvent(ProbeEvent)} must be called to properly progress through the scan.
     */
    public void scan() {
        Position work = backend.getWorkPosition();
        Position machine = backend.getMachinePosition();
        machineWorkOffset.x = work.x - machine.x;
        machineWorkOffset.y = work.y - machine.y;
        machineWorkOffset.z = work.z - machine.z;
        probeNextPoint(work.getZ());
    }

    private void probeNextPoint(Double zLast) {
        try {
            Position p = this.pendingPositions.peek();

            double zRetract = settings.zRetract;
            if (zRetract <= 0) {
                zRetract = maxXYZ.getZ() - minXYZ.getZ();
            }

            // Start by backing off the current position
            double zBackoff = Math.min(zLast + zRetract, maxXYZ.getZ());
            PartialPosition safeZ = PartialPosition.builder(maxXYZ.getUnits()).setZ(zBackoff).build();
            String cmd = GcodeUtils.generateMoveCommand(
                    "G90G0",
                    settings.probeScanFeedRate,
                    safeZ);
            logger.log(Level.INFO, "MoveTo {0} {1}", new Object[] {safeZ, cmd});
            backend.sendGcodeCommand(true, cmd);

            // Position over next probe position
            PartialPosition startPos = PartialPosition.builder(p).clearZ().build();
            logger.log(Level.INFO, "MoveTo {0} {1}", new Object[] {startPos, cmd});
            cmd = GcodeUtils.generateMoveCommand(
                    "G90G0", settings.probeScanFeedRate, startPos);
            backend.sendGcodeCommand(true, cmd);

            // Send probe command, probing down to zMin
            double probeDistance = minXYZ.getZ() - zBackoff;
            logger.log(Level.INFO, "Probe {0}", probeDistance);
            backend.probe("Z", settings.probeSpeed, probeDistance, units);
        } catch (Exception e) {
            reset();
            throw new RuntimeException(e);
        }
    }

    public void scanRandomData(){
        machineWorkOffset.x = 0;
        machineWorkOffset.y = 0;
        machineWorkOffset.z = 0;

        // Generate some random test data.
        while (!pendingPositions.isEmpty()) {
            Position p = new Position(pendingPositions.peek());
            p.setZ(ThreadLocalRandom.current().nextDouble(minXYZ.getZ(), maxXYZ.getZ()));
            probeEvent(p.getPositionIn(Units.MM));
        }
    }
    
    public ImmutableList<Position> getProbeStartPositions() {
        ImmutableList.Builder<Position> builder = ImmutableList.builder();
        double z = maxXYZ.getZ();
        for (Position[] columns : probePositionGrid) {
            for (Position p : columns) {
                Position zMaxPoint = new Position(p);
                zMaxPoint.setZ(z);
                builder.add(zMaxPoint);
            }
        }
        return builder.build();
    }

    public final Position[][] getProbePositionGrid() {
        return this.probePositionGrid;
    }

    public final Position getMaxXYZ() {
        return this.maxXYZ;
    }

    public final Position getMinXYZ() {
        return this.minXYZ;
    }

    public final Units getUnits() {
        return units;
    }

    public boolean isValid() {
        return probePositionGrid.length > 0 && pendingPositions.isEmpty();
    }
}
