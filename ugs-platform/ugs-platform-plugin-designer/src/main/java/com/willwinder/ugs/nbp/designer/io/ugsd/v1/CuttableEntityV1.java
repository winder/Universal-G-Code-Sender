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
    private int spindleSpeed;

    @Expose
    private int passes;

    @Expose
    private int feedRate;

    @Expose
    private CutTypeV1 cutType;

    @Expose
    private boolean isHidden;

    @Expose
    private int leadInPercent;

    @Expose
    private int leadOutPercent;

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

    public void setCutDepth(double cutDepth) {
        this.cutDepth = cutDepth;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public void setSpindleSpeed(int spindleSpeed) {
        this.spindleSpeed = spindleSpeed;
    }

    public void setPasses(int passes) {
        this.passes = passes;
    }

    public void setFeedRate(int feedRate) {
        this.feedRate = feedRate;
    }

    public void setLeadInPercent(int leadInPercent) {
        this.leadInPercent = leadInPercent;
    }

    public void setLeadOutPercent(int leadOutPercent) {
        this.leadOutPercent = leadOutPercent;
    }

    @Override
    protected void applyCommonAttributes(Entity entity) {
        super.applyCommonAttributes(entity);

        // We need to make a copy of the transformation to set the affine transformation state and type which is not serialized
        entity.setTransform(new AffineTransform(transform.getScaleX(), transform.getShearY(), transform.getShearX(), transform.getScaleY(), transform.getTranslateX(), transform.getTranslateY()));

        if (entity instanceof Cuttable cuttable) {
            cuttable.setStartDepth(startDepth);
            cuttable.setTargetDepth(cutDepth);
            cuttable.setCutType(CutTypeV1.toCutType(cutType));
            cuttable.setHidden(isHidden);
            cuttable.setSpindleSpeed(spindleSpeed);
            cuttable.setPasses(passes);
            cuttable.setFeedRate(feedRate);
            cuttable.setLeadInPercent(leadInPercent);
            cuttable.setLeadOutPercent(leadOutPercent);
        }
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }
}
