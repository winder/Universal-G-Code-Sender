/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.File;
import javax.swing.filechooser.FileFilter;
/**
 *
 * @author wwinder
 */
public class GcodeFileTypeFilter extends FileFilter {
       
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("cnc") ||
                extension.equals("nc")  ||
                extension.equals("ngc") ||
                extension.equals("tap") ||
                extension.equals("txt") ||
                extension.equals("gcode")) {
                    return true;
            } else {
                return false;
            }
        }
 
        return false;
    }
 
    //The description of this filter
    @Override
    public String getDescription() {
        return "G-Code Files";
    }
    
    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
