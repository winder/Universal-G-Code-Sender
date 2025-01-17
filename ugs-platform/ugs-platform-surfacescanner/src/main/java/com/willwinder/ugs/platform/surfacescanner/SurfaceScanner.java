/*
    Copyright 2017-2023 Will Winder

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
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.ProbeEvent;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wwinder
 */
public class SurfaceScanner {
    private static final Logger logger = Logger.getLogger(SurfaceScanner.class.getSimpleName());

    private final BackendAPI backend;
    private final AutoLevelSettings settings;
    private final Set<SurfaceScannerListener> listeners = ConcurrentHashMap.newKeySet();
    private Position[][] probePositionGrid = new Position[0][0];
    private Deque<Position> pendingPositions = new LinkedList<>();
    private Position minXYZ = Position.ZERO;
    private Position maxXYZ = Position.ZERO;
    private Position machineWorkOffset = new Position(Units.MM);

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    public SurfaceScanner(BackendAPI backend) {
        this.backend = backend;
        this.settings = backend.getSettings().getAutoLevelSettings();
        Position minPosition = new Position(settings.getMinX(), settings.getMinY(), settings.getMinZ(), backend.getSettings().getPreferredUnits());
        Position maxPosition = new Position(settings.getMaxX(), settings.getMaxY(), settings.getMaxZ(), backend.getSettings().getPreferredUnits());
        update(minPosition, maxPosition);
    }

    /**
     * Provides two points of the scanners bounding box and the number of points to sample in the X/Y directions.
     */
    public void update(final Position corner1, final Position corner2) {
        if (corner1.getUnits() != corner2.getUnits()) {
            throw new IllegalArgumentException("Provide same unit for both measures.");
        }

        double xMin = Math.min(corner1.x, corner2.x);
        double xMax = Math.max(corner1.x, corner2.x);
        double yMin = Math.min(corner1.y, corner2.y);
        double yMax = Math.max(corner1.y, corner2.y);
        double zMin = Math.min(corner1.z, corner2.z);
        double zMax = Math.max(corner1.z, corner2.z);

        Position newMin = new Position(xMin, yMin, zMin, corner1.getUnits());
        Position newMax = new Position(xMax, yMax, zMax, corner1.getUnits());

        // If we're 0 in any dimension there is nothing we can do yet.
        if (newMin.getX() != newMax.getX() && newMin.getY() != newMax.getY() && newMin.getZ() != newMax.getZ()) {
            this.minXYZ = newMin;
            this.maxXYZ = newMax;
        }

        reset();
    }

    public void handleEvent(ProbeEvent evt) {
        if (pendingPositions.isEmpty() || !isScanning.get()) return;

        Position probeMachinePosition = evt.getProbePosition();
        if (!Double.isFinite(probeMachinePosition.getZ())) {
            reset();
            throw new RuntimeException("Probe returned invalid position");
        }

        if (probeMachinePosition.getUnits() == Units.UNKNOWN) {
            logger.warning("Unknown units in autoleveler receiving probe. Assuming " + getPreferredUnits());
        }
        probeMachinePosition = probeMachinePosition.getPositionIn(getPreferredUnits());
        Position probePosition = probeMachinePosition.add(machineWorkOffset);

        logger.log(Level.INFO, "Record ({0}, {1}, {2})",
                new Object[]{probePosition.getX(), probePosition.getY(), probePosition.getZ()});
        probeEvent(probePosition);

        if (pendingPositions.isEmpty()) {
            // The probing is done!
            moveToSafeStartPoint(probePosition);
        } else {
            double retractedZ = retract(probePosition.getZ());
            probeNextPoint(retractedZ);
        }
}

    private Units getPreferredUnits() {
        return this.backend.getSettings().getPreferredUnits();
    }

    public void reset() {
        isScanning.set(false);
        double resolution = settings.getStepResolution();

        int xAxisPoints = (int) (Math.ceil((maxXYZ.getX() - minXYZ.getX()) / resolution)) + 1;
        int yAxisPoints = (int) (Math.ceil((maxXYZ.getY() - minXYZ.getY()) / resolution)) + 1;
        this.probePositionGrid = new Position[xAxisPoints][yAxisPoints];

        // Calculate probe locations.
        for (int x = 0; x < xAxisPoints; x++) {
            for (int y = 0; y < yAxisPoints; y++) {
                Position p = new Position(
                        minXYZ.getX() + Math.min(maxXYZ.getX() - minXYZ.getX(), x * resolution),
                        minXYZ.getY() + Math.min(maxXYZ.getY() - minXYZ.getY(), y * resolution),
                        Double.NaN,
                        minXYZ.getUnits());
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

        listeners.forEach(SurfaceScannerListener::onScannerUpdate);
    }

    public void probeEvent(final Position p) {
        Position expectedProbePosition = pendingPositions.pop();
        Position probedPosition = p.getPositionIn(expectedProbePosition.getUnits());
        Position settingsOffset = settings.getAutoLevelProbeOffset().getPositionIn(getPreferredUnits());

        expectedProbePosition.setX(expectedProbePosition.getX() + settingsOffset.getX());
        expectedProbePosition.setY(expectedProbePosition.getY() + settingsOffset.getY());
        expectedProbePosition.setZ(probedPosition.getZ() + settingsOffset.getZ());
        listeners.forEach(SurfaceScannerListener::onScannerUpdate);
    }

    /**
     * Begin a scan the surface {@link #handleEvent(ProbeEvent)} must be called to properly progress through the scan.
     */
    public void scan() {
        isScanning.set(true);
        Position work = backend.getWorkPosition();
        Position machine = backend.getMachinePosition();
        machineWorkOffset = new Position(work.getUnits());
        machineWorkOffset.x = work.x - machine.x;
        machineWorkOffset.y = work.y - machine.y;
        machineWorkOffset.z = work.z - machine.z;

        moveToSafeStartPoint(work);
        probeNextPoint(maxXYZ.getZ());
    }

    private void moveToSafeStartPoint(Position currentPosition) {
        try {
            // Move up if below probe area
            double safetyHeight = (UnitUtils.scaleUnits(Units.MM, maxXYZ.getUnits()) * backend.getSettings().getSafetyHeight()) + maxXYZ.getZ();
            if (currentPosition.getPositionIn(maxXYZ.getUnits()).getZ() < safetyHeight) {
                PartialPosition safeHeightPos = PartialPosition.builder(maxXYZ.getUnits()).setZ(safetyHeight).build();
                String cmd = GcodeUtils.generateMoveCommand(
                        "G90G0", getProbeScanFeedRate(), safeHeightPos);
                logger.log(Level.INFO, "Move up to safe height {0}", new Object[]{safeHeightPos});
                backend.sendGcodeCommand(true, cmd);
            }

            // Move to the XY start position
            PartialPosition startPos = PartialPosition.builder(minXYZ)
                    .clearZ()
                    .clearABC()
                    .build();

            String cmd = GcodeUtils.generateMoveCommand(
                    "G90G0", getProbeScanFeedRate(), startPos);
            logger.log(Level.INFO, "Move to start position {0}", new Object[]{startPos});
            backend.sendGcodeCommand(true, cmd);

            // Move to the Z start position
            PartialPosition startHeight = PartialPosition.builder(maxXYZ.getUnits()).setZ(maxXYZ.getZ()).build();
            cmd = GcodeUtils.generateMoveCommand(
                    "G90G0", getProbeScanFeedRate(), startHeight);
            logger.log(Level.INFO, "Move to start height {0}", new Object[]{startHeight});
            backend.sendGcodeCommand(true, cmd);
        } catch (Exception e) {
            reset();
            throw new RuntimeException(e);
        }
    }

    public Optional<Position> getNextProbePoint() {
        return Optional.ofNullable(this.pendingPositions.peek());
    }

    private void probeNextPoint(Double zBackoff) {
        try {
            Position p = this.pendingPositions.peek();

            // Position over next probe position
            PartialPosition startPos = PartialPosition.builder(p)
                    .clearZ()
                    .clearABC()
                    .build();

            String cmd = GcodeUtils.generateMoveCommand(
                    "G90G0", getProbeScanFeedRate(), startPos);
            logger.log(Level.INFO, "MoveTo {0} {1}", new Object[]{startPos, cmd});
            backend.sendGcodeCommand(true, cmd);

            // Send probe command, probing down to zMin
            double probeDistance = minXYZ.getZ() - zBackoff;
            logger.log(Level.INFO, "Probe {0}", probeDistance);
            backend.probe("Z", getProbeSpeed(), probeDistance, getPreferredUnits());
        } catch (Exception e) {
            reset();
            throw new RuntimeException(e);
        }
    }

    private double getProbeSpeed() {
        return settings.getProbeSpeed() * UnitUtils.scaleUnits(Units.MM, getPreferredUnits());
    }

    private double getProbeScanFeedRate() {
        return settings.getProbeScanFeedRate() * UnitUtils.scaleUnits(Units.MM, getPreferredUnits());
    }

    private double retract(Double zLast) {
        double zRetract = settings.getZRetract() * maxXYZ.getZ();
        if (zRetract <= 0) {
            zRetract = maxXYZ.getZ() - minXYZ.getZ();
        }

        // Start by backing off the current position
        double zBackoff = Math.min(zLast + zRetract, maxXYZ.getZ());
        PartialPosition safeZ = PartialPosition.builder(maxXYZ.getUnits()).setZ(zBackoff).build();
        String retractCommand = GcodeUtils.generateMoveCommand(
                "G90G0",
                getProbeScanFeedRate(),
                safeZ);

        try {
            logger.log(Level.INFO, "Retract to {0} {1}", new Object[]{safeZ, retractCommand});
            backend.sendGcodeCommand(true, retractCommand);
        } catch (Exception e) {
            reset();
            throw new RuntimeException(e);
        }
        return zBackoff;
    }

    public void scanRandomData() {
        machineWorkOffset.x = 0;
        machineWorkOffset.y = 0;
        machineWorkOffset.z = 0;

        // Generate some random test data.
        while (!pendingPositions.isEmpty()) {
            Position p = new Position(pendingPositions.peek());
            p.setZ(ThreadLocalRandom.current().nextDouble(minXYZ.getZ(), maxXYZ.getZ()));
            probeEvent(p);
        }

        listeners.forEach(SurfaceScannerListener::onScannerUpdate);
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

    public boolean isValid() {
        return probePositionGrid.length > 0 && pendingPositions.isEmpty();
    }

    public void addListener(SurfaceScannerListener listener) {
        listeners.add(listener);
    }
}
