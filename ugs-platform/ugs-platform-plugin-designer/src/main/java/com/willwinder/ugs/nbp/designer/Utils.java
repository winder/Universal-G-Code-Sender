/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer;

import org.apache.commons.lang3.StringUtils;

import java.awt.geom.Point2D;
import java.text.ParseException;

/**
 * @author Joacim Breiler
 */
public class Utils {
    public static double normalizeRotation(double degrees) {
        if (degrees >= 360) {
            return degrees % 360;
        } else if (degrees < 0) {
            return degrees % 360 + 360;
        }

        return Math.abs(degrees);
    }

    public static double parseDouble(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0;
        }

        try {
            return com.willwinder.universalgcodesender.Utils.formatter.parse(value).doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }


    public static String toString(double value) {
        return com.willwinder.universalgcodesender.Utils.formatter.format(value);
    }

    /**
     * Calculates the angle from centerPt to targetPt in degrees.
     * The return should range from [0,360), rotating CLOCKWISE,
     * 0 and 360 degrees represents NORTH,
     * 90 degrees represents EAST, etc...
     * <p>
     * Assumes all points are in the same coordinate space.  If they are not,
     * you will need to call SwingUtilities.convertPointToScreen or equivalent
     * on all arguments before passing them  to this function.
     * <p>
     * Source: https://stackoverflow.com/a/16340752
     *
     * @param centerPt Point we are rotating around.
     * @param targetPt Point we want to calculate the angle to.
     * @return angle in degrees.  This is the angle from centerPt to targetPt.
     */
    public static double calcRotationAngleInDegrees(Point2D centerPt, Point2D targetPt) {
        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.
        // NOTE: By preserving Y and X param order to atan2,  we are expecting
        // a CLOCKWISE angle direction.
        double theta = Math.atan2(targetPt.getY() - centerPt.getY(), targetPt.getX() - centerPt.getX());

        // rotate the theta angle clockwise by 90 degrees
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.
        // subtracting would rotate it counter-clockwise
        theta += Math.PI / 2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if (angle < 0) {
            angle += 360;
        }

        if (angle > 360) {
            angle -= 360;
        }

        return angle;
    }

    public static double roundToDecimals(double value, int decimals) {
        double power = Math.pow(10, decimals);
        if (power == 0) {
            power = 1;
        }
        return Math.round(value * power) / power;
    }
}
