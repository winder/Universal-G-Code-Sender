/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
