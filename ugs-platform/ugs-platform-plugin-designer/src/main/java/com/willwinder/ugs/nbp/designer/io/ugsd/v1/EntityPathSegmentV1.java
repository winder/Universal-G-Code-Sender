package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

public class EntityPathSegmentV1 implements Serializable {

    @Expose
    private EntityPathTypeV1 type;
    private List<Double[]> coordinates;

    public EntityPathTypeV1 getType() {
        return type;
    }

    public void setType(EntityPathTypeV1 type) {
        this.type = type;
    }

    public void setCoordinates(List<Double[]> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Double[]> getCoordinates() {
        return coordinates;
    }
}
