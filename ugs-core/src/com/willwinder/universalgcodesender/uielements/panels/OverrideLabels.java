/*
    Copyright 2024 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.i18n.Localization;

public class OverrideLabels {
    public static final String FEED_SHORT = Localization.getString("overrides.feed.short");
    public static final String SPINDLE_SHORT = Localization.getString("overrides.spindle.short");
    public static final String RAPID_SHORT = Localization.getString("overrides.rapid.short");
    public static final String TOGGLE_SHORT = Localization.getString("overrides.toggle.short");
    public static final String RESET_SPINDLE = Localization.getString("overrides.spindle.reset");
    public static final String RESET_FEED = Localization.getString("overrides.feed.reset");
    public static final String MINUS_COARSE = "--";
    public static final String MINUS_FINE = "-";
    public static final String PLUS_COARSE = "++";
    public static final String PLUS_FINE = "+";
    public static final String RAPID_LOW = Localization.getString("overrides.rapid.low");
    public static final String RAPID_MEDIUM = Localization.getString("overrides.rapid.medium");
    public static final String RAPID_FULL = Localization.getString("overrides.rapid.full");
    public static final String MIST = Localization.getString("overrides.mist");
    public static final String FLOOD = Localization.getString("overrides.flood");
    public static final String NOT_SUPPORTED = Localization.getString("overrides.not.supported");
}
