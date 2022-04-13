package com.willwinder.ugs.nbp.designer.io.c2d.model;

public enum C2dPointType {
    MOVE_TO(0),
    LINE_TO(1),
    CURVE_TO(3),
    CLOSE(4);

    private final int typeId;

    C2dPointType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

}
