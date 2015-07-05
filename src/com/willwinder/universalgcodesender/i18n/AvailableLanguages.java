/*
    Copywrite 2014-2015 Will Winder

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

import java.util.Vector;

/**
 *
 * @author moll
 */
public class AvailableLanguages {
    private static Vector<Language> availableLanguages = new Vector<>();
    
    static {
        availableLanguages.add(new Language("en", "US", "English"));
        availableLanguages.add(new Language("de", "DE", "German"));
        availableLanguages.add(new Language("es", "ES", "Spanish"));
        availableLanguages.add(new Language("it", "IT", "Italian"));
        availableLanguages.add(new Language("af", "ZA", "Afrikaans"));
        availableLanguages.add(new Language("fr", "FR", "French"));
    }

    public static Vector<Language> getAvailableLanguages() {
        return availableLanguages;
    }

    public static Language getLanguageByString(String language) {
        String[] lang = language.split("_");
        for (Language l : availableLanguages) {
            if (l.getLanguage().equals(lang[0]) && l.getRegion().equals(lang[1])) {
                return l;
            }
        }
        return null;
    }
    
}

