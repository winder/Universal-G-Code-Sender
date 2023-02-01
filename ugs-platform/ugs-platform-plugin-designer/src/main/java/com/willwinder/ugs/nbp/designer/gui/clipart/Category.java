/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * @author Joacim Breiler
 */
public enum Category {
    ANIMALS("platform.plugin.designer.clipart.animals"),
    BUILDINGS("platform.plugin.designer.clipart.buildings"),
    DECORATIONS("platform.plugin.designer.clipart.decorations"),
    ELECTRONICS("platform.plugin.designer.clipart.electronics"),
    FOOD("platform.plugin.designer.clipart.food"),
    HOLIDAY("platform.plugin.designer.clipart.holiday"),
    LOGOS("platform.plugin.designer.clipart.logos"),
    MYTHICAL("platform.plugin.designer.clipart.mythical"),
    PEOPLE_AND_CHARACTERS("platform.plugin.designer.clipart.people"),
    PLANTS("platform.plugin.designer.clipart.plants"),
    SCIENCE("platform.plugin.designer.clipart.science"),
    SIGNS_AND_SYMBOLS("platform.plugin.designer.clipart.signs_and_symbols"),
    TOOLS("platform.plugin.designer.clipart.tools"),
    TRANSPORTATION("platform.plugin.designer.clipart.transportation"),
    UNSORTED("platform.plugin.designer.clipart.unsorted"),
    WEATHER("platform.plugin.designer.clipart.weather");

    private final String localizationKey;

    Category(String localizationKey) {
        this.localizationKey = localizationKey;
    }

    public String getTitle() {
        return Localization.getString(localizationKey);
    }
}
