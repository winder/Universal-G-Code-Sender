package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;

import java.util.List;

public class EntityPathV1 extends CuttableEntityV1 {
    private double x;
    private double y;
    private double rotation;
    private List<EntityPathSegmentV1> segments;

    public EntityPathV1() {
        super(EntityTypeV1.PATH);
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public Entity toInternal() {
        Path path = new Path();
        getSegments().forEach(segment -> {
            switch(segment.getType()) {
                case MOVE_TO:
                    path.moveTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1]);
                case LINE_TO:
                    path.lineTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1]);
                    break;
                case QUAD_TO:
                    path.quadTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1], segment.getCoordinates().get(1)[0], segment.getCoordinates().get(1)[1]);
                    break;
                case CUBIC_TO:
                    path.curveTo(segment.getCoordinates().get(0)[0], segment.getCoordinates().get(0)[1], segment.getCoordinates().get(1)[0], segment.getCoordinates().get(1)[1], segment.getCoordinates().get(2)[0], segment.getCoordinates().get(2)[1]);
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
