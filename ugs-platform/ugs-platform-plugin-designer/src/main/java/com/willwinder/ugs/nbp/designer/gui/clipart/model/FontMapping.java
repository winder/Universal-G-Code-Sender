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
package com.willwinder.ugs.nbp.designer.gui.clipart.model;

import java.util.Collections;
import java.util.List;

/**
 * A model for deserialize font configuration JSON
 *
 * @author Joacim Breiler
 */
public class FontMapping {
    private String font;
    private String name;
    private String credits;
    private String url;
    private String license;
    private List<FontMappingClipart> cliparts;

    public List<FontMappingClipart> getCliparts() {
        if (cliparts == null) {
            return Collections.emptyList();
        }
        return cliparts;
    }

    public String getFont() {
        return font;
    }

    public String getName() {
        return name;
    }

    public String getCredits() {
        return credits;
    }

    public String getUrl() {
        return url;
    }

    public String getLicense() {
        return license;
    }
}
