/*
    Copyright 2021-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class OutlineToolPath extends AbstractToolPath {
    private final Cuttable source;

    private double offset;

    public OutlineToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private static void addGeometriesToCoordinateList(ArrayList<List<PartialPosition>> coordinateList, List<PartialPosition> geometryCoordinates, double depth) {
        coordinateList.add(geometryCoordinates.stream()
                .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).setZ(-depth).build())
                .toList());
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
    
    @Override
    protected Double getSafeHeightToUse(Double currentZ, boolean isFirst) {
        
        Double result = settings.getSafeHeight()+currentZ;
        if (isFirst) {
            result = settings.getSafeHeight();
        }
        // Outline Paths always Start and end in the same spot so its worthwhile to only climb a smaller amount
        return result;
    }
    
    public final static int ON_PATH = 0x00;
    public final static int INSIDE_PATH = 0x01;
    public final static int OUTSIDE_PATH = 0x02;
    
    public double toolWidth = settings.getToolDiameter();
    public double lineWidth = 12.0;
    public int cutMode = INSIDE_PATH;
    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        List<Geometry> geometries;
        
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory());
            
            if (toolWidth >= lineWidth) {
                Geometry bufferedGeometry = geometry.buffer(offset);
                geometries = ToolPathUtils.toGeometryList(bufferedGeometry);                
            } else {
                geometries = new ArrayList<>();
                Geometry bufferedGeometry;
                
                double actualWidth = (cutMode == ON_PATH ? 2.0 : 1.0);
                double stepSize = ( lineWidth / actualWidth  ) / ( toolWidth * settings.getToolStepOver() );
                
                switch (cutMode) {                    
                    case ON_PATH: {                        
                        bufferedGeometry = geometry.buffer(0); // On path First.
                        geometries = ToolPathUtils.toGeometryList(bufferedGeometry);                         
                        
                        // Then Alternating inside/out 
                        for (double curStep = stepSize; curStep <= actualWidth; curStep += stepSize ) {
                            bufferedGeometry = geometry.buffer(curStep);
                            geometries.addAll(ToolPathUtils.toGeometryList(bufferedGeometry));  
                            bufferedGeometry = geometry.buffer(-curStep);
                            geometries.addAll(ToolPathUtils.toGeometryList(bufferedGeometry));                              
                        }
                        
                        
                    }
                    break;
                    
                    case INSIDE_PATH: {
                        // to cut a clean inside path we go from outside In:
                        for (double x = offset+lineWidth; x >= (offset); x-=stepSize) {                            
                            bufferedGeometry = geometry.buffer(x);
                            geometries.addAll(ToolPathUtils.toGeometryList(bufferedGeometry));                              
                        }                        
                    }
                    break;
                    
                    case OUTSIDE_PATH: {
                        // to cut a clean outside path we go from inside out;
                        for (double x = offset-lineWidth; x <= (offset); x+=stepSize) {
                            bufferedGeometry = geometry.buffer(x);
                            geometries.addAll(ToolPathUtils.toGeometryList(bufferedGeometry));  
                        }                                              
                    }
                    break;                        
                }
            }
            
        } else {     
            
            // TODO: Loop through ( 0 -> width ) or ( width -> 0 ) and create an expanding or
            // Shrinking set of geometries. 
            geometries = ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory());
        }


        ArrayList<List<PartialPosition>> coordinateList = new ArrayList<>();
        geometries.forEach(g -> {
            List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(g);

            addGeometriesToCoordinateList(coordinateList, geometryCoordinates, getStartDepth());

            double currentDepth = getStartDepth();
            while (currentDepth < getTargetDepth()) {
                currentDepth += settings.getDepthPerPass();
                if (currentDepth > getTargetDepth()) {
                    currentDepth = getTargetDepth();
                }

                addGeometriesToCoordinateList(coordinateList, geometryCoordinates, currentDepth);
            }
        });

        addToGcodePath(gcodePath, coordinateList, source);
    }
}
