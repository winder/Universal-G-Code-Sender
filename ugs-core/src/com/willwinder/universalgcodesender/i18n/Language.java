/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.i18n;

/**
 *
 * @author Christian Moll
 */
public class Language {
        private final String language;
        private final String region;
        private final String nameEnglish;

        public Language(String language, String region, String nameEnglish) {
            this.language = language;
            this.region = region;
            this.nameEnglish = nameEnglish;
        }
        
        public String toString() {
            return nameEnglish;
        }
        
        public String getLanguage() {
            return language;
        }
    
        public String getRegion() {
            return region;
        }

        public String getLanguageCode() {
            return language + "_" + region;
        }
    }