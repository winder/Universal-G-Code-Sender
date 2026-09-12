package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProbeSettings implements Serializable {
    private double feedRateFast;
    private double feedRateSlow;
    private double retractDistance;
    private double delayAfterRetract;
    private double probeDiameter;
    private double plateThickness;
    private double maxTravel;
    private boolean compensateSoftLimits;

    public double getFeedRateFast() {
        return feedRateFast;
    }

    public void setFeedRateFast(double feedRateFast) {
        this.feedRateFast = feedRateFast;
    }

    public double getFeedRateSlow() {
        return feedRateSlow;
    }

    public void setFeedRateSlow(double feedRateSlow) {
        this.feedRateSlow = feedRateSlow;
    }

    public double getRetractDistance() {
        return retractDistance;
    }

    public void setRetractDistance(double retractDistance) {
        this.retractDistance = retractDistance;
    }

    public double getDelayAfterRetract() {
        return delayAfterRetract;
    }

    public void setDelayAfterRetract(double delayAfterRetract) {
        this.delayAfterRetract = delayAfterRetract;
    }

    public double getProbeDiameter() {
        return probeDiameter;
    }

    public void setProbeDiameter(double probeDiameter) {
        this.probeDiameter = probeDiameter;
    }

    public double getPlateThickness() {
        return plateThickness;
    }

    public void setPlateThickness(double plateThickness) {
        this.plateThickness = plateThickness;
    }

    public double getMaxTravel() {
        return maxTravel;
    }

    public void setMaxTravel(double maxTravel) {
        this.maxTravel = maxTravel;
    }

    public boolean isCompensateSoftLimits() {
        return compensateSoftLimits;
    }

    public void setCompensateSoftLimits(boolean compensateSoftLimits) {
        this.compensateSoftLimits = compensateSoftLimits;
    }
}
