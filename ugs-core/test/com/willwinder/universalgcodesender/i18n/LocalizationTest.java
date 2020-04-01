/*
    Copyright 2018 Will Winder

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

import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the localization functions
 *
 * @author Joacim Breiler
 */
public class LocalizationTest {

    private static final String DESCRIPTION_KEY = "description";

    @Test
    public void loadLocalizationThatDoesNotExistShouldRevertToEnglish() {
        boolean containsAllTranslations = Localization.initialize("fake", "LOCALE");
        assertTrue("The english translations is the reference and should always contain every translatable string", containsAllTranslations);
        assertEquals("Description", Localization.getString(DESCRIPTION_KEY));
        assertEquals("en", Locale.getDefault().getLanguage());
        assertEquals("US", Locale.getDefault().getCountry());
    }

    @Test
    public void loadLocalizationThatDoesExistShouldLoad() {
        Localization.initialize("sv", "SE");
        assertEquals("Beskrivning", Localization.getString(DESCRIPTION_KEY));
        assertEquals("sv", Locale.getDefault().getLanguage());
        assertEquals("SE", Locale.getDefault().getCountry());
    }

    @Test
    public void minusSignsShouldBeConvertedWithRightCharacter() {
        NumberFormat formatter = new DecimalFormat("#.###", Localization.dfs);
        assertEquals("-1", formatter.format(-1));
    }

    @Test
    public void commasShouldBeConvertedWithRightCharacter() {
        NumberFormat formatter = new DecimalFormat("#.###", Localization.dfs);
        assertEquals("1.111", formatter.format(1.111));
    }
}
