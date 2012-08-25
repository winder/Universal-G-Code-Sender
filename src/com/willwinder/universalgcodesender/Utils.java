package com.willwinder.universalgcodesender;

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
}
