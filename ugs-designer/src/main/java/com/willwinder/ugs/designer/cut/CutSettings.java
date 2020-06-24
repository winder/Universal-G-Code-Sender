package com.willwinder.ugs.designer.cut;

public class CutSettings {
    private CutType cutType = CutType.ON_PATH;
    private double depth = 10;

    public CutType getCutType() {
        return cutType;
    }

    public void setCutType(CutType cutType) {
        this.cutType = cutType;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }

    public double getDepth() {
        return depth;
    }
}
