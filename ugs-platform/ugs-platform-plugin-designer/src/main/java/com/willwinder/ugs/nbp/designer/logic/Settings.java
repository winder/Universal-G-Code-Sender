package com.willwinder.ugs.nbp.designer.logic;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class Settings {
    private Set<SettingsListener> listeners = new HashSet<>();
    private int feedSpeed = 1000;
    private int plungeSpeed = 400;
    private double toolDiameter = 3d;
    private double stockThickness = 20;
    private double safeHeight = 10;
    private UnitUtils.Units preferredUnits = UnitUtils.Units.MM;
    private Size stockSize = new Size(300, 200);
    private double toolStepOver = 0.3;

    public int getFeedSpeed() {
        return feedSpeed;
    }

    public void setFeedSpeed(int feedSpeed) {
        this.feedSpeed = feedSpeed;
        notifyListeners();
    }

    public int getPlungeSpeed() {
        return plungeSpeed;
    }

    public void setPlungeSpeed(int plungeSpeed) {
        this.plungeSpeed = plungeSpeed;
        notifyListeners();
    }

    /**
     * Returns the tool diameter in millimeters
     *
     * @return the tool diameter
     */
    public double getToolDiameter() {
        return toolDiameter;
    }

    public void setToolDiameter(double toolDiameter) {
        this.toolDiameter = toolDiameter;
        notifyListeners();
    }

    /**
     * Returns the stock size in millimeters
     *
     * @return the stock size
     */
    public Size getStockSize() {
        return stockSize;
    }

    public void setStockSize(Size size) {
        this.stockSize = size;
        notifyListeners();
    }

    /**
     * Returns the stock thickness in millimeters
     *
     * @return the stock thickness
     */
    public double getStockThickness() {
        return stockThickness;
    }

    public void setStockThickness(double thickness) {
        this.stockThickness = thickness;
        notifyListeners();
    }

    private void notifyListeners() {
        listeners.forEach(SettingsListener::onSettingsChanged);
    }

    public void addListener(SettingsListener settingsListener) {
        listeners.add(settingsListener);
    }

    /**
     * Returns the general safety height in millimeters
     *
     * @return the safety height
     */
    public double getSafeHeight() {
        return safeHeight;
    }

    public void setSafeHeight(double safeHeight) {
        this.safeHeight = safeHeight;
        notifyListeners();
    }

    /**
     * Returns the preferred units to view in the UI
     *
     * @return the preferred viewing units
     */
    public UnitUtils.Units getPreferredUnits() {
        return preferredUnits;
    }

    public void setPreferredUnits(UnitUtils.Units preferredUnits) {
        this.preferredUnits = preferredUnits;
        notifyListeners();
    }

    /**
     * Returns the percentage of how much the tool should cut each time given a value between 0.01 and 1.
     *
     * @return the percentage
     */
    public double getToolStepOver() {
        return toolStepOver;
    }

    public void setToolStepOver(double toolStepOver) {
        this.toolStepOver = toolStepOver;
    }

    @NotNull
    public String getStockSizeText() {
        double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, getPreferredUnits());
        return Utils.formatter.format(getStockSize().getWidth() * scale) + " x " + Utils.formatter.format(getStockSize().getHeight() * scale) + " x " + Utils.formatter.format(getStockThickness() * scale) + " " + getPreferredUnits().abbreviation;
    }
}
