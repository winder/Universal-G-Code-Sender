/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.vecmath.Point3d;

/**
 *
 * @author Owen
 */
public class VisualizerUtils {
    
    enum Color {
        RED, 
        BLUE, 
        PURPLE, 
        YELLOW, 
        OTHER_YELLOW, 
        GREEN, 
        WHITE,
        GRAY,
    }
    
    /**
     * Returns the maximum side dimension of a box containing two points.
     */
    static double findMaxSide(Point3d min, Point3d max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        double z = Math.abs(min.z) + Math.abs(max.z);
        return Math.max(x, Math.max(y, z));
    }

    /**
     * Returns the aspect ratio from two points.
     */
    static double findAspectRatio(Point3d min, Point3d max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        return x / y;
    }

    /**
     * Returns the center point on a line.
     */
    static Point3d findCenter(Point3d min, Point3d max) {
        Point3d center = new Point3d();
        center.x = (min.x + max.x) / 2.0;
        center.y = (min.y + max.y) / 2.0;
        center.z = (min.z + max.z) / 2.0;
        return center;
    }

    /**
     * Find a factor to scale an object by so that it fits in the window.
     */
    static double findScaleFactor(double x, double y, Point3d min, Point3d max) {
        if (y == 0 || x == 0 || min == null || max == null) {
            return 1;
        }
        double xObj = Math.abs(min.x) + Math.abs(max.x);
        double yObj = Math.abs(min.y) + Math.abs(max.y);
        double windowRatio = x / y;
        double objRatio = xObj / yObj;
        if (windowRatio < objRatio) {
            return (1.0 / xObj) * windowRatio;
        } else {
            return 1.0 / yObj;
        }
    }

    /** Constructor to setup the GUI for this Component */
    public static ArrayList<String> readFiletoArrayList(String gCode) throws IOException {
        ArrayList<String> vect = null;
        File gCodeFile = new File(gCode);
        vect = new ArrayList<String>();
        FileInputStream fstream = new FileInputStream(gCodeFile);
        DataInputStream dis = new DataInputStream(fstream);
        BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));
        String line;
        while ((line = fileStream.readLine()) != null) {
            vect.add(line);
        }

        return vect;
    }

    static byte[] getVertexColor(Color color) {
        byte[] ret;
        switch (color) {
            case RED:
                ret = new byte[]{(byte) 255, (byte) 100, (byte) 100};
                break;
            case BLUE:
                ret = new byte[]{(byte) 0, (byte) 255, (byte) 255};
                break;
            case PURPLE:
                ret = new byte[]{(byte) 242, (byte) 0, (byte) 255};
                break;
            case YELLOW:
                ret = new byte[]{(byte) 237, (byte) 255, (byte) 0};
                break;
            case OTHER_YELLOW:
                ret = new byte[]{(byte) 234, (byte) 212, (byte) 7};
                break;
            case GREEN:
                ret = new byte[]{(byte) 33, (byte) 255, (byte) 0};
                break;
            case WHITE:
                ret = new byte[]{(byte) 255, (byte) 255, (byte) 255};
                break;
            case GRAY:
                ret = new byte[]{(byte) 80, (byte) 80, (byte) 80};
                break;
            default:
                ret = new byte[]{(byte) 255, (byte) 255, (byte) 255};
        }
        return ret;
    }
    
}
