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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.util.Collection;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class SurfaceScanner {
    private ImmutableCollection<Position> probePositions;
    private Point3d[][] probePositionGrid;
    private Collection<Position> surfacePositions;

    private Units u = null;
    private Point3d minXYZ = null;
    private Point3d maxXYZ = null;
    private double resolution = 1;
    private double probeDistance = 0;
    private int yAxisPoints = -1;
    private int xAxisPoints = -1;
    
    public SurfaceScanner(Units u) {
        this.u = u;
    }

    public void probeEvent(final Position p) {
        int x = (int) Math.ceil((p.x - minXYZ.x) / resolution);
        int y = (int) Math.ceil((p.y - minXYZ.y) / resolution);

        probePositionGrid[x][y] = p.getPositionIn(u);
    }

    /**
     * Provides two points of the scanners bounding box and the number of points to sample in the X/Y directions.
     */
    public void update(final Position corner1, final Position corner2, double resolution) {
        if (corner1.getUnits() != corner2.getUnits()) {
            throw new IllegalArgumentException("Provide same unit for both measures.");
        }

        if (resolution == 0) return;

        Units units = corner1.getUnits();
        double minx = Math.min(corner1.x, corner2.x);
        double maxx = Math.max(corner1.x, corner2.x);
        double miny = Math.min(corner1.y, corner2.y);
        double maxy = Math.max(corner1.y, corner2.y);
        double minz = Math.min(corner1.z, corner2.z);
        double maxz = Math.max(corner1.z, corner2.z);

        Position newMin = new Position(minx, miny, minz, units);
        Position newMax = new Position(maxx, maxy, maxz, units);

        // Make sure the position changed before resetting things.
        if (newMin.equals(this.minXYZ) && newMax.equals(this.maxXYZ) && this.resolution == resolution) {
            return;
        }

        this.minXYZ = newMin;
        this.maxXYZ = newMax;
        this.probeDistance = minz - maxz;
        this.resolution = resolution;

        this.xAxisPoints = (int) (Math.ceil((maxx - minx) / resolution)) + 1;
        this.yAxisPoints = (int) (Math.ceil((maxy - miny) / resolution)) + 1;
        this.probePositionGrid = new Position[this.xAxisPoints][this.yAxisPoints];

        // Calculate probe locations.
        ImmutableList.Builder<Position> probePositionBuilder = ImmutableList.builder();
        for (int x = 0; x < this.xAxisPoints; x++) {
            for (int y = 0; y < this.yAxisPoints; y++) {
                Position p = new Position(
                        minx + Math.min(maxx-minx, x*resolution),
                        miny + Math.min(maxy-miny, y*resolution),
                        maxz,
                        units);
                probePositionBuilder.add(p);
            }
        }

        this.probePositions = probePositionBuilder.build();
    }

    public ImmutableCollection<Position> getProbeStartPositions() {
        return this.probePositions;
    }

    public final Point3d[][] getProbePositionGrid() {
        return this.probePositionGrid;
    }

    public double getProbeDistance() {
        return this.probeDistance;
    }

    public int getXAxisPoints() {
        return this.xAxisPoints;
    }

    public int getYAxisPoints() {
        return this.yAxisPoints;
    }

    public final Point3d getMaxXYZ() {
        return this.maxXYZ;
    }

    public final Point3d getMinXYZ() {
        return this.minXYZ;
    }

    public final Units getUnits() {
        return u;
    }
}
