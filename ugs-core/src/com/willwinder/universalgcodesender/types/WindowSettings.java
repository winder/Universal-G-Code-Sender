/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.willwinder.universalgcodesender.types;

/**
 *
 * @author wwinder
 */
public class WindowSettings {
    public int xLocation;
    public int yLocation;
    public int width;
    public int height;
    
    public WindowSettings(int x, int y, int w, int h) {
        xLocation = x;
        yLocation = y;
        width = w;
        height = h;
    }
}
