/*
 * Simple coordinate class.
 */

/*
    Copywrite 2012 Will Winder

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

package com.willwinder.universalgcodesender;

/**
 *
 * @author wwinder
 */
public class Coordinate {
    private Double x,y,z;
    
    Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    void setCoordinates(Coordinate coord) {
        this.x = coord.getX();
        this.y = coord.getY();
        this.z = coord.getZ();
    }
    
    void setX(double x) {
        this.x = x;
    }
    
    void setY(double y) {
        this.y = y;
    }
    
    void setZ(double z) {
        this.z = z;
    }
    
    Double getX() {
        return this.x;
    }
    
    Double getY() {
        return this.y;
    }
    
    Double getZ() {
        return this.z;
    }
    
    @Override
    public String toString() {
        return "("+x+", "+y+", "+z+")";
    }
}
