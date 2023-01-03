package com.willwinder.ugs.nbp.dro.panels;

import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.text.DecimalFormat;

public class CoordinateLabel extends JLabel {

    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000");

    private double value = 0.0;
    private boolean highlighted = false;
    private boolean isEnabled = true;

    public CoordinateLabel(double value) {
        setValue(value);
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        updateColor();
    }

    @Override
    public boolean isEnabled() {
        // Force returning true or else we will get problems with colors on some LaFs
        return true;
    }

    public double getValue() {
        return value;
    }


    public void setValue(double value) {
        this.value = value;
        String textValue = decimalFormatter.format(value);
        setText(textValue);
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        updateColor();
    }

    private void updateColor() {
        Color color = isEnabled ? ThemeColors.LIGHT_BLUE : ThemeColors.LIGHT_BLUE_GREY;

        if (highlighted) {
            color = ThemeColors.RED;
        }

        setForeground(color);
    }
}
