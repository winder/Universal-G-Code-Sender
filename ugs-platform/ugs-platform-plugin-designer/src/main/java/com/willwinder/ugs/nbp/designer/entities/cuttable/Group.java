package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntityGroup;

public class Group extends EntityGroup implements Cuttable {
    private CutType cutType = CutType.NONE;

    public Group() {
        setName("Group");
    }

    @Override
    public CutType getCutType() {
        return cutType;
    }

    @Override
    public void setCutType(CutType cutType) {
        this.cutType = cutType;
    }

    @Override
    public double getCutDepth() {
        return 0;
    }

    @Override
    public void setCutDepth(double cutDepth) {

    }
}
