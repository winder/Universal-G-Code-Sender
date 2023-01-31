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
public class Corners2Source implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public Corners2Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, Corners2Source.class.getResourceAsStream("/fonts/corners2/Corners2.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Corner", Category.DECORATIONS, font, "A", this));
        cliparts.add(new FontClipart("Corner with flowers 1", Category.DECORATIONS, font, "B", this));
        cliparts.add(new FontClipart("Corner with fireworks", Category.DECORATIONS, font, "C", this));
        cliparts.add(new FontClipart("Corner with star and banner", Category.DECORATIONS, font, "D", this));
        cliparts.add(new FontClipart("Corner with star and leaves", Category.DECORATIONS, font, "E", this));
        cliparts.add(new FontClipart("Corner with eagle", Category.DECORATIONS, font, "F", this));
        cliparts.add(new FontClipart("Corner with bells", Category.DECORATIONS, font, "G", this));
        cliparts.add(new FontClipart("Banner with heart and petals", Category.DECORATIONS, font.deriveFont(font.getSize() * 0.8f), "H", this));
        cliparts.add(new FontClipart("Banner", Category.DECORATIONS, font, "I", this));
        cliparts.add(new FontClipart("Corner with flowers 2", Category.DECORATIONS, font, "J", this));
        cliparts.add(new FontClipart("Corner with fairy", Category.DECORATIONS, font, "K", this));
        cliparts.add(new FontClipart("Corner with angel", Category.DECORATIONS, font, "L", this));
    }

    @Override
    public String getName() {
        return "Corners2";
    }

    @Override
    public String getCredits() {
        return "Digital Magic";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/corners-2-font-f5917";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    public String getLicense() {
        return "Free for commercial use";
    }
}
