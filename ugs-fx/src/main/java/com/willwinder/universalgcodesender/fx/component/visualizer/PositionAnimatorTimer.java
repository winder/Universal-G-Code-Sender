package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.model.Position;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class PositionAnimatorTimer extends AnimationTimer {
    private final DoubleProperty posX = new SimpleDoubleProperty(0);
    private final DoubleProperty posY = new SimpleDoubleProperty(0);
    private final DoubleProperty posZ = new SimpleDoubleProperty(0);

    private double targetX, targetY, targetZ;
    private double startX, startY, startZ;
    private long startTime = System.nanoTime();
    private long lastStartTime = System.nanoTime();

    public DoubleProperty posXProperty() {
        return posX;
    }

    public DoubleProperty posYProperty() {
        return posY;
    }

    public DoubleProperty posZProperty() {
        return posZ;
    }

    @Override
    public void handle(long now) {
        long durationNS = Math.min(startTime - lastStartTime, 500_000_000);
        double t = (now - startTime) / (double) durationNS;
        if (t >= 1.0) {
            posX.set(targetX);
            posY.set(targetY);
            posZ.set(targetZ);
            stop();
        } else {
            // Linear interpolation; change to ease-in/out if desired
            posX.set(lerp(startX, targetX, t));
            posY.set(lerp(startY, targetY, t));
            posZ.set(lerp(startZ, targetZ, t));
        }
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    public void setTarget(Position target) {
        startX = posX.get();
        startY = posY.get();
        startZ = posZ.get();
        targetX = target.getX();
        targetY = target.getY();
        targetZ = target.getZ();
        lastStartTime = startTime;
        startTime = System.nanoTime();
    }
}
