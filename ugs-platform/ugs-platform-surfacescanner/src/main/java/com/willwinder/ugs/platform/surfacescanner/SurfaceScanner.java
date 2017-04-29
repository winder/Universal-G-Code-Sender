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
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class SurfaceScanner {
    private ImmutableList<Position> probeLocations;

    private Position corner1 = null;
    private Position corner2 = null;
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

        this.corner1 = corner1;
        this.corner2 = corner2;

        Units units = this.corner1.getUnits();
        double minx = Math.min(this.corner1.x, this.corner2.x);
        double maxx = Math.max(this.corner1.x, this.corner2.x);
        double miny = Math.min(this.corner1.y, this.corner2.y);
        double maxy = Math.max(this.corner1.y, this.corner2.y);
        double minz = Math.min(this.corner1.z, this.corner2.z);
        double maxz = Math.max(this.corner1.z, this.corner2.z);

        probeDistance = maxz - minz;

        ImmutableList.Builder<Position> probeLocationBuilder = ImmutableList.builder();
        for(double x = minx; x <= maxx; x = Math.min(maxx, x + resolution)) {
            for(double y = miny; y <= maxy; y = Math.min(maxy, y + resolution)) {
                probeLocationBuilder.add(new Position(x, y, maxz, units));
                if (y == maxy) break;
            }
            if (x == maxx) break;
        }

        probeLocations = probeLocationBuilder.build();
    }

    public ImmutableList<Position> getProbeLocations() {
        return probeLocations;
    }
}
