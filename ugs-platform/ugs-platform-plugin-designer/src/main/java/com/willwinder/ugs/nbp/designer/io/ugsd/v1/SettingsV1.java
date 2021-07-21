package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.model.UnitUtils;

public class SettingsV1 {
    private int feedSpeed = 1000;
    private int plungeSpeed = 400;
    private double toolDiameter = 3d;
    private double stockThickness = 20;
    private double safeHeight = 10;
    private UnitUtils.Units preferredUnits = UnitUtils.Units.MM;
    private double toolStepOver = 0.3;
    private double depthPerPass = 1;

    public int getPlungeSpeed() {
        return plungeSpeed;
    }

    public void setPlungeSpeed(int plungeSpeed) {
        this.plungeSpeed = plungeSpeed;
    }

    public double getToolDiameter() {
        return toolDiameter;
    }

    public void setToolDiameter(double toolDiameter) {
        this.toolDiameter = toolDiameter;
    }

    public double getStockThickness() {
        return stockThickness;
    }

    public void setStockThickness(double stockThickness) {
        this.stockThickness = stockThickness;
    }

    public double getSafeHeight() {
        return safeHeight;
    }

    public void setSafeHeight(double safeHeight) {
        this.safeHeight = safeHeight;
    }

    public UnitUtils.Units getPreferredUnits() {
        return preferredUnits;
    }

    public void setPreferredUnits(UnitUtils.Units preferredUnits) {
        this.preferredUnits = preferredUnits;
    }

    public double getToolStepOver() {
        return toolStepOver;
    }

    public void setToolStepOver(double toolStepOver) {
        this.toolStepOver = toolStepOver;
    }

    public void setFeedSpeed(int feedSpeed) {
        this.feedSpeed = feedSpeed;
    }

    public int getFeedSpeed() {
        return feedSpeed;
    }

    public void setDepthPerPass(double depthPerPass) {
        this.depthPerPass = depthPerPass;
    }

    public double getDepthPerPass() {
        return depthPerPass;
    }

    public Settings toInternal() {
        Settings settings = new Settings();
        settings.setSafeHeight(safeHeight);
        settings.setPreferredUnits(preferredUnits);
        settings.setToolStepOver(toolStepOver);
        settings.setToolDiameter(toolDiameter);
        settings.setStockThickness(stockThickness);
        settings.setPlungeSpeed(plungeSpeed);
        settings.setDepthPerPass(depthPerPass);
        settings.setFeedSpeed(feedSpeed);
        return settings;
    }
}
