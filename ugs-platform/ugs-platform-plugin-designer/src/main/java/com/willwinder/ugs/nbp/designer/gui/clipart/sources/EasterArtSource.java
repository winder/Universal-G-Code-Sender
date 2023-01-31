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
public class EasterArtSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public EasterArtSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, EasterArtSource.class.getResourceAsStream("/fonts/easterart/EasterArt.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }
        cliparts.add(new FontClipart("Easter egg 1", Category.HOLIDAY, font, "A", this));
        cliparts.add(new FontClipart("Easter egg 2", Category.HOLIDAY, font, "J", this));
        cliparts.add(new FontClipart("Easter egg 4", Category.HOLIDAY, font, "M", this));
        cliparts.add(new FontClipart("Easter egg 5", Category.HOLIDAY, font, "g", this));
        cliparts.add(new FontClipart("Easter egg 6", Category.HOLIDAY, font, "h", this));
        cliparts.add(new FontClipart("Easter egg 7", Category.HOLIDAY, font, "i", this));
        cliparts.add(new FontClipart("Easter egg 8", Category.HOLIDAY, font, "j", this));
        cliparts.add(new FontClipart("Easter egg 11", Category.HOLIDAY, font, "f", this));
        cliparts.add(new FontClipart("Easter egg 12", Category.HOLIDAY, font, "p", this));
        cliparts.add(new FontClipart("Easter egg 13", Category.HOLIDAY, font, "q", this));
        cliparts.add(new FontClipart("Easter egg 14", Category.HOLIDAY, font, "r", this));
        cliparts.add(new FontClipart("Easter eggs 1", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.6f), "L", this));
        cliparts.add(new FontClipart("Easter eggs 2", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.8f), "k", this));
        cliparts.add(new FontClipart("Easter eggs 3", Category.HOLIDAY, font, "l", this));
        cliparts.add(new FontClipart("Easter basket 3", Category.HOLIDAY, font, "P", this));
        cliparts.add(new FontClipart("Easter eggs 4", Category.HOLIDAY, font, "e", this));
        cliparts.add(new FontClipart("Easter bunny 4", Category.HOLIDAY, font, "Q", this));
        cliparts.add(new FontClipart("Easter basket 1", Category.HOLIDAY, font, "R", this));
        cliparts.add(new FontClipart("Easter basket 2", Category.HOLIDAY, font, "S", this));
        cliparts.add(new FontClipart("Easter basket 3", Category.HOLIDAY, font, "v", this));
        cliparts.add(new FontClipart("Easter basket 4", Category.HOLIDAY, font, "m", this));
        cliparts.add(new FontClipart("Bunny 1", Category.ANIMALS, font, "V", this));
        cliparts.add(new FontClipart("Bunny 3", Category.ANIMALS, font, "C", this));
        cliparts.add(new FontClipart("Bunny 7", Category.ANIMALS, font, "n", this));
        cliparts.add(new FontClipart("Bunny 8", Category.ANIMALS, font, "o", this));
        cliparts.add(new FontClipart("Bunny 9", Category.ANIMALS, font, "w", this));
        cliparts.add(new FontClipart("Easter bunny 1", Category.HOLIDAY, font, "K", this));
        cliparts.add(new FontClipart("Easter bunny 2", Category.HOLIDAY, font, "N", this));
        cliparts.add(new FontClipart("Easter bunny 3", Category.HOLIDAY, font, "O", this));
        cliparts.add(new FontClipart("Easter bunny 4", Category.HOLIDAY, font, "T", this));
        cliparts.add(new FontClipart("Easter bunny 5", Category.HOLIDAY, font, "U", this));
        cliparts.add(new FontClipart("Easter bunny 6", Category.HOLIDAY, font, "X", this));
        cliparts.add(new FontClipart("Easter bunny 7", Category.HOLIDAY, font, "Y", this));
        cliparts.add(new FontClipart("Easter bunny 8", Category.HOLIDAY, font, "Z", this));
        cliparts.add(new FontClipart("Easter bunny 9", Category.HOLIDAY, font, "a", this));
        cliparts.add(new FontClipart("Easter bunny 10", Category.HOLIDAY, font, "c", this));
        cliparts.add(new FontClipart("Easter bunny 11", Category.HOLIDAY, font, "d", this));
        cliparts.add(new FontClipart("Easter bunny 12", Category.HOLIDAY, font, "s", this));
        cliparts.add(new FontClipart("Easter bunny 13", Category.HOLIDAY, font, "t", this));
        cliparts.add(new FontClipart("Easter bunny 14", Category.HOLIDAY, font, "E", this));
        cliparts.add(new FontClipart("Easter bunny 15", Category.HOLIDAY, font, "F", this));
        cliparts.add(new FontClipart("Easter bunny 16", Category.HOLIDAY, font, "G", this));
        cliparts.add(new FontClipart("Easter bunny 17", Category.HOLIDAY, font, "H", this));
        cliparts.add(new FontClipart("Easter bunny 18", Category.HOLIDAY, font, "I", this));
        cliparts.add(new FontClipart("Easter bunny 19", Category.HOLIDAY, font, "B", this));
        cliparts.add(new FontClipart("Easter Bunny 20", Category.HOLIDAY, font, "W", this));
        cliparts.add(new FontClipart("Easter Bunny 21", Category.HOLIDAY, font, "b", this));
        cliparts.add(new FontClipart("Easter Bunny 22", Category.HOLIDAY, font, "D", this));
        cliparts.add(new FontClipart("Easter chickens 1", Category.HOLIDAY, font, "u", this));
    }

    @Override
    public String getName() {
        return "EasterArt";
    }

    @Override
    public String getCredits() {
        return "GemFonts";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/easter-art-font-f4082";
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
