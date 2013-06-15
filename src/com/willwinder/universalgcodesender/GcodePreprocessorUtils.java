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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        df.append("#.");
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
        return command.replaceAll(" ", "");
    }
}
