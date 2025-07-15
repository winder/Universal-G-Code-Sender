package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.addGeometriesToCoordinatesList;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.bufferAndCollectGeometries;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.convertAreaToGeometry;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.model.PartialPosition;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class ToolPathUtilsTest {

    /**
     * When using a tool diameter that is larger than parts of the shape the buffer may result in smaller lines.
     * Make sure that the spindle is moved up between these line passes
     */
    @Test
    public void addGeometriesToCoordinatesList_shouldHandleLineStringAsSeparateGeometries() {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(PocketToolPathTest.class.getResourceAsStream("/x.ugsd")).orElseThrow(RuntimeException::new);

        double toolDiameter = 1.2;

        Geometry geometryCollection = convertAreaToGeometry(new Area(design.getEntities().get(0).getShape()), new GeometryFactory(), 0.1);
        Geometry shell = geometryCollection.buffer(-toolDiameter / 2d);
        List<Geometry> geometries = bufferAndCollectGeometries(geometryCollection, toolDiameter, 1);
        assertEquals(4, geometries.size());

        List<List<PartialPosition>> coordinateList = new ArrayList<>();
        addGeometriesToCoordinatesList(shell, geometries, coordinateList, 0);
        assertEquals(3, coordinateList.size());
    }
}
