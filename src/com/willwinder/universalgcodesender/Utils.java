/*
 * A collection of utilities that don't relate to anything in particular.
 */

/*
    Copywrite 2012 Will Winder

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

import java.io.*;

/**
 *
 * @author wwinder
 */
public class Utils {    
    public static String timeSince(long from){
        long elapsedTime = millisSince(from);
        return Utils.formattedMillis(elapsedTime);  
    }
    
    public static long millisSince(long from) {
        long until = System.currentTimeMillis();
        return until - from;
    }

    public static String formattedMillis(long millis) {
        String format = String.format("%%0%dd", 2);  
        long elapsedTime = millis / 1000;  
        String seconds = String.format(format, elapsedTime % 60);  
        String minutes = String.format(format, (elapsedTime % 3600) / 60);  
        String hours = String.format(format, elapsedTime / 3600);  
        String time =  hours + ":" + minutes + ":" + seconds;  
        return time;
    }
    
    // Processes input file.
    // This could theoretically scan it for errors, but GcodeSender just counts
    // how many lines are in it.
    public static int processFile(File file) throws FileNotFoundException, IOException {
        Integer numRows = 0;
        InputStream is = new BufferedInputStream(new FileInputStream(file));

        byte[] c = new byte[1024];

        int readChars;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n')
                    ++numRows;
            }
        }

        is.close();

        return numRows;
    }
}
