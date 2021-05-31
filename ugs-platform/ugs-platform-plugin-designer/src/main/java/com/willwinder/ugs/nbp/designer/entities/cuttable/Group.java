package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntityGroup;

public class Group extends EntityGroup implements Cuttable {
    private CutType cutType = CutType.NONE;

    @Override
    public CutType getCutType() {
        return cutType;
    }

    @Override
    public void setCutType(CutType cutType) {
        this.cutType = cutType;
    }
}
