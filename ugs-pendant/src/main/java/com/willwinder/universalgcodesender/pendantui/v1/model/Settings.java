package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.io.Serializable;

public class Settings implements Serializable {

    private double jogFeedRate;
    private double jogStepSizeXY;
    private UnitUtils.Units preferredUnits;
    private double jogStepSizeZ;

    public void setJogFeedRate(double jogFeedRate) {
        this.jogFeedRate = jogFeedRate;
    }

    public double getJogFeedRate() {
        return jogFeedRate;
    }

    public void setJogStepSizeXY(double jogStepSizeXY) {
        this.jogStepSizeXY = jogStepSizeXY;
    }

    public double getJogStepSizeXY() {
        return jogStepSizeXY;
    }

    public void setPreferredUnits(UnitUtils.Units preferredUnits) {
        this.preferredUnits = preferredUnits;
    }

    public UnitUtils.Units getPreferredUnits() {
        return preferredUnits;
    }

    public double getJogStepSizeZ() {
        return jogStepSizeZ;
    }

    public void setJogStepSizeZ(double jogStepSizeZ) {
        this.jogStepSizeZ = jogStepSizeZ;
    }
}
