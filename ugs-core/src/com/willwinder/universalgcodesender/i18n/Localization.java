/*
 * Localization messages.
 *
 * Created on Dec 15 2013
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
package com.willwinder.universalgcodesender.i18n;

import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author wwinder
 */
public class Localization {
    public final static DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
    static {dfs.setDecimalSeparator('.');}

    private static ResourceBundle bundle = null;
    private static ResourceBundle english = null;

    private static int englishKeyCount = 0;

    /**
     * Loads a given language.
     * @param language
     * @return Returns false if some keys are missing compared to "en_US"
     */
    synchronized public static boolean initialize(String language) {
        String[] lang = language.split("_");
        return initialize(lang[0], lang[1]);
    }
    
    /**
     * Loads a given language.
     * @param language
     * @return Returns false if some keys are missing compared to "en_US"
     */
    synchronized public static boolean initialize(String language, String region) {
        Locale locale = new Locale(language, region);
        bundle = ResourceBundle.getBundle("resources.MessagesBundle", locale);
        return getKeyCount(bundle) >= getEnglishKeyCount();
    }

    public static String getString(String id) {
        try {
            String val = bundle.getString(id);
            return new String(val.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            try {
                if (english == null)
                    english = ResourceBundle.getBundle("resources.MessagesBundle", new Locale("en", "US"));
                String val = english.getString(id);
                return new String(val.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e2) { 
                return "<" + id + ">";
            }
        }
    }

    private static int getEnglishKeyCount() {
        if (englishKeyCount > 0) return englishKeyCount;
        ResourceBundle b= ResourceBundle.getBundle("resources.MessagesBundle", new Locale("en", "US"));
        englishKeyCount = getKeyCount(b);
        return englishKeyCount;
    }

    private static int getKeyCount(ResourceBundle b) {
        Enumeration<String> keyEnum = b.getKeys();

        int ret = 0;
        while (keyEnum.hasMoreElements()) {
            keyEnum.nextElement();
            ret++;
        }

        return ret;
    }
}
