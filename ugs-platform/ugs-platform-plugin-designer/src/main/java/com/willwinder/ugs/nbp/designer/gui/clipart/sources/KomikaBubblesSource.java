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
public class KomikaBubblesSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public KomikaBubblesSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, KomikaBubblesSource.class.getResourceAsStream("/fonts/komika-bubbles/KomikaBubbles-6pq.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("bubble 1", Category.DECORATIONS, font, "A", this));
        cliparts.add(new FontClipart("bubble 2", Category.DECORATIONS, font, "B", this));
        cliparts.add(new FontClipart("bubble 3", Category.DECORATIONS, font, "C", this));
        cliparts.add(new FontClipart("bubble 4", Category.DECORATIONS, font, "D", this));
        cliparts.add(new FontClipart("bubble 5", Category.DECORATIONS, font, "E", this));
        cliparts.add(new FontClipart("bubble 6", Category.DECORATIONS, font, "F", this));
        cliparts.add(new FontClipart("bubble 7", Category.DECORATIONS, font, "G", this));
        cliparts.add(new FontClipart("bubble 8", Category.DECORATIONS, font, "H", this));
        cliparts.add(new FontClipart("bubble 9", Category.DECORATIONS, font, "I", this));
        cliparts.add(new FontClipart("bubble 10", Category.DECORATIONS, font, "J", this));
        cliparts.add(new FontClipart("bubble 11", Category.DECORATIONS, font, "K", this));
        cliparts.add(new FontClipart("bubble 12", Category.DECORATIONS, font, "L", this));
        cliparts.add(new FontClipart("bubble 13", Category.DECORATIONS, font, "M", this));
        cliparts.add(new FontClipart("bubble 14", Category.DECORATIONS, font, "N", this));
        cliparts.add(new FontClipart("bubble 15", Category.DECORATIONS, font, "O", this));
        cliparts.add(new FontClipart("bubble 16", Category.DECORATIONS, font, "P", this));
        cliparts.add(new FontClipart("bubble 17", Category.DECORATIONS, font, "Q", this));
        cliparts.add(new FontClipart("bubble 18", Category.DECORATIONS, font, "R", this));
        cliparts.add(new FontClipart("bubble 19", Category.DECORATIONS, font, "S", this));
        cliparts.add(new FontClipart("bubble 20", Category.DECORATIONS, font, "T", this));
    }

    @Override
    public String getName() {
        return "Komika Bubbles";
    }

    @Override
    public String getCredits() {
        return "Apostrophic Lab";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/komika-bubbles-font-f404";
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
