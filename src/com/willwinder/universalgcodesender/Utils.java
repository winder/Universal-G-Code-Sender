package com.willwinder.universalgcodesender;

import java.io.*;

/**
 *
 * @author wwinder
 */
public class Utils {
    public static String timeSince(long from){
        long until = System.currentTimeMillis();
        long elapsedTime = until - from;
        String format = String.format("%%0%dd", 2);  
        elapsedTime = elapsedTime / 1000;  
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
