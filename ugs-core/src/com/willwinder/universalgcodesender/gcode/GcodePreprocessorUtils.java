/*
    Copyright 2013-2020 Will Winder

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

import com.google.common.base.Preconditions;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.willwinder.universalgcodesender.gcode.util.Code.*;
import static com.willwinder.universalgcodesender.gcode.util.Code.ModalGroup.Motion;

/**
 * Collection of useful command preprocessor methods.
 *
 * @author wwinder
 */
public class GcodePreprocessorUtils {

    public static final Pattern COMMENT = Pattern.compile("\\(.*\\)|\\s*;.*|%.*$");
    private static final String EMPTY = "";
    private static final Pattern COMMENTPARSE = Pattern.compile("(?<=\\()[^()]*|(?<=;).*|%");

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
        if (matcher.find()) {
            double originalFeedRate = Double.parseDouble(matcher.group(1));
            //System.out.println( "Found feed     " + originalFeedRate.toString() );
            double newFeedRate = originalFeedRate * speed / 100.0;
            //System.out.println( "Change to feed " + newFeedRate.toString() );
            returnString = matcher.replaceAll("F" + newFeedRate);
        }

        return returnString;
    }
    
    /**
     * Removes any comments within parentheses or beginning with a semi-colon.
     */
    static public String removeComment(String command) {
        return COMMENT.matcher(command).replaceAll(EMPTY);
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
        double d;
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

    /**
     * Update a point given the arguments of a command, using a pre-parsed list.
     */
    static public Position updatePointWithCommand(List<String> commandArgs, Position initial, boolean absoluteMode) {

        double x = parseCoord(commandArgs, 'X');
        double y = parseCoord(commandArgs, 'Y');
        double z = parseCoord(commandArgs, 'Z');
        double a = parseCoord(commandArgs, 'A');
        double b = parseCoord(commandArgs, 'B');
        double c = parseCoord(commandArgs, 'C');

        if (Double.isNaN(x) && Double.isNaN(y) && Double.isNaN(z) &&
            Double.isNaN(a) && Double.isNaN(b) && Double.isNaN(c)) {
            return null;
        }

        return updatePointWithCommand(initial, x, y, z, a, b, c, absoluteMode);
    }

    /**
     * Update a point given the new coordinates.
     */
    static public Position updatePointWithCommand(Position initial, double x, double y, double z, double a, double b, double c, boolean absoluteMode) {

        Position newPoint = new Position(initial);

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
            if (!Double.isNaN(a)) {
                newPoint.a = a;
            }
            if (!Double.isNaN(b)) {
                newPoint.b = b;
            }
            if (!Double.isNaN(c)) {
                newPoint.c = c;
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
            if (!Double.isNaN(a)) {
                newPoint.a += a;
            }
            if (!Double.isNaN(b)) {
                newPoint.b += b;
            }
            if (!Double.isNaN(c)) {
                newPoint.c += c;
            }
        }

        return newPoint;
    }
    
    static public Position updateCenterWithCommand(
            List<String> commandArgs,
            Position initial,
            Position nextPoint,
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

        return updatePointWithCommand(initial, i, j, k, 0, 0, 0, absoluteIJKMode);

    }

    static public String generateLineFromPoints(final Code command, final Position start, final Position end, final boolean absoluteMode, DecimalFormat formatter) {
        DecimalFormat df = formatter;
        if (df == null) {
            df = new DecimalFormat("0.####", Localization.dfs);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(command);

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
        // Special handling for GRBL system commands which will not be splitted
        if(command.startsWith("$")) {
            return Collections.singletonList(command);
        }

        List<String> l = new ArrayList<>();
        boolean readNumeric = false;
        boolean readLineComment = false;
        boolean readBlockComment = false;
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < command.length(); i++){
            char c = command.charAt(i);

            if (c == '(' && !readLineComment && !readBlockComment) {
                if( sb.length() > 0 ){
                    l.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(c);
                readBlockComment = true;
                continue;
            } else if (readBlockComment && c == ')') {
                readBlockComment = false;
                sb.append(c);
                l.add(sb.toString());
                sb = new StringBuilder();
                continue;
            } else if (c == ';' && !readLineComment && !readBlockComment) {
                if( sb.length() > 0 ){
                    l.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(c);
                readLineComment = true;
                continue;
            }


            if (readLineComment || readBlockComment) {
                sb.append(c);
            } else if (Character.isWhitespace(c)) {
                continue;
            }
            // If the last character was numeric (readNumeric is true) and this
            // character is a letter or whitespace, then we hit a boundary.
            else if (readNumeric && !Character.isDigit(c) && c != '.') {
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
    static public boolean hasAxisWords(List<String> argList) {
        for(String t : argList) {
            if (t.length() > 1) {
                char c = Character.toUpperCase(t.charAt(0));
                if (c == 'X' || c == 'Y' || c == 'Z' || c == 'A' || c == 'B' || c == 'C') {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO: Replace everything that uses this with a loop that loops through
    //       the string and creates a hash with all the values.
    /**
     * Pulls out a word, like "F100", "S1300", "T0", "X-0.5"
     */
    static public String extractWord(List<String> argList, char c) {
        char address = Character.toUpperCase(c);
        for(String t : argList)
        {
            if (Character.toUpperCase(t.charAt(0)) == address)
            {
                return t;
            }
        }
        return null;
    }

    // TODO: Replace everything that uses this with a loop that loops through
    //       the string and creates a hash with all the values.
    static public double parseCoord(List<String> argList, char c)
    {
        String word = extractWord(argList, c);
        if (word != null && word.length() > 1) {
            try {
                return Double.parseDouble(word.substring(1));
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }
    
    /**
     * Generates the points along an arc including the start and end points.
     * 
     * @param start start position XYZ and rotations
     * @param end end position XYZ and rotations
     * @param center center of rotation
     * @param clockwise flag indicating clockwise or counter-clockwise
     * @param radius radius of the arc
     * @param minArcLength minimum length before expansions are made.
     * @param arcSegmentLength length of segments in resulting Positions.
     * @param plane helper to select values for arcs across different planes
     */
    static public List<Position> generatePointsAlongArcBDring(
            final Position start,
            final Position end,
            final Position center,
            boolean clockwise,
            double radius,
            double minArcLength,
            double arcSegmentLength,
            PlaneFormatter plane) {
        double r = radius;

        // Calculate radius if necessary.
        if (r == 0) {
            r = Math.sqrt(Math.pow(plane.axis0(start) - plane.axis0(center),2.0) + Math.pow(plane.axis1(end) - plane.axis1(center), 2.0));
        }

        double startAngle = GcodePreprocessorUtils.getAngle(center, start, plane);
        double endAngle = GcodePreprocessorUtils.getAngle(center, end, plane);
        double sweep = GcodePreprocessorUtils.calculateSweep(startAngle, endAngle, clockwise);

        // Convert units.
        double arcLength = sweep * r;

        // If this arc doesn't meet the minimum threshold, don't expand.
        if (minArcLength > 0 && arcLength < minArcLength) {
            return null;
        }

        int numPoints = 20;

        if (arcSegmentLength <= 0 && minArcLength > 0) {
            arcSegmentLength = (sweep * r) / minArcLength;
        }

        if (arcSegmentLength > 0) {
            numPoints = (int)Math.ceil(arcLength/arcSegmentLength);
        }

        return GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, clockwise, r, startAngle, sweep, numPoints, plane);
    }

    /**
     * Generates the points along an arc including the start and end points.
     * @param p1 start position XYZ and rotations
     * @param p2 end position XYZ and rotations
     * @param center center of rotation
     * @param isCw flag indicating clockwise or counter-clockwise
     * @param radius radius of the arc
     * @param startAngle beginning angle of arc
     * @param sweep sweep length in radians
     * @param numPoints number of points to generate
     * @param plane helper to select values for arcs across different planes
     */
    static private List<Position> generatePointsAlongArcBDring(
            final Position p1,
            final Position p2,
            final Position center,
            boolean isCw,
            double radius, 
            double startAngle,
            double sweep,
            int numPoints,
            PlaneFormatter plane) {

        Preconditions.checkArgument(numPoints > 0, "Arcs must have at least 1 segment.");

        Position nextPoint = new Position(p1);
        List<Position> segments = new ArrayList<>();
        double angle;

        // Calculate radius if necessary.
        if (radius == 0) {
            radius = Math.sqrt(Math.pow(plane.axis0(p1) - plane.axis1(center), 2.0) + Math.pow(plane.axis1(p1) - plane.axis1(center), 2.0));
        }

        double linearIncrement = (plane.linear(p2) - plane.linear(p1)) / numPoints;
        double linearPos = plane.linear(nextPoint);
        double aIncrement = (p2.a - p1.a) / numPoints;
        double bIncrement = (p2.b - p1.b) / numPoints;
        double cIncrement = (p2.c - p1.c) / numPoints;

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
            plane.setAxis0(nextPoint, Math.cos(angle) * radius + plane.axis0(center));
            //lineStart.y = Math.sin(angle) * radius + center.y;
            plane.setAxis1(nextPoint, Math.sin(angle) * radius + plane.axis1(center));

            // Increment (optional) linear motions.
            plane.setLinear(nextPoint, linearPos);
            linearPos += linearIncrement;
            nextPoint.a += aIncrement;
            nextPoint.b += bIncrement;
            nextPoint.c += cIncrement;

            segments.add(new Position(nextPoint));
        }
        
        segments.add(new Position(p2));

        return segments;
    }

    /**
     * Helper method for to convert IJK syntax to center point.
     *
     * @return the center of rotation between two points with IJK codes.
     */
    static private Position convertRToCenter(
            Position start,
            Position end,
            double radius,
            boolean absoluteIJK,
            boolean clockwise,
            PlaneFormatter plane) {
        Position center = new Position(start.getUnits());
        
        // This math is copied from GRBL in gcode.c
        double x = plane.axis0(end) - plane.axis0(start);
        double y = plane.axis1(end) - plane.axis1(start);

        double h_x2_div_d = 4 * radius * radius - x * x - y * y;
        //if (h_x2_div_d < 0) { System.out.println("Error computing arc radius."); }
        h_x2_div_d = (-Math.sqrt(h_x2_div_d)) / Math.hypot(x, y);

        if (!clockwise) {
            h_x2_div_d = -h_x2_div_d;
        }

        // Special message from gcoder to software for which radius
        // should be used.
        if (radius < 0) {
            h_x2_div_d = -h_x2_div_d;
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
     *
     * @return angle in radians of a line going from start to end.
     */
    static public double getAngle(final Position start, final Position end, PlaneFormatter plane) {
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
     * @return sweep in radians.
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

    static public Set<Code> getMCodes(List<String> args) {
        return getCodes(args, 'M');
    }

    static public Set<Code> getGCodes(List<String> args) {
        return getCodes(args, 'G');
    }

    static public Set<Code> getCodes(List<String> args, Character letter) {
        List<String> gCodeStrings = parseCodes(args, letter);
        return gCodeStrings.stream()
                .map(c -> letter + c)
                .map(Code::lookupCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    public static class SplitCommand {
        public String extracted;
        public String remainder;
    }

    public static boolean isMotionWord(char character) {
        char c = Character.toUpperCase(character);
        return 
                c == 'X' || c == 'Y' || c == 'Z'
                || c == 'U' || c == 'V' || c == 'W'
                || c == 'I' || c == 'J' || c == 'K'
                || c == 'R';
    }

    /**
     * Return extracted motion words and remainder words.
     *
     * If the code is implicit, like the command "X0Y0", we'll still extract "X0Y0".
     * If the code is G0 or G1 and G53 is found, it will also be extracted:
     * http://linuxcnc.org/docs/html/gcode/g-code.html#gcode:g53
     */
    public static SplitCommand extractMotion(Code code, String command) {
        List<String> args = splitCommand(command);
        if (args.isEmpty()) return null;
        
        StringBuilder extracted = new StringBuilder();
        StringBuilder remainder = new StringBuilder();

        boolean includeG53 = code == G0 || code == G1;
        for (String arg : args) {
            char c = arg.charAt(0);
            Code lookup = Code.lookupCode(arg);
            if (lookup.getType() == Motion && lookup != code) return null;
            if (lookup == code || isMotionWord(c) || (includeG53 && lookup == G53)) {
                extracted.append(arg);
            } else {
                remainder.append(arg);
            }
        }

        if (extracted.length() == 0) return null;

        SplitCommand sc = new SplitCommand();
        sc.extracted= extracted.toString();
        sc.remainder = remainder.toString();

        return sc;
    }

    /**
     * Normalize a command by adding in implicit state.
     *
     * For example given the following program:
     *     G20
     *     G0 X10 F25
     *     Y10
     *
     * The third command would be normalized to:
     *     G0 Y10 F25
     *
     * @param command a command string to normalize.
     * @param state the machine state before the command.
     * @return normalized command.
     */
    public static String normalizeCommand(String command, GcodeState state) throws GcodeParserException {
        List<String> args = GcodePreprocessorUtils.splitCommand(command);
        Set<Code> gCodes = getGCodes(args);

        Code code = null;
        for (Code c : gCodes) {
            if (c.getType() == Motion) {
                code = c;
            }
        }

        // Fallback to current motion mode if the motion cannot be detected from command.
        if (code == null) {
            code = state.currentMotionMode;
        }

        SplitCommand split = extractMotion(code, command);

        // This could happen if the currentMotionMode is wrong.
        if (split == null) {
            throw new GcodeParserException("Invalid state attached to command, please notify the developers.");
        }

        StringBuilder result = new StringBuilder();

        // Don't add the state
        //result.append(state.toGcode());

        result.append("F").append(state.speed);
        result.append("S").append(state.spindleSpeed);

        // Check if we need to add the motion command back in.
        if (!gCodes.contains(code)) {
            result.append(state.currentMotionMode.toString());
        }

        // Add the motion command
        result.append(split.extracted);

        return result.toString();
    }
}
