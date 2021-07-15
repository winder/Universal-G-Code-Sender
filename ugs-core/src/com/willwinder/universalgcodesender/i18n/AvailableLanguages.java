/*
    Copyright 2014-2015 Will Winder

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

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author moll
 */
public class AvailableLanguages {
    private static Collection<Language> availableLanguages = new ArrayList<>();
    
    static {
        availableLanguages.add(new Language("af", "ZA", "Afrikaans"));
        availableLanguages.add(new Language("ca", "AD", "Catalan"));
        availableLanguages.add(new Language("zh", "CHS", "Chinese"));
        availableLanguages.add(new Language("zh", "Hans", "Chinese (Simplified)"));
        availableLanguages.add(new Language("cs", "CZ", "Czech"));
        availableLanguages.add(new Language("nl", "NL", "Dutch"));
        availableLanguages.add(new Language("en", "US", "English"));
        availableLanguages.add(new Language("fr", "FR", "French"));
        availableLanguages.add(new Language("de", "DE", "German"));
        availableLanguages.add(new Language("el", "EL", "Greek"));
        availableLanguages.add(new Language("it", "IT", "Italian"));
        availableLanguages.add(new Language("jp", "JA", "Japanese"));
        availableLanguages.add(new Language("lt", "LT", "Lithuanian"));
        availableLanguages.add(new Language("nb", "NO", "Norwegian Bokmål"));
        availableLanguages.add(new Language("nn", "NO", "Norwegian Nynorsk"));
        availableLanguages.add(new Language("fa", "IR", "Persian (Farsi)"));
        availableLanguages.add(new Language("pl", "PL", "Polish"));
        availableLanguages.add(new Language("pt", "BR", "Portuguese (Brazilian)"));
        availableLanguages.add(new Language("ru", "RU", "Russian"));
        availableLanguages.add(new Language("sk", "SK", "Slovak"));
        availableLanguages.add(new Language("es", "ES", "Spanish"));
        availableLanguages.add(new Language("sv", "SE", "Swedish"));
        availableLanguages.add(new Language("tr", "TR", "Turkish"));
        availableLanguages.add(new Language("uk", "UA", "Ukrainian"));
    }

    public static Collection<Language> getAvailableLanguages() {
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

