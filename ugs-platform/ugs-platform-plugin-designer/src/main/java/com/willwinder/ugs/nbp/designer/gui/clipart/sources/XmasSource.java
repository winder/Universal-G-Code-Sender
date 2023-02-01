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
public class XmasSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public XmasSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, XmasSource.class.getResourceAsStream("/fonts/xmas/XmasClipart2.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Christmas Candy Cane 1", Category.HOLIDAY, font, "A", this));
        cliparts.add(new FontClipart("Present 1", Category.HOLIDAY, font, "B", this));
        cliparts.add(new FontClipart("Christmas Santa 1", Category.HOLIDAY, font, "C", this));
        cliparts.add(new FontClipart("Christmas bells", Category.HOLIDAY, font, "D", this));
        cliparts.add(new FontClipart("Christmas ornament 2", Category.HOLIDAY, font, "E", this));
        cliparts.add(new FontClipart("Snow man 1", Category.HOLIDAY, font, "F", this));
        cliparts.add(new FontClipart("Snow man 2", Category.HOLIDAY, font, "G", this));
        cliparts.add(new FontClipart("Present 2", Category.HOLIDAY, font, "H", this));
        cliparts.add(new FontClipart("Candle 1", Category.HOLIDAY, font, "I", this));
        cliparts.add(new FontClipart("Present 3", Category.HOLIDAY, font, "J", this));
        cliparts.add(new FontClipart("Christmas Sled 1", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.8f), "K", this));
        cliparts.add(new FontClipart("Christmas Santa 3", Category.HOLIDAY, font, "L", this));
        cliparts.add(new FontClipart("Christmas Sled 2", Category.HOLIDAY, font, "M", this));
        cliparts.add(new FontClipart("Christmas ball 1", Category.HOLIDAY, font, "N", this));
        cliparts.add(new FontClipart("Snow man 3", Category.HOLIDAY, font, "O", this));
        cliparts.add(new FontClipart("Christmas tree 1", Category.HOLIDAY, font, "P", this));
        cliparts.add(new FontClipart("Christmas Santa 4", Category.HOLIDAY, font, "Q", this));
        cliparts.add(new FontClipart("Reindeer", Category.ANIMALS, font, "R", this));
        cliparts.add(new FontClipart("Christmas ornament 1", Category.HOLIDAY, font, "S", this));
        cliparts.add(new FontClipart("Christmas Santa 5", Category.HOLIDAY, font, "T", this));
        cliparts.add(new FontClipart("Christmas Santa 6", Category.HOLIDAY, font, "U", this));
        cliparts.add(new FontClipart("Christmas Elves", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.8f), "V", this));
        cliparts.add(new FontClipart("Christmas ornament 3", Category.HOLIDAY, font, "W", this));
        cliparts.add(new FontClipart("Christmas Santa 7", Category.HOLIDAY, font, "X", this));
        cliparts.add(new FontClipart("Christmas Santa 8", Category.HOLIDAY, font, "Y", this));
        cliparts.add(new FontClipart("Christmas Santa 9", Category.HOLIDAY, font, "Z", this));

        cliparts.add(new FontClipart("Christmas Santa 10", Category.HOLIDAY, font, "a", this));
        cliparts.add(new FontClipart("Christmas sock 2", Category.HOLIDAY, font, "b", this));
        cliparts.add(new FontClipart("Christmas Santa 11", Category.HOLIDAY, font, "c", this));
        cliparts.add(new FontClipart("Christmas Santa 12", Category.HOLIDAY, font, "d", this));
        cliparts.add(new FontClipart("Snowman 4", Category.HOLIDAY, font, "e", this));
        cliparts.add(new FontClipart("Christmas North pole", Category.HOLIDAY, font, "f", this));
        cliparts.add(new FontClipart("Christmas Santa 14", Category.HOLIDAY, font, "g", this));
        cliparts.add(new FontClipart("Christmas Santa 15", Category.HOLIDAY, font, "h", this));
        cliparts.add(new FontClipart("Christmas Sled 3", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.8f), "i", this));
        cliparts.add(new FontClipart("Christmas sock 1", Category.HOLIDAY, font, "j", this));
    }

    @Override
    public String getName() {
        return "Xmas";
    }

    @Override
    public String getCredits() {
        return "GemFonts";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/xmas-clipart-2-font-f4270";
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
