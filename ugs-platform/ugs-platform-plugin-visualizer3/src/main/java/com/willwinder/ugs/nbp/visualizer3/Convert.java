package com.willwinder.ugs.nbp.visualizer3;

import java.awt.Color;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.willwinder.universalgcodesender.model.Position;

/**
 * Helpers to convert from UGS types to JME types.
 */
public class Convert {

    public static ColorRGBA ToColorRGBA(Color color) {
        return new ColorRGBA(
                ((float) color.getRed()) / 255.0f,
                ((float) color.getGreen()) / 255.0f,
                ((float) color.getBlue()) / 255.0f,
                ((float) color.getAlpha()) / 255.0f);
    }

    public static Vector3f ToVector3f(Position position) {
        return new Vector3f(
                (float) position.getX(),
                (float) position.getY(),
                (float) position.getZ());
    }
}
