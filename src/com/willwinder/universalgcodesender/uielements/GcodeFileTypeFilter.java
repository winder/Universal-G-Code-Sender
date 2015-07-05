/*
 * FileFilter which is limited to gcode files.
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

package com.willwinder.universalgcodesender.uielements;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
/**
 *
 * @author wwinder
 */
public class GcodeFileTypeFilter extends FileFilter {
    public static JFileChooser getGcodeFileChooser(String startDir) {
        //Setup the file filter for gcode files.
        GcodeFileTypeFilter filter = new GcodeFileTypeFilter();
        
        // Setup file browser with the last path used.
        JFileChooser fileChooser = new JFileChooser(startDir); 
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(true);
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(filter);
        
        return fileChooser;
    }
    
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
        String extension = getExtension(f);
        if (extension != null) {
            return 
                    extension.equals("cnc") ||
                    extension.equals("nc")  ||
                    extension.equals("ngc") ||
                    extension.equals("tap") ||
                    extension.equals("txt") ||
                    extension.equals("gcode");
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
