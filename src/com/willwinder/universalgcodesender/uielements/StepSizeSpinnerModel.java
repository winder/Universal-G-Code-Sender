/*
 * A custom spinner that will step full integers unless less than or equal to 1
 * at which point it will step tenths unless less than or equal to .1 at which
 * point it will step hundredths, etc.
 */
package com.willwinder.universalgcodesender.uielements;

import javax.swing.SpinnerNumberModel;

/**
 *
 * @author wwinder
 */
public class StepSizeSpinnerModel extends SpinnerNumberModel {

    public StepSizeSpinnerModel(Number value, Comparable min, Comparable max, Number size) {
        super (value, min, max, size);
        maxStepSize = size.doubleValue();
    }
    Double maxStepSize = null;
    
//    @Override
//    public void setStepSize(Number newStepSize) {
//        super.setStepSize(newStepSize);
//    }
    
    private Double getPreviousStepSize() {
        Number stepSize = this.getStepSize();
        
        Double size =  stepSize.doubleValue();

        //System.out.println("Size = "+size+,"stepSize = "+stepSize.doubleValue());
        // Determine if step size needs adjusting
        if (getNumber().doubleValue() == this.getStepSize().doubleValue()) {
            size /= 10.0;
        }

        return size;
    }

    private Double getNextStepSize() {
        Double size = this.getStepSize().doubleValue();
        
        System.out.println("Current number = "+getNumber().doubleValue()+", step size = "+size+",size * 10 = "+10*size);
        System.out.println("Max step size = " + maxStepSize);
        if (getNumber().doubleValue() >= (10 * size)) {
            if (this.getStepSize().doubleValue() < maxStepSize) {
                System.out.println("ADJUST");

                size *= 10;
                if (size > maxStepSize) {
                    size = maxStepSize;
                }
            }
        }

        if (this.getNumber().doubleValue() == 0) {
            size = .0001;
        }

        return size;
    }

    /**
     * Returns the next value, or
     * <code>null</code> if adding the step size to the current value results in
     * a value greater than the maximum value. The current value is not changed.
     *
     * @return The next value, or
     * <code>null</code> if the current value is the maximum value represented
     * by this model.
     */
    @Override
    public Object getNextValue() {
        Double num;
        this.setStepSize(getNextStepSize());

        num = new Double(this.getNumber().doubleValue() + this.getStepSize().doubleValue());

        // check upper bound if set
        if ((this.getMaximum() != null) && this.getMaximum().compareTo(num) < 0) {
            num = null;
        }

        System.out.println("Next value: " + num + ", step size = " + this.getStepSize().doubleValue());
        return num;
    }

    /**
     * Returns the previous value, or
     * <code>null</code> if subtracting the step size from the current value
     * results in a value less than the minimum value. The current value is not
     * changed.
     *
     * @return The previous value, or
     * <code>null</code> if the current value is the minimum value represented
     * by this model.
     */
    @Override
    public Object getPreviousValue() {
        Double num;
        this.setStepSize(getPreviousStepSize());

        num = new Double(this.getNumber().doubleValue() - this.getStepSize().doubleValue());

        // check lower bound if set
        if ((this.getMinimum() != null) && this.getMinimum().compareTo(num) > 0) {
            num = null;
        }

        System.out.println("Previous value: " + num + ", step size = " + this.getStepSize());
        return num;
    }
}



/*
public class StepSizeSpinnerModel extends AbstractSpinnerModel
        implements Serializable {

    /**
     * For compatability with Sun's JDK
     *
    private static final long serialVersionUID = 7279176385485777821L;
    /**
     * The current value.
     *
    private Double value;
    /**
     * The minimum value (or
     * <code>null</code>).
     *
    private Double minimum;
    /**
     * The maximum value (or
     * <code>null</code>).
     *
    private Double maximum;
    /**
     * The step size.
     *
    private Double stepSize;
    /**
     * Max step size.
     *
    private Double maxStepSize;

    /**
     * Creates a
     * <code>StepSizeSpinnerModel</code> with initial value 0, step 1, and no
     * maximum nor minimum.
     *
    public StepSizeSpinnerModel() {
        this(new Double(0), null, null, new Double(1));
    }

    /**
     * Creates a
     * <code>SpinnerNumberModel</code> with double precision.
     *
     * @param value the initial value
     * @param minimum the minimum value
     * @param maximum the maximum value
     * @param stepSize the step size
     * @throws IllegalArgumentException if minimum &lt;= value &lt;= maximum
     * does not hold.
     *
    public StepSizeSpinnerModel(double value, double minimum, double maximum,
            double stepSize) {
        this(new Double(value), new Double(minimum), new Double(maximum),
                new Double(stepSize));
    }
    
    public StepSizeSpinnerModel(int value, int minimum, int maximum,
            int stepSize) {
        this(new Double(value), new Double(minimum), new Double(maximum),
                new Double(stepSize));
    }


    /**
     * Creates a
     * <code>StepSizeSpinnerModel</code> with the given attributes. The caller
     * should ensure that both
     * <code>minimum</code> and
     * <code>maximum</code> are serializable.
     *
     * @param value the initial value (
     * <code>null</code> not permitted).
     * @param minimum the minimum value (
     * <code>null</code> permitted).
     * @param maximum the maximum value (
     * <code>null</code> permitted).
     * @param stepSize the step size  (
     * <code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if minimum &lt;= value &lt;= maximum
     * does not hold
     * @throws IllegalArgumentException if
     * <code>value</code> is
     * <code>null</code>.
     * @throws IllegalArgumentException if
     * <code>stepSize</code> is
     * <code>null</code>.
     *
    public StepSizeSpinnerModel(Double value, Double minimum,
            Double maximum, Double stepSize) {
        if (stepSize == null) {
            throw new IllegalArgumentException("stepSize may not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        if (minimum != null) {
            if (minimum.compareTo(value) > 0) {
                throw new IllegalArgumentException("minimum is not <= value");
            }
        }
        if (maximum != null) {
            if (maximum.compareTo(value) < 0) {
                throw new IllegalArgumentException("maximum is not >= value");
            }
        }

        this.value = value;
        this.stepSize = stepSize;
        this.maxStepSize = stepSize;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Sets the current value and, if the new value is different to the old
     * value, sends a {@link ChangeEvent} to all registered listeners.
     *
     * @param value the new value (
     * <code>null</code> not permitted, must be an instance of
     * <code>Double</code>).
     *
    @Override
    public void setValue(Object value) {
        System.out.println("setvalue: "+value);
        
        if (!(value instanceof Double)) {
            System.out.println("Exception?");
            throw new IllegalArgumentException("value must be a Double");
        }

        if (!this.value.equals(value)) {
            System.out.println("Changing the value");
            this.value = (Double) value;
            fireStateChanged();
        }
    }

    /**
     * Returns the current value, which for this class is always an instance of
     * {@link Number}.
     *
     * @return The current value.
     *
    @Override
    public Object getValue() {
        return value;
    }


    /**
     * Returns the current value.
     *
     * @return The current value.
     *
    public Double getNumber() {
        return value;
    }

    /**
     * Returns the minimum value, or
     * <code>null</code> if there is no minimum.
     *
     * @return The minimum value.
     *
    public Double getMinimum() {
        return minimum;
    }

    /**
     * Sets the minimum value and, if the new value is different to the old
     * value, sends a {@link ChangeEvent} to all registered listeners. A
     * <code>null</code> value is interpreted as "no minimum value". No check is
     * made to ensure that the new minimum is less than or equal to the current
     * value, the caller is responsible for ensuring that this relationship
     * holds. In addition, the caller should ensure that
     * <code>newMinimum</code> is {@link Serializable}.
     *
     * @param newMinimum the new minimum value (
     * <code>null</code> permitted).
     *
     * @see #getMinimum()
     *
    public void setMinimum(Double newMinimum) {
        if (minimum != null ? !minimum.equals(newMinimum) : newMinimum != null) {
            minimum = newMinimum;
            fireStateChanged();
        }
    }

    /**
     * Returns the maximum value, or
     * <code>null</code> if there is no maximum.
     *
     * @return The maximum value.
     *
    public Double getMaximum() {
        return maximum;
    }

    /**
     * Sets the maximum value and, if the new value is different to the old
     * value, sends a {@link ChangeEvent} to all registered listeners. A
     * <code>null</code> value is interpreted as "no maximum value". No check is
     * made to ensure that the new maximum is greater than or equal to the
     * current value, the caller is responsible for ensuring that this
     * relationship holds. In addition, the caller should ensure that
     * <code>newMaximum</code> is {@link Serializable}.
     *
     * @param newMaximum the new maximum (
     * <code>null</code> permitted).
     *
     * @see #getMaximum()
     *
    public void setMaximum(Double newMaximum) {
        if (maximum != null ? !maximum.equals(newMaximum) : newMaximum != null) {
            maximum = newMaximum;
            fireStateChanged();
        }
    }

    /**
     * Returns the step size.
     *
     * @return The step size (never
     * <code>null</code>).
     *
    public Number getStepSize() {
        return stepSize;
    }

    /**
     * Sets the step size and, if the new step size is different to the old step
     * size, sends a {@link ChangeEvent} to all registered listeners.
     *
     * @param newStepSize the new step size (
     * <code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if
     * <code>newStepSize</code> is
     * <code>null</code>.
     *
    public void setStepSize(Double newStepSize) {
        if (newStepSize == null) {
            throw new IllegalArgumentException();
        }

        if (!stepSize.equals(newStepSize)) {
            stepSize = newStepSize;
            fireStateChanged();
        }
    }
}
*/
