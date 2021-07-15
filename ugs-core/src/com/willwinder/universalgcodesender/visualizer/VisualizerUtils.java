/*
 * Helper functions for visualizer routines.
 */
/*
    Copyright 2013 Will Winder

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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author wwinder
 */
public class VisualizerUtils {
    
    public enum Color {
        RED(255,100,100), 
        BLUE(0,255,255), 
        PURPLE(242,0,255), 
        YELLOW(237,255,0), 
        OTHER_YELLOW(234,212,7), 
        GREEN(33,255,0), 
        WHITE(255,255,255),
        GRAY(80,80,80),
        BLACK(0,0,0);

        final byte[] rgb;

        Color(int r, int g, int b) {
            rgb = new byte[]{(byte)r,(byte)g,(byte)b};
        }

        public byte[] getBytes() {
            return rgb;
        }
    }
    
    /**
     * Returns the maximum side dimension of a box containing two points.
     */
    public static double findMaxSide(Position min, Position max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        double z = Math.abs(min.z) + Math.abs(max.z);
        return Math.max(x, Math.max(y, z));
    }

    /**
     * Returns the aspect ratio from two points.
     */
    public static double findAspectRatio(Position min, Position max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        return x / y;
    }

    /**
     * Returns the center point on a line.
     */
    public static Position findCenter(Position min, Position max) {
        Position center = new Position(
            (min.x + max.x) / 2.0,
            (min.y + max.y) / 2.0,
            (min.z + max.z) / 2.0);
        return center;
    }

    /**
     * Find a factor to scale an object by so that it fits in the window.
     * The buffer factor is how much of a border to leave.
     */
    public static double findScaleFactor(double x, double y, Position min, Position max, double bufferFactor) {
        
        if (y == 0 || x == 0 || min == null || max == null) {
            return 1;
        }
        double xObj = Math.abs(min.x) + Math.abs(max.x);
        double yObj = Math.abs(min.y) + Math.abs(max.y);
        double windowRatio = x / y;
        double objRatio = xObj / yObj;
        if (windowRatio < objRatio) {
            return (1.0 / xObj) * windowRatio * bufferFactor;
        } else {
            return (1.0 / yObj) * bufferFactor;
        }
    }

    /** Constructor to setup the GUI for this Component */
    public static ArrayList<String> readFiletoArrayList(String gCode) throws IOException {
        ArrayList<String> vect = new ArrayList<>();
        File gCodeFile = new File(gCode);
        try(FileInputStream fstream = new FileInputStream(gCodeFile)) {
            DataInputStream dis = new DataInputStream(fstream);
            BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));
            String line;
            while ((line = fileStream.readLine()) != null) {
                vect.add(line);
            }
        }

        return vect;
    }

    /**
     * Determine the ratio of mouse movement to model movement for panning operations on a single axis.
     * @param objectMin The lowest value on the axis from the model's size.
     * @param objectMax The highest point on the axis from the model's size.
     * @param movementRange The length of the axis in the window displaying the model.
     * @return the ratio of the model size to the display size on that axis.
     */
    public static double getRelativeMovementMultiplier(double objectMin, double objectMax, int movementRange) {
        if (movementRange == 0)
            return 0;

        double objectAxis = Math.abs(objectMax - objectMin);

        return objectAxis / (double)movementRange;
    }
    
}
