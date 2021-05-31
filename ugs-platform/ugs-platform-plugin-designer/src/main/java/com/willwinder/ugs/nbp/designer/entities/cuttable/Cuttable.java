package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;

public interface Cuttable extends Entity {
    CutType getCutType();

    void setCutType(CutType cutType);


    double getToolDiameter();

    void setToolDiameter(double toolDiameter)
}
