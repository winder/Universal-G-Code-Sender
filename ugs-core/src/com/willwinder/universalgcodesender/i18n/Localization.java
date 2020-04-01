/*
    Copyright 2013-2018 Will Winder

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

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Localization messages.
 * 
 * @author wwinder
 */
public class Localization {
    private static final Logger logger = Logger.getLogger(Localization.class.getName());
    public final static DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
    static {
        dfs.setDecimalSeparator('.');
        dfs.setMinusSign('-');
    }

    private static ResourceBundle bundle = null;
    private static ResourceBundle english = null;

    private static int englishKeyCount = 0;
    private static String region = null;

    /**
     * Loads a given language. If no translations is found for the given language it will default to english.
     *
     * @param language IETF language tag with an underscore, like "en_US"
     * @return Returns false if some keys are missing compared to "en_US"
     */
    synchronized public static boolean initialize(String language) {
        String[] lang = language.split("_");
        return initialize(lang[0], lang[1]);
    }

    /**
     * Loads a given language. If no translations is found for the given language it will default to english.
     *
     * @param language the language to load, ex: en, sv, de
     * @param region the region of the language to load, ex: US, SE, DE
     * @return Returns false if some keys are missing compared to "en_US"
     */
    synchronized public static boolean initialize(String language, String region) {
        try {
            loadResourceBundle(language, region);
        } catch (MissingResourceException e) {
            logger.log(Level.WARNING, "Couldn't find translations for the locale " + language + "_" + region + ". Reverts to en_US");
            loadResourceBundle("en", "US");
        }
        return getKeyCount(bundle) >= getEnglishKeyCount();
    }

    /**
     * Loads the resource bundle with all translations for the given language and region
     *
     * @param language the language to load, ex: en, sv, de
     * @param region the region of the language to load, ex: US, SE, DE
     * @throws MissingResourceException if the resource bundle couldn't be found
     */
    private static void loadResourceBundle(String language, String region) throws MissingResourceException {
        Localization.region = region;
        Locale locale = new Locale(language, region);
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("resources.MessagesBundle", locale);
    }

    public static String loadedLocale() {
        return Locale.getDefault() + "";
    }

    /**
     * When localizing GUI components, sometimes you need to ensure the region
     * is loaded when getting a string to avoid falling back to English.
     */
    public static String getString(String id, String region) {
        if (region == null || !region.equals(Localization.region)) {
            initialize(region);
        }
        return getString(id);
    }

    public static String getString(String id) {
        String result = "";
        try {
            String val = bundle.getString(id);
            result = new String(val.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            // Ignore this error, we will later try to fetch the string from the english bundle
        }

        if( StringUtils.isEmpty(StringUtils.trimToEmpty(result))) {
            try {
                if (english == null) {
                    english = ResourceBundle.getBundle("resources.MessagesBundle", new Locale("en", "US"));
                }
                String val = english.getString(id);
                result = new String(val.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                result = "<" + id + ">";
            }
        }

        return result;
    }

    public static Map<String, String> getStrings() {
        Map<String, String> texts = new HashMap<>();
        bundle.keySet().forEach(key -> texts.put(key, bundle.getString(key)));
        return texts;
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
