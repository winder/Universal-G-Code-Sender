package com.willwinder.universalgcodesender.uielements.components;

import static com.willwinder.universalgcodesender.utils.MathUtils.clamp;

import javax.swing.JSlider;

/**
 * A JSlider that maps its integer model to a double range.
 * <p/>
 * Resolution is controlled by "steps": number of discrete positions between min and max.
 * Example: steps=1000 means ~0.001 precision over a 0..1 range.
 */
public class DoubleSlider extends JSlider {
    private final double minDouble;
    private final double maxDouble;
    private final int steps;

    public DoubleSlider(double minDouble, double maxDouble, int steps) {
        this(HORIZONTAL, minDouble, maxDouble, steps);
    }

    public DoubleSlider(int orientation, double minDouble, double maxDouble, int steps) {
        super(orientation, 0, Math.max(1, steps), 0);

        if (!Double.isFinite(minDouble) || !Double.isFinite(maxDouble)) {
            throw new IllegalArgumentException("minDouble/maxDouble must be finite");
        }
        if (maxDouble <= minDouble) {
            throw new IllegalArgumentException("maxDouble must be > minDouble");
        }

        this.minDouble = minDouble;
        this.maxDouble = maxDouble;
        this.steps = Math.max(1, steps);

        setDoubleValue(minDouble);
    }

    public int getSteps() {
        return steps;
    }

    public double getDoubleValue() {
        double t = (double) super.getValue() / (double) steps; // 0..1
        return minDouble + t * (maxDouble - minDouble);
    }

    public void setDoubleValue(double value) {
        double clamped = clamp(value, minDouble, maxDouble);
        double t = (clamped - minDouble) / (maxDouble - minDouble); // 0..1
        int iv = (int) Math.round(t * steps);
        super.setValue(clamp(iv, 0, steps));
    }
}