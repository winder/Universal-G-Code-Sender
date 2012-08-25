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
    private Integer x,y,z;
    
    Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    void setX(int x) {
        this.x = x;
    }
    
    void setY(int y) {
        this.y = y;
    }
    
    void setZ(int z) {
        this.z = z;
    }
    
    Integer getX() {
        return this.x;
    }
    
    Integer getY() {
        return this.y;
    }
    
    Integer getZ() {
        return this.z;
    }
    
    @Override
    public String toString() {
        return "("+x+", "+y+", "+z+")";
    }
}
