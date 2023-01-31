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

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class DestinysBordersSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public DestinysBordersSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, DestinysBordersSource.class.getResourceAsStream("/fonts/destinys-borders/DestinysBorderDings.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("sign1", Category.DECORATIONS, font, "A", this));
        cliparts.add(new FontClipart("sign2", Category.DECORATIONS, font, "B", this));
        cliparts.add(new FontClipart("sign3", Category.DECORATIONS, font, "C", this));
        cliparts.add(new FontClipart("sign4", Category.DECORATIONS, font, "D", this));
        cliparts.add(new FontClipart("sign5", Category.DECORATIONS, font, "E", this));
        cliparts.add(new FontClipart("sign6", Category.DECORATIONS, font, "F", this));
        cliparts.add(new FontClipart("sign7", Category.DECORATIONS, font, "G", this));
        cliparts.add(new FontClipart("sign8", Category.DECORATIONS, font, "H", this));
        cliparts.add(new FontClipart("sign9", Category.DECORATIONS, font, "I", this));
        cliparts.add(new FontClipart("sign10", Category.DECORATIONS, font, "J", this));
    }

    @Override
    public String getName() {
        return "Destinys Border";
    }

    @Override
    public String getCredits() {
        return "Destiny's Designs";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/destinys-border-dings-font-f12969";
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
