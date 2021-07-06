package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.google.gson.annotations.Expose;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;

public class CuttableEntityV1 extends EntityV1 {
    @Expose
    private double cutDepth;

    @Expose
    private CutTypeV1 cutType;

    public CuttableEntityV1(EntityTypeV1 type) {
        super(type);
    }

    public CutTypeV1 getCutType() {
        return cutType;
    }

    public void setCutType(CutTypeV1 cutType) {
        this.cutType = cutType;
    }

    public double getCutDepth() {
        return cutDepth;
    }

    public void setCutDepth(double cutDepth) {
        this.cutDepth = cutDepth;
    }

    @Override
    protected void applyCommonAttributes(Entity entity) {
        super.applyCommonAttributes(entity);

        if (entity instanceof Cuttable) {
            ((Cuttable) entity).setCutDepth(cutDepth);
            ((Cuttable) entity).setCutType(CutTypeV1.toCutType(cutType));
        }
    }
}
