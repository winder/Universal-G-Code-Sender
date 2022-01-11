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

import com.google.gson.annotations.Expose;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;

import java.awt.geom.AffineTransform;

/**
 * @author Joacim Breiler
 */
public class CuttableEntityV1 extends EntityV1 {
    @Expose
    private double startDepth;

    @Expose
    private double cutDepth;

    @Expose
    private CutTypeV1 cutType;

    @Expose
    private AffineTransform transform;

    public CuttableEntityV1(EntityTypeV1 type) {
        super(type);
    }

    public CutTypeV1 getCutType() {
        return cutType;
    }

    public void setCutType(CutTypeV1 cutType) {
        this.cutType = cutType;
    }

    public double getStartDepth() {
        return startDepth;
    }

    public void setStartDepth(double startDepth) {
        this.startDepth = startDepth;
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

        // We need to make a copy of the transformation to set the affine transformation state and type which is not serialized
        entity.setTransform(new AffineTransform(transform.getScaleX(), transform.getShearY(), transform.getShearX(), transform.getScaleY(), transform.getTranslateX(), transform.getTranslateY()));

        if (entity instanceof Cuttable) {
            ((Cuttable) entity).setStartDepth(startDepth);
            ((Cuttable) entity).setTargetDepth(cutDepth);
            ((Cuttable) entity).setCutType(CutTypeV1.toCutType(cutType));
        }
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }
}
