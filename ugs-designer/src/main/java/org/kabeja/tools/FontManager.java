/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class FontManager {
    private static FontManager instance = new FontManager();
    private String fontDescription = "conf/font.properties";
    private Hashtable fontProperties = new Hashtable();

    private FontManager() {
        loadFontDescription();
    }

    public void setFontDescription(String file) {
        this.fontDescription = file;
        loadFontDescription();
    }

    private void loadFontDescription() {
        fontProperties.clear();

        try {
            InputStream stream = this.getClass()
                                     .getResourceAsStream(this.fontDescription);

            if (stream == null) {
                try {
                    stream = new FileInputStream(this.fontDescription);
                } catch (FileNotFoundException e1) {
                }
            }

            if (stream != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                            stream));
                String line = null;

                while ((line = in.readLine()) != null) {
                    int index = line.indexOf("=");

                    if (index >= 0) {
                        String font = line.substring(0, index).trim()
                                          .toLowerCase();
                        String svgFont = line.substring(index + 1).trim();
                        fontProperties.put(font, svgFont);
                    }
                }
            } else {
                // System.out.println("no font.properties");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FontManager getInstance() {
        return instance;
    }

    /**
     * Query if a SVG font description exists for the given shx font.
     *
     * @param font
     *            The font.shx or font
     * @return
     */
    public boolean hasFontDescription(String font) {
        font = getFontKey(font);

        if (fontProperties.containsKey(font)) {
            return true;
        }

        return false;
    }

    public String getFontDescription(String font) {
        return (String) fontProperties.get(getFontKey(font));
    }

    private String getFontKey(String font) {
        font = font.toLowerCase();

        if (font.endsWith(".shx")) {
            font = font.substring(0, font.indexOf(".shx"));
        }

        return font;
    }
}
