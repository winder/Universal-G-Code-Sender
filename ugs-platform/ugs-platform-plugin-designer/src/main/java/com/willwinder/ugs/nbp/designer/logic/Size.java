package com.willwinder.ugs.nbp.designer.logic;

import java.io.Serializable;

public class Size implements Serializable {
    private double width;
    private double height;

    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
