/*
    Copyright 2013-2023 Will Winder

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

package com.willwinder.universalgcodesender.visualizer;

import com.willwinder.universalgcodesender.model.Position;

/**
 * Segment of gcode by the start/end positions.
 */
public class LineSegment {

    private final Position first;
    private final Position second;
    private final int lineNumber;
    // Line properties
    private boolean isZMovement = false;
    private boolean isArc = false;
    private boolean isFastTraverse = false;
    private boolean isRotation = false;
    private double feedRate;
    private double spindleSpeed;

    public LineSegment(final Position a, final Position b, int num) {
        first = new Position(a);
        second = new Position(b);
        lineNumber = num;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Position getStart() {
        return this.first;
    }

    public Position getEnd() {
        return this.second;
    }

    public double getFeedRate() {
        return feedRate;
    }

    public void setFeedRate(double s) {
        this.feedRate = s;
    }

    public void setIsZMovement(boolean isZ) {
        this.isZMovement = isZ;
    }

    public boolean isZMovement() {
        return isZMovement;
    }

    public void setIsArc(boolean isA) {
        this.isArc = isA;
    }

    public boolean isArc() {
        return isArc;
    }

    public void setIsFastTraverse(boolean isF) {
        this.isFastTraverse = isF;
    }

    public boolean isFastTraverse() {
        return this.isFastTraverse;
    }

    public void setIsRotation(boolean isR) {
        this.isRotation = isR;
    }

    public boolean isRotation() {
        return this.isRotation;
    }

    public double getSpindleSpeed() {
        return spindleSpeed;
    }

    public void setSpindleSpeed(double spindleSpeed) {
        this.spindleSpeed = spindleSpeed;
    }
}
