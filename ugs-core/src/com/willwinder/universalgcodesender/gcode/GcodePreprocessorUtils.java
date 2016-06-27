/*
 * Collection of useful command preprocessor methods.
 */

/*
    Copywrite 2013-2016 Will Winder

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
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GcodePreprocessorUtils {

    public static final String EMPTY = "";
    private static final Pattern COMMENT_PAREN = Pattern.compile("\\([^\\(]*\\)");
    private static final Pattern COMMENT_SEMICOLON = Pattern.compile(";.*");
    private static final Pattern COMMENTPARSE = Pattern.compile("(?<=\\()[^\\(\\)]*|(?<=\\;).*|%");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern M30 = Pattern.compile("[Mm]30");
    private static final Pattern GCODE_PATTERN = Pattern.compile("[Gg]0*(\\d+)");

    private static int decimalLength = -1;
    private static Pattern decimalPattern;
    private static DecimalFormat decimalFormatter;

    /**
     * Searches the command string for an 'f' and replaces the speed value 
     * between the 'f' and the next space with a percentage of that speed.
     * In that way all speed values become a ratio of the provided speed 
     * and don't get overridden with just a fixed speed.
     */
    static public String overrideSpeed(String command, double speed) {
        String returnString = command;
        
        // Check if command sets feed speed.
        Pattern pattern = Pattern.compile("F([0-9.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            Double originalFeedRate = Double.parseDouble(matcher.group(1));
            //System.out.println( "Found feed     " + originalFeedRate.toString() );
            Double newFeedRate      = originalFeedRate * speed / 100.0;
            //System.out.println( "Change to feed " + newFeedRate.toString() );
            returnString = matcher.replaceAll( "F" + newFeedRate.toString() );
        }

        return returnString;
    }
    
    /**
     * Removes any comments within parentheses or beginning with a semi-colon.
     */
    static public String removeComment(String command) {
        String newCommand = command;

        // Remove any comments within ( parentheses ) using regex "\([^\(]*\)"
        newCommand = COMMENT_PAREN.matcher(command).replaceAll(EMPTY);
        newCommand = COMMENT_SEMICOLON.matcher(newCommand).replaceAll(EMPTY);

        // Don't send these to the controller.
        if (newCommand.endsWith("%")) {
            newCommand = newCommand.substring(0, newCommand.length()-1);
        }
        
        return newCommand.trim();
    }
    
    /**
     * Searches for a comment in the input string and returns the first match.
     */
    static public String parseComment(String command) {
        String comment = EMPTY;

        // REGEX: Find any comment, includes the comment characters:
        //              "(?<=\()[^\(\)]*|(?<=\;)[^;]*"
        //              "(?<=\\()[^\\(\\)]*|(?<=\\;)[^;]*"
        Matcher matcher = COMMENTPARSE.matcher(command);
        if (matcher.find()){
            comment = matcher.group(0);
        }

        return comment;
    }
    
    static public String truncateDecimals(int length, String command) {
        if (length != decimalLength) {
            //Only build the decimal formatter if the truncation length has changed.
            updateDecimalFormatter(length);

        }
        Matcher matcher = decimalPattern.matcher(command);

        // Build up the truncated command.
        Double d;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            d = Double.parseDouble(matcher.group());
            matcher.appendReplacement(sb, decimalFormatter.format(d));
        }
        matcher.appendTail(sb);
        
        // Return new command.
        return sb.toString();
    }

    private static void updateDecimalFormatter(int length) {
        StringBuilder df = new StringBuilder();

        // Build up the decimal formatter.
        df.append("#");

        if (length != 0) {
            df.append(".");
        }
        for (int i = 0; i < length; i++) {
            df.append('#');
        }

        decimalFormatter = new DecimalFormat(df.toString(), Localization.dfs);

        // Build up the regular expression.
        df = new StringBuilder();
        df.append("\\d+\\.\\d");
        for (int i = 0; i < length; i++) {
            df.append("\\d");
        }
        df.append('+');
        decimalPattern = Pattern.compile(df.toString());
        decimalLength = length;
    }


    static public String removeAllWhitespace(String command) {
        return WHITESPACE.matcher(command).replaceAll(EMPTY);
    }

    static public String removeM30(String command) {
        return M30.matcher(command).replaceAll(EMPTY);
    }
    
    static public List<String> parseCodes(List<String> args, char code) {
        List<String> l = new ArrayList<>();
        char address = Character.toUpperCase(code);
        
        for (String s : args) {
            if (s.length() > 0 && Character.toUpperCase(s.charAt(0)) == address) {
                l.add(s.substring(1));
            }
        }
        
        return l;
    }
    

    static public List<Integer> parseGCodes(String command) {
        Matcher matcher = GCODE_PATTERN.matcher(command);
        List<Integer> codes = new ArrayList<>();
        
        while (matcher.find()) {
            codes.add(Integer.parseInt(matcher.group(1)));
        }
        
        return codes;
    }

    static private Pattern mPattern = Pattern.compile("[Mm]0*(\\d+)");
    static public List<Integer> parseMCodes(String command) {
        Matcher matcher = GCODE_PATTERN.matcher(command);
        List<Integer> codes = new ArrayList<>();
        
        while (matcher.find()) {
            codes.add(Integer.parseInt(matcher.group(1)));
        }
        
        return codes;
    }

    /**
     * Update a point given the arguments of a command.
     */
    static public Point3d updatePointWithCommand(String command, Point3d initial, boolean absoluteMode) {
        List<String> l = GcodePreprocessorUtils.splitCommand(command);
        return updatePointWithCommand(l, initial, absoluteMode);
    }
    
    /**
     * Update a point given the arguments of a command, using a pre-parsed list.
     */
    static public Point3d updatePointWithCommand(List<String> commandArgs, Point3d initial, boolean absoluteMode) {

        double x = parseCoord(commandArgs, 'X');
        double y = parseCoord(commandArgs, 'Y');
        double z = parseCoord(commandArgs, 'Z');

        return updatePointWithCommand(initial, x, y, z, absoluteMode);
    }

    /**
     * Update a point given the new coordinates.
     */
    static public Point3d updatePointWithCommand(Point3d initial, double x, double y, double z, boolean absoluteMode) {

        Point3d newPoint = new Point3d(initial.x, initial.y, initial.z);

        if (absoluteMode) {
            if (!Double.isNaN(x)) {
                newPoint.x = x;
            }
            if (!Double.isNaN(y)) {
                newPoint.y = y;
            }
            if (!Double.isNaN(z)) {
                newPoint.z = z;
            }
        } else {
            if (!Double.isNaN(x)) {
                newPoint.x += x;
            }
            if (!Double.isNaN(y)) {
                newPoint.y += y;
            }
            if (!Double.isNaN(z)) {
                newPoint.z += z;
            }
        }

        return newPoint;
    }
    
    static public Point3d updateCenterWithCommand(
            List<String> commandArgs,
            Point3d initial,
            Point3d nextPoint,
            boolean absoluteIJKMode,
            boolean clockwise,
            PlaneFormatter plane) {
        double i      = parseCoord(commandArgs, 'I');
        double j      = parseCoord(commandArgs, 'J');
        double k      = parseCoord(commandArgs, 'K');
        double radius = parseCoord(commandArgs, 'R');
        
        if (Double.isNaN(i) && Double.isNaN(j) && Double.isNaN(k)) {
            return GcodePreprocessorUtils.convertRToCenter(
                            initial, nextPoint, radius, absoluteIJKMode,
                            clockwise, plane);
        }

        return updatePointWithCommand(initial, i, j, k, absoluteIJKMode);

    }
        
    static public String generateG1FromPoints(final Point3d start, final Point3d end, final boolean absoluteMode, DecimalFormat formatter) {
        DecimalFormat df = formatter;
        if (df == null) {
            df = new DecimalFormat("#.####");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("G1");

        if (absoluteMode) {
            if (!Double.isNaN(end.x)) {
                sb.append("X");
                sb.append(df.format(end.x));
            }
            if (!Double.isNaN(end.y)) {
                sb.append("Y");
                sb.append(df.format(end.y));
            }
            if (!Double.isNaN(end.z)) {
                sb.append("Z");
                sb.append(df.format(end.z));
            }
        } else { // calculate offsets.
            if (!Double.isNaN(end.x)) {
                sb.append("X");
                sb.append(df.format(end.x-start.x));
            }
            if (!Double.isNaN(end.y)) {
                sb.append("Y");
                sb.append(df.format(end.y-start.x));
            }
            if (!Double.isNaN(end.z)) {
                sb.append("Z");
                sb.append(df.format(end.z-start.x));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Splits a gcode command by each word/argument, doesn't care about spaces.
     * This command is about the same speed as the string.split(" ") command,
     * but might be a little faster using precompiled regex.
     */
    static public List<String> splitCommand(String command) {
        List<String> l = new ArrayList<>();
        boolean readNumeric = false;
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < command.length(); i++){
            char c = command.charAt(i);
                        
            // If the last character was numeric (readNumeric is true) and this
            // character is a letter or whitespace, then we hit a boundary.
            if (readNumeric && !Character.isDigit(c) && c != '.') {
                readNumeric = false; // reset flag.
                
                l.add(sb.toString());
                sb = new StringBuilder();
                
                if (Character.isLetter(c)) {
                    sb.append(c);
                }
            }

            else if (Character.isDigit(c) || c == '.' || c == '-') {
                sb.append(c);
                readNumeric = true;
            }
            
            else if (Character.isLetter(c)) {
                sb.append(c);
            }
        }
        
        // Add final one
        if (sb.length() > 0) {
            l.add(sb.toString());
        }
        
        return l;
    }
    
    // TODO: Replace everything that uses this with a loop that loops through
    //       the string and creates a hash with all the values.
    static public double parseCoord(List<String> argList, char c)
    {
        char address = Character.toUpperCase(c);
        for(String t : argList)
        {
            if (t.length() > 1 && Character.toUpperCase(t.charAt(0)) == address)
            {
                try {
                    return Double.parseDouble(t.substring(1));
                } catch (NumberFormatException e) {
                    return Double.NaN;
                }
            }
        }
        return Double.NaN;
    }
    
    static public List<String> convertArcsToLines(Point3d start, Point3d end) {
        List<String> l = new ArrayList<>();
        
        return l;
    }
    
    /**
     * Generates the points along an arc including the start and end points.
     */
    static public List<Point3d> generatePointsAlongArcBDring(
            final Point3d start,
            final Point3d end,
            final Point3d center,
            boolean clockwise,
            double R,
            double minArcLength,
            double arcSegmentLength,
            PlaneFormatter plane) {
        double radius = R;

        // Calculate radius if necessary.
        if (radius == 0) {
            radius = Math.sqrt(Math.pow(plane.axis0(start) - plane.axis0(center),2.0) + Math.pow(plane.axis1(end) - plane.axis1(center), 2.0));
        }

        double startAngle = GcodePreprocessorUtils.getAngle(center, start, plane);
        double endAngle = GcodePreprocessorUtils.getAngle(center, end, plane);
        double sweep = GcodePreprocessorUtils.calculateSweep(startAngle, endAngle, clockwise);

        // Convert units.
        double arcLength = sweep * radius;

        // If this arc doesn't meet the minimum threshold, don't expand.
        if (minArcLength > 0 && arcLength < minArcLength) {
            return null;
        }

        int numPoints = 20;

        if (arcSegmentLength <= 0 && minArcLength > 0) {
            arcSegmentLength = (sweep * radius) / minArcLength;
        }

        if (arcSegmentLength > 0) {
            numPoints = (int)Math.ceil(arcLength/arcSegmentLength);
        }

        return GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, clockwise, radius, startAngle, sweep, numPoints, plane);
    }

    /**
     * Generates the points along an arc including the start and end points.
     */
    static private List<Point3d> generatePointsAlongArcBDring(
            final Point3d p1,
            final Point3d p2,
            final Point3d center,
            boolean isCw,
            double radius, 
            double startAngle,
            double sweep,
            int numPoints,
            PlaneFormatter plane) {

        Point3d lineStart = new Point3d(p1.x, p1.y, p1.z);
        List<Point3d> segments = new ArrayList<>();
        double angle;

        // Calculate radius if necessary.
        if (radius == 0) {
            radius = Math.sqrt(Math.pow(plane.axis0(p1) - plane.axis1(center), 2.0) + Math.pow(plane.axis1(p1) - plane.axis1(center), 2.0));
        }

        double zIncrement = (plane.linear(p2) - plane.linear(p1)) / numPoints;
        for(int i=0; i<numPoints; i++)
        {
            if (isCw) {
                angle = (startAngle - i * sweep/numPoints);
            } else {
                angle = (startAngle + i * sweep/numPoints);
            }

            if (angle >= Math.PI * 2) {
                angle = angle - Math.PI * 2;
            }

            //lineStart.x = Math.cos(angle) * radius + center.x;
            plane.setAxis0(lineStart, Math.cos(angle) * radius + plane.axis0(center));
            //lineStart.y = Math.sin(angle) * radius + center.y;
            plane.setAxis1(lineStart, Math.sin(angle) * radius + plane.axis1(center));
            //lineStart.z += zIncrement;
            plane.setLinear(lineStart, plane.linear(lineStart) + zIncrement);
            
            segments.add(new Point3d(lineStart));
        }
        
        segments.add(new Point3d(p2));

        return segments;
    }

    /**
     * Helper method for to convert IJK syntax to center point.
     * @return the center of rotation between two points with IJK codes.
     */
    static private Point3d convertRToCenter(
            Point3d start,
            Point3d end,
            double radius,
            boolean absoluteIJK,
            boolean clockwise,
            PlaneFormatter plane) {
        double R = radius;
        Point3d center = new Point3d();
        
        // This math is copied from GRBL in gcode.c
        double x = plane.axis0(end) - plane.axis0(start);
        double y = plane.axis1(end) - plane.axis1(start);

        double h_x2_div_d = 4 * R*R - x*x - y*y;
        if (h_x2_div_d < 0) { System.out.println("Error computing arc radius."); }
        h_x2_div_d = (-Math.sqrt(h_x2_div_d)) / Math.hypot(x, y);

        if (clockwise == false) {
            h_x2_div_d = -h_x2_div_d;
        }

        // Special message from gcoder to software for which radius
        // should be used.
        if (R < 0) {
            h_x2_div_d = -h_x2_div_d;
            // TODO: Places that use this need to run ABS on radius.
            radius = -radius;
        }

        double offsetX = 0.5*(x-(y*h_x2_div_d));
        double offsetY = 0.5*(y+(x*h_x2_div_d));

        if (!absoluteIJK) {
            plane.setAxis0(center, plane.axis0(start) + offsetX);
            plane.setAxis1(center, plane.axis1(start) + offsetY);
        } else {
            plane.setAxis0(center, offsetX);
            plane.setAxis1(center, offsetY);
        }

        return center;
    }

    /** 
     * Helper method for arc calculation
     * @return angle in radians of a line going from start to end.
     */
    static private double getAngle(final Point3d start, final Point3d end, PlaneFormatter plane) {
        double deltaX = plane.axis0(end) - plane.axis0(start);
        double deltaY = plane.axis1(end) - plane.axis1(start);

        double angle = 0.0;

        if (deltaX != 0) { // prevent div by 0
            // it helps to know what quadrant you are in
            if (deltaX > 0 && deltaY >= 0) {  // 0 - 90
                angle = Math.atan(deltaY/deltaX);
            } else if (deltaX < 0 && deltaY >= 0) { // 90 to 180
                angle = Math.PI - Math.abs(Math.atan(deltaY/deltaX));
            } else if (deltaX < 0 && deltaY < 0) { // 180 - 270
                angle = Math.PI + Math.abs(Math.atan(deltaY/deltaX));
            } else if (deltaX > 0 && deltaY < 0) { // 270 - 360
                angle = Math.PI * 2 - Math.abs(Math.atan(deltaY/deltaX));
            }
        }
        else {
            // 90 deg
            if (deltaY > 0) {
                angle = Math.PI / 2.0;
            }
            // 270 deg
            else {
                angle = Math.PI * 3.0 / 2.0;
            }
        }
      
        return angle;
    }

    /**
     * Helper method for arc calculation to calculate sweep from two angles.
     * @returns sweep in radians.
     */
    static private double calculateSweep(double startAngle, double endAngle, boolean isCw) {
        double sweep;

        // Full circle
        if (startAngle == endAngle) {
            sweep = (Math.PI * 2);
            // Arcs
        } else {
            // Account for full circles and end angles of 0/360
            if (endAngle == 0) {
                endAngle = Math.PI * 2;
            }
            // Calculate distance along arc.
            if (!isCw && endAngle < startAngle) {
                sweep = ((Math.PI * 2 - startAngle) + endAngle);
            } else if (isCw && endAngle > startAngle) {
                sweep = ((Math.PI * 2 - endAngle) + startAngle);
            } else {
                sweep = Math.abs(endAngle - startAngle);
            }
        }

        return sweep;
    }

}
