/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.io.gerber;

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {
    private Aperture currentAperture;
    private UnitUtils.Units units;
    private int intDigits;
    private int decDigits;
    private Interpolation interpolation;
    private Map<String, Aperture> apertures;
    private double x;
    private double y;
    private boolean inRegion;
    private List<Point2D.Double> regionPoints;
    private String currentNet;
    private List<GerberEntity> shapes;
    private Polarity polarity;
    private final Map<String, Macro> macros = new HashMap<>();
    private List<Path2D> centerLines;

    public Map<String, Macro> getMacros() {
        return macros;
    }

    public State() {
        reset();
    }

    public void reset() {
        currentAperture = null;
        units = UnitUtils.Units.MM;
        intDigits = 2;
        decDigits = 4;
        interpolation = Interpolation.LINEAR;
        apertures = new HashMap<>();
        x = 0;
        y = 0;
        inRegion = false;
        regionPoints = new ArrayList<>();
        shapes = new ArrayList<>();
        polarity = Polarity.DARK;
        centerLines = new ArrayList<>();
    }

    public Aperture getCurrentAperture() {
        return currentAperture;
    }

    public void setCurrentAperture(Aperture currentAperture) {
        this.currentAperture = currentAperture;
    }

    public UnitUtils.Units getUnits() {
        return units;
    }

    public void setUnits(UnitUtils.Units units) {
        this.units = units;
    }

    public int getIntDigits() {
        return intDigits;
    }

    public void setIntDigits(int intDigits) {
        this.intDigits = intDigits;
    }

    public int getDecDigits() {
        return decDigits;
    }

    public void setDecDigits(int decDigits) {
        this.decDigits = decDigits;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public Map<String, Aperture> getApertures() {
        return apertures;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isInRegion() {
        return inRegion;
    }

    public void setInRegion(boolean inRegion) {
        this.inRegion = inRegion;
    }

    public List<Point2D.Double> getRegionPoints() {
        return regionPoints;
    }

    public void setCurrentNet(String currentNet) {
        this.currentNet = currentNet;
    }

    public Polarity getPolarity() {
        return polarity;
    }

    public void setPolarity(Polarity polarity) {
        this.polarity = polarity;
    }

    public List<GerberEntity> getShapes() {
        return shapes;
    }

    public List<Path2D> getCenterLines() {
        return centerLines;
    }

    public String getCurrentNet() {
        return currentNet;
    }
}
