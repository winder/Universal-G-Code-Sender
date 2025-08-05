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
    private static final Collection<Language> LANGUAGES = new ArrayList<>();
    
    static {
        LANGUAGES.add(new Language("af", "ZA", "Afrikaans"));
        LANGUAGES.add(new Language("ca", "ES", "Catalan"));
        LANGUAGES.add(new Language("zh", "CN", "Chinese (Simplified)"));
        LANGUAGES.add(new Language("zh", "TW", "Chinese (Traditional)"));
        LANGUAGES.add(new Language("cs", "CZ", "Czech"));
        LANGUAGES.add(new Language("nl", "NL", "Dutch"));
        LANGUAGES.add(new Language("en", "US", "English"));
        LANGUAGES.add(new Language("fr", "FR", "French"));
        LANGUAGES.add(new Language("de", "DE", "German"));
        LANGUAGES.add(new Language("el", "GR", "Greek"));
        LANGUAGES.add(new Language("it", "IT", "Italian"));
        LANGUAGES.add(new Language("ja", "JP", "Japanese"));
        LANGUAGES.add(new Language("lt", "LT", "Lithuanian"));
        LANGUAGES.add(new Language("nb", "NO", "Norwegian Bokmål"));
        LANGUAGES.add(new Language("nn", "NO", "Norwegian Nynorsk"));
        LANGUAGES.add(new Language("fa", "IR", "Persian (Farsi)"));
        LANGUAGES.add(new Language("pl", "PL", "Polish"));
        LANGUAGES.add(new Language("pt", "BR", "Portuguese (Brazilian)"));
        LANGUAGES.add(new Language("ru", "RU", "Russian"));
        LANGUAGES.add(new Language("sk", "SK", "Slovak"));
        LANGUAGES.add(new Language("es", "ES", "Spanish"));
        LANGUAGES.add(new Language("sv", "SE", "Swedish"));
        LANGUAGES.add(new Language("tr", "TR", "Turkish"));
        LANGUAGES.add(new Language("uk", "UA", "Ukrainian"));
    }

    public static Collection<Language> getAvailableLanguages() {
        return LANGUAGES;
    }
}

