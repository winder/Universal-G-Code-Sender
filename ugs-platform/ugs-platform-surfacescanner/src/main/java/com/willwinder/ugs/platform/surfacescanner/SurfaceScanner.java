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

/**
 *
 * @author wwinder
 */
public class SurfaceScanner {
    private ImmutableCollection<Position> probePositions;
    private Position[][] probePositionGrid;

    // step error 
    final private static double STEP_OFFSET = 1;
    
    private Units units = null;
    private Position minXYZ = null;
    private Position maxXYZ = null;
    private Position probeOffset = null;
    private double resolution = 1;
    private double probeDistance = 0;
    private int yAxisPoints = -1;
    private int xAxisPoints = -1;
    private int countProbe = 0;
    private boolean scanningSurface = false;
    
    public SurfaceScanner() {
        probeOffset = new Position(Units.MM);
    }

    public void probeEvent(final Position p) {      
        Position pOffset = new Position(
                p.x + probeOffset.x, 
                p.y + probeOffset.y, 
                p.z + probeOffset.z, Units.MM).getPositionIn(units);
        
        Position pCount = probePositions.asList().get(countProbe);
        
        double pCountMinX = pCount.x - STEP_OFFSET;
        double pCountMaxX = pCount.x + STEP_OFFSET;
        double pCountMinY = pCount.y - STEP_OFFSET;
        double pCountMaxY = pCount.y + STEP_OFFSET;
              
        if(pOffset.x >= pCountMinX && pOffset.x <= pCountMaxX &&
           pOffset.y >= pCountMinY && pOffset.y <= pCountMaxY)
        {
            probePositionGrid[countProbe / yAxisPoints][countProbe % yAxisPoints] = pOffset.getPositionIn(units);
            pCount.z = pOffset.z;
            
            countProbe++;
            if(countProbe >= probePositions.size())
                scanningSurface = false;
            
        } else{
            scanningSurface = false;
            throw new IllegalArgumentException("Error in probe reference.");
        }
    }

    /**
     * Provides two points of the scanners bounding box and the number of points to sample in the X/Y directions.
     */
    public void update(final Position corner1, final Position corner2, double resolution, Units units) {
        if (corner1.getUnits() != corner2.getUnits()) {
            throw new IllegalArgumentException("Provide same unit for both measures.");
        }
        this.units = units;
        
        if (resolution == 0) return;

        double minx = Math.min(corner1.x, corner2.x);
        double maxx = Math.max(corner1.x, corner2.x);
        double miny = Math.min(corner1.y, corner2.y);
        double maxy = Math.max(corner1.y, corner2.y);
        double minz = Math.min(corner1.z, corner2.z);
        double maxz = Math.max(corner1.z, corner2.z);

        Position newMin = new Position(minx, miny, minz, units);
        Position newMax = new Position(maxx, maxy, maxz, units);


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
    
    
    
    public boolean isCollectedAllProbe(){
        return scanningSurface;
    }
    

    public void enableCollectProbe(Position work, Position machine){
        probeOffset.x = (-1 * machine.x) + work.x;
        probeOffset.y = (-1 * machine.y) + work.y;
        probeOffset.z = (-1 * machine.z) + work.z;
        countProbe = 0;
        scanningSurface = true;
    }

    public void enableTestProbe(){
        probeOffset.x = 0;
        probeOffset.y = 0;
        probeOffset.z = 0;
        countProbe = 0;
    }
    
    public ImmutableCollection<Position> getProbeStartPositions() {
        return this.probePositions;
    }

    public final Position[][] getProbePositionGrid() {
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

    public final Position getMaxXYZ() {
        return this.maxXYZ;
    }

    public final Position getMinXYZ() {
        return this.minXYZ;
    }

    public final Units getUnits() {
        return units;
    }
}
