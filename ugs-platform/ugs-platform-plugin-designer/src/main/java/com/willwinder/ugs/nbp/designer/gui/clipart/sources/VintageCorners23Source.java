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
package com.willwinder.ugs.nbp.designer.gui.clipart.sources;

import com.willwinder.ugs.nbp.designer.gui.clipart.Category;
import com.willwinder.ugs.nbp.designer.gui.clipart.Clipart;
import com.willwinder.ugs.nbp.designer.gui.clipart.ClipartSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.ClipartSourceException;
import com.willwinder.ugs.nbp.designer.gui.clipart.FontClipart;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class VintageCorners23Source implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public VintageCorners23Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, VintageCorners23Source.class.getResourceAsStream("/fonts/vintage-decorative-corners-23-font/VintageDecorativeCorners23-5wXa.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Corner 04", Category.DECORATIONS, font, "d", this));
        cliparts.add(new FontClipart("Corner 10", Category.DECORATIONS, font, "j", this));
        cliparts.add(new FontClipart("Corner 15", Category.DECORATIONS, font, "o", this));
        cliparts.add(new FontClipart("Corner 17", Category.DECORATIONS, font, "q", this));
        cliparts.add(new FontClipart("Corner 20", Category.DECORATIONS, font, "t", this));
        cliparts.add(new FontClipart("Corner 24", Category.DECORATIONS, font, "x", this));

    }

    @Override
    public String getName() {
        return "Vintage Corners 23";
    }

    @Override
    public String getCredits() {
        return "Sughayer Foundry";
    }

    @Override
    public String getUrl() {
        return "link: https://www.fontspace.com/vintage-decorative-corners-23-font-f20049";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    public String getLicense() {
        return "Free for non-commercial use";
    }
}
