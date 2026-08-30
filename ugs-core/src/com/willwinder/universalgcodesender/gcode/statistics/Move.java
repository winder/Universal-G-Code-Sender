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
package com.willwinder.universalgcodesender.gcode.statistics;

/**
 * A movement waiting to be timed. It can not be timed until the following movement is known, as
 * that decides how much the machine has to brake before the corner.
 *
 * @author Joacim Breiler
 */
class Move {
    private final double length;
    private final double targetVelocity;
    private final double[] direction;
    private final boolean isRapid;
    private double entryVelocity;

    /**
     * Creates a movement.
     *
     * @param length         the length of the movement in millimeters
     * @param targetVelocity the velocity the movement would be made at in millimeters per second
     *                       if the machine had the room to reach it
     * @param direction      a unit vector with the direction of the movement, or null if it has no
     *                       direction such as a rotation
     * @param isRapid        if this is a rapid movement
     */
    Move(double length, double targetVelocity, double[] direction, boolean isRapid) {
        this.length = length;
        this.targetVelocity = targetVelocity;
        this.direction = direction;
        this.isRapid = isRapid;
    }

    double getLength() {
        return length;
    }

    double getTargetVelocity() {
        return targetVelocity;
    }

    double[] getDirection() {
        return direction;
    }

    boolean isRapid() {
        return isRapid;
    }

    double getEntryVelocity() {
        return entryVelocity;
    }

    void setEntryVelocity(double entryVelocity) {
        this.entryVelocity = entryVelocity;
    }
}
