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

/**
 *
 * @author wwinder
 */
public class SurfaceScanner {
    private ImmutableCollection<Position> probePositions;
    private Collection<Position> surfacePositions;

    private Position minXYZ = null;
    private Position maxXYZ = null;
    private double resolution = 1;
    private double probeDistance = 0;
    
    public SurfaceScanner() {
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

        this.minXYZ = new Position(minx, miny, minz, units);
        this.maxXYZ = new Position(maxx, maxy, maxz, units);
        this.probeDistance = maxz - minz;
        this.resolution = resolution;

        // Calculate probe locations.
        ImmutableList.Builder<Position> probePositionBuilder = ImmutableList.builder();
        for(double x = minx; x <= maxx; x = Math.min(maxx, x + resolution)) {
            for(double y = miny; y <= maxy; y = Math.min(maxy, y + resolution)) {
                probePositionBuilder.add(new Position(x, y, maxz, units));
                if (y == maxy) break;
            }
            if (x == maxx) break;
        }

        this.probePositions = probePositionBuilder.build();
    }

    public ImmutableCollection<Position> getProbePositions() {
        return this.probePositions;
    }

    public double getProbeDistance() {
        return this.probeDistance;
    }
}
