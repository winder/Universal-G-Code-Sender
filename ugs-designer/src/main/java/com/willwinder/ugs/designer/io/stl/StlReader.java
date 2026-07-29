/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.stl;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.io.DesignReader;
import com.willwinder.ugs.designer.io.DesignReaderException;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Size;

import java.awt.EventQueue;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Imports an STL mesh as a {@link Raster} holding a depth map of the model as seen from above. The
 * raster is sized to the footprint of the mesh and its target depth is set to the height of the
 * model, so carving it as a height map reproduces the mesh at its original scale.
 *
 * @author Joacim Breiler
 */
public class StlReader implements DesignReader {

    private final StlDepthMapRasterizer rasterizer;

    public StlReader() {
        this(new StlDepthMapRasterizer());
    }

    public StlReader(StlDepthMapRasterizer rasterizer) {
        this.rasterizer = rasterizer;
    }

    @Override
    public Optional<Design> read(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return read(inputStream).map(design -> {
                design.getEntities().forEach(entity -> entity.setName(file.getName()));
                return design;
            });
        } catch (IOException e) {
            throw new DesignReaderException("Could not read the STL file " + file.getName(), e);
        }
    }

    @Override
    public Optional<Design> read(InputStream inputStream) {
        if (EventQueue.isDispatchThread()) {
            throw new DesignReaderException("Method can not be executed in dispatch thread");
        }

        StlMesh mesh;
        try {
            mesh = new StlParser().parse(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new DesignReaderException("Could not read the STL model", e);
        }

        if (mesh.isEmpty()) {
            return Optional.empty();
        }

        Design design = new Design();
        design.setEntities(List.of(createRaster(mesh)));
        return Optional.of(design);
    }

    private Entity createRaster(StlMesh mesh) {
        BufferedImage depthMap = rasterizer.rasterize(mesh);

        Raster raster = new Raster(depthMap);
        raster.setName("Depth map");
        raster.setCutType(CutType.HEIGHT_MAP);
        raster.setTargetDepth(mesh.getHeight());

        Rectangle2D.Double bounds = mesh.getBounds();
        raster.setSize(new Size(bounds.width, bounds.height));
        return raster;
    }
}
