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
public class VintageDecorativeSigns2Source implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public VintageDecorativeSigns2Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, VintageDecorativeSigns2Source.class.getResourceAsStream("/fonts/vintage-decorative-signs-2-font/VintageDecorativeSigns2-mmLG.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Frame 01", Category.DECORATIONS, font, "a", this));
        cliparts.add(new FontClipart("Frame 02", Category.DECORATIONS, font, "b", this));
        cliparts.add(new FontClipart("Frame 03", Category.DECORATIONS, font, "c", this));
        cliparts.add(new FontClipart("Frame 04", Category.DECORATIONS, font, "d", this));
        cliparts.add(new FontClipart("Frame 05", Category.DECORATIONS, font, "e", this));
        cliparts.add(new FontClipart("Frame 06", Category.DECORATIONS, font, "f", this));
        cliparts.add(new FontClipart("Frame 07", Category.DECORATIONS, font, "g", this));
        cliparts.add(new FontClipart("Frame 08", Category.DECORATIONS, font, "h", this));
        cliparts.add(new FontClipart("Frame 09", Category.DECORATIONS, font, "i", this));
        cliparts.add(new FontClipart("Frame 10", Category.DECORATIONS, font, "j", this));
        cliparts.add(new FontClipart("Frame 11", Category.DECORATIONS, font, "k", this));
        cliparts.add(new FontClipart("Frame 12", Category.DECORATIONS, font, "l", this));
        cliparts.add(new FontClipart("Frame 13", Category.DECORATIONS, font, "m", this));
        cliparts.add(new FontClipart("Frame 14", Category.DECORATIONS, font, "n", this));
        cliparts.add(new FontClipart("Frame 15", Category.DECORATIONS, font, "o", this));
        cliparts.add(new FontClipart("Frame 16", Category.DECORATIONS, font, "p", this));
        cliparts.add(new FontClipart("Frame 17", Category.DECORATIONS, font, "q", this));
        cliparts.add(new FontClipart("Frame 18", Category.DECORATIONS, font, "r", this));
        cliparts.add(new FontClipart("Frame 19", Category.DECORATIONS, font, "s", this));
        cliparts.add(new FontClipart("Frame 20", Category.DECORATIONS, font, "t", this));
        cliparts.add(new FontClipart("Frame 21", Category.DECORATIONS, font, "u", this));
        cliparts.add(new FontClipart("Frame 22", Category.DECORATIONS, font, "v", this));
        cliparts.add(new FontClipart("Frame 23", Category.DECORATIONS, font, "w", this));
        cliparts.add(new FontClipart("Frame 24", Category.DECORATIONS, font, "x", this));
        cliparts.add(new FontClipart("Frame 25", Category.DECORATIONS, font, "y", this));
        cliparts.add(new FontClipart("Frame 26", Category.DECORATIONS, font, "z", this));
        cliparts.add(new FontClipart("Frame 27", Category.DECORATIONS, font, "A", this));
        cliparts.add(new FontClipart("Frame 28", Category.DECORATIONS, font, "B", this));
        cliparts.add(new FontClipart("Frame 29", Category.DECORATIONS, font, "C", this));
        cliparts.add(new FontClipart("Frame 30", Category.DECORATIONS, font, "D", this));
        cliparts.add(new FontClipart("Frame 31", Category.DECORATIONS, font, "E", this));
        cliparts.add(new FontClipart("Frame 32", Category.DECORATIONS, font, "F", this));
        cliparts.add(new FontClipart("Frame 33", Category.DECORATIONS, font, "G", this));
        cliparts.add(new FontClipart("Frame 34", Category.DECORATIONS, font, "H", this));
        cliparts.add(new FontClipart("Frame 35", Category.DECORATIONS, font, "I", this));
        cliparts.add(new FontClipart("Frame 36", Category.DECORATIONS, font, "J", this));
        cliparts.add(new FontClipart("Frame 37", Category.DECORATIONS, font, "K", this));
        cliparts.add(new FontClipart("Frame 38", Category.DECORATIONS, font, "L", this));
        cliparts.add(new FontClipart("Frame 39", Category.DECORATIONS, font, "M", this));
        cliparts.add(new FontClipart("Frame 40", Category.DECORATIONS, font, "N", this));
        cliparts.add(new FontClipart("Frame 41", Category.DECORATIONS, font, "O", this));
        cliparts.add(new FontClipart("Frame 42", Category.DECORATIONS, font, "P", this));
        cliparts.add(new FontClipart("Frame 43", Category.DECORATIONS, font, "Q", this));
        cliparts.add(new FontClipart("Frame 44", Category.DECORATIONS, font, "R", this));
        cliparts.add(new FontClipart("Frame 45", Category.DECORATIONS, font, "S", this));
        cliparts.add(new FontClipart("Frame 46", Category.DECORATIONS, font, "T", this));
        cliparts.add(new FontClipart("Frame 47", Category.DECORATIONS, font, "U", this));
        cliparts.add(new FontClipart("Frame 48", Category.DECORATIONS, font, "V", this));
        cliparts.add(new FontClipart("Frame 49", Category.DECORATIONS, font, "W", this));
        cliparts.add(new FontClipart("Frame 50", Category.DECORATIONS, font, "X", this));
        cliparts.add(new FontClipart("Frame 51", Category.DECORATIONS, font, "Y", this));
        cliparts.add(new FontClipart("Frame 52", Category.DECORATIONS, font, "Z", this));
        cliparts.add(new FontClipart("Frame 53", Category.DECORATIONS, font, "0", this));
        cliparts.add(new FontClipart("Frame 54", Category.DECORATIONS, font, "1", this));
        cliparts.add(new FontClipart("Frame 55", Category.DECORATIONS, font, "2", this));
        cliparts.add(new FontClipart("Frame 56", Category.DECORATIONS, font, "3", this));
        cliparts.add(new FontClipart("Frame 57", Category.DECORATIONS, font, "4", this));
        cliparts.add(new FontClipart("Frame 58", Category.DECORATIONS, font, "5", this));
        cliparts.add(new FontClipart("Frame 59", Category.DECORATIONS, font, "6", this));
        cliparts.add(new FontClipart("Frame 60", Category.DECORATIONS, font, "7", this));
        cliparts.add(new FontClipart("Frame 61", Category.DECORATIONS, font, "8", this));
        cliparts.add(new FontClipart("Frame 62", Category.DECORATIONS, font, "9", this));
    }

    @Override
    public String getName() {
        return "Vintage Decorative signs 2";
    }

    @Override
    public String getCredits() {
        return "Sughayer Foundry";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/vintage-decorative-signs-2-font-f19771";
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
