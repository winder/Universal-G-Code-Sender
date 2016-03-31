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
        private String language;
        private String region;
        private String nameEnglish;

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
    }