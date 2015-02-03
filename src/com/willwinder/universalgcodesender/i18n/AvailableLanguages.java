/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

