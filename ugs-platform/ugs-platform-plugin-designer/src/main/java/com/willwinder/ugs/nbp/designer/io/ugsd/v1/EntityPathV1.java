/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class EntityPathV1 extends CuttableEntityV1 {
    private List<EntityPathSegmentV1> segments;

    public EntityPathV1() {
        super(EntityTypeV1.PATH);
    }

    @Override
    public Entity toInternal() {
        Path path = new Path();
        final Point2D latestMoveTo = new Point2D.Double();
        getSegments().forEach(segment -> {
            switch(segment.getType()) {
                case MOVE_TO:
                    path.moveTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1]);
                    latestMoveTo.setLocation(new Point2D.Double(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1]));
                    break;
                case LINE_TO:
                    path.lineTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1]);
                    break;
                case QUAD_TO:
                    path.quadTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1], segment.getCoordinates().get(1)[0], segment.getCoordinates().get(1)[1]);
                    break;
                case CUBIC_TO:
                    path.curveTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1], segment.getCoordinates().get(1)[0], segment.getCoordinates().get(1)[1], segment.getCoordinates().get(2)[0], segment.getCoordinates().get(2)[1]);
                    break;
                case CLOSE:
                    path.lineTo(latestMoveTo.getX(), latestMoveTo.getY());
                    path.close();
            }
        });
        applyCommonAttributes(path);
        return path;
    }

    public List<EntityPathSegmentV1> getSegments() {
        return segments;
    }

    public void setSegments(List<EntityPathSegmentV1> segments) {
        this.segments = segments;
    }
}
