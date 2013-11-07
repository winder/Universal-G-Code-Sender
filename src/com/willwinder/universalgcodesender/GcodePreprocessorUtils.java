/*
 * Collection of useful command preprocessor methods.
 */

/*
    Copywrite 2013 Will Winder

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
    /**
     * Searches the command string for an 'f' and replaces the speed value 
     * between the 'f' and the next space with a percentage of that speed.
     * In that way all speed values become a ratio of the provided speed 
     * and don't get overridden with just a fixed speed.
     */
    static protected String overrideSpeed(String command, double speed) {
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
    static protected String removeComment(String command) {
        String newCommand = command;

        // Remove any comments within ( parentheses ) with regex "\([^\(]*\)"
        newCommand = newCommand.replaceAll("\\([^\\(]*\\)", "");

        // Remove any comment beginning with ';' with regex "\;[^\\(]*"
        newCommand = newCommand.replaceAll("\\;[^\\\\(]*", "");

        return newCommand.trim();
    }
    
    /**
     * Searches for a comment in the input string and returns the first match.
     */
    static protected String parseComment(String command) {
        String comment = "";

        // REGEX: Find any comment, includes the comment characters:
        //              "(?<=\()[^\(\)]*|(?<=\;)[^;]*"
        //              "(?<=\\()[^\\(\\)]*|(?<=\\;)[^;]*"
        
        Pattern pattern = Pattern.compile("(?<=\\()[^\\(\\)]*|(?<=\\;)[^;]*");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()){
            comment = matcher.group(0);
        }

        return comment;
    }
    
    static protected String truncateDecimals(int length, String command) {
        StringBuilder df = new StringBuilder();
        
        // Build up the decimal formatter.
        df.append("#");
        
        if (length != 0) { df.append("."); }
        for (int i=0; i < length; i++) {
            df.append('#');
        }
        DecimalFormat formatter = new DecimalFormat(df.toString());
        
        // Build up the regular expression.
        df = new StringBuilder();
        df.append("\\d+\\.\\d");
        for (int i=0; i < length; i++) {
            df.append("\\d");
        }
        df.append('+');
        Pattern pattern = Pattern.compile(df.toString());
        Matcher matcher = pattern.matcher(command);

        // Build up the truncated command.
        Double d;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            d = Double.parseDouble(matcher.group());
            matcher.appendReplacement(sb, formatter.format(d));
        }
        matcher.appendTail(sb);
        
        // Return new command.
        return sb.toString();
    }
    
    static protected String removeAllWhitespace(String command) {
        return command.replaceAll("\\s","");
    }
    
    static private Pattern gPattern = Pattern.compile("[Gg]0*(\\d+)");
    static public List<Integer> parseGCodes(String command) {
        Matcher matcher = gPattern.matcher(command);
        List<Integer> codes = new ArrayList<Integer>();
        
        while (matcher.find()) {
            codes.add(Integer.parseInt(matcher.group(1)));
        }
        
        return codes;
    }

    static private Pattern mPattern = Pattern.compile("[Mm]0*(\\d+)");
    static public List<Integer> parseMCodes(String command) {
        Matcher matcher = gPattern.matcher(command);
        List<Integer> codes = new ArrayList<Integer>();
        
        while (matcher.find()) {
            codes.add(Integer.parseInt(matcher.group(1)));
        }
        
        return codes;
    }

    static Point3d updatePointWithCommand(String command, Point3d initial) {
        Point3d newPoint = new Point3d(initial);
        String[] sarr = command.split(" ");
        

        
        
        return newPoint;
    }
    
    /**
     * Splits a gcode command by each word/argument, doesn't care about spaces.
     * This command is about 2.4x slower than splitting on spaces.
     */
    static public List<String> splitCommand(String command) {
        List<String> l = new ArrayList<String>();
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
    static public double parseCoord(List<String> sarr, char c)
    {
        char address = Character.toUpperCase(c);
        for(String t : sarr)
        {
            if (Character.toUpperCase(t.charAt(0)) == address)
            {
                return Double.parseDouble(t.substring(1));
            }
        }
        return Double.NaN;
    }
}
