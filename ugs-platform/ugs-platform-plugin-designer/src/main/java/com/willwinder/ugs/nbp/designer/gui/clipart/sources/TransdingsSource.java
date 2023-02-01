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
public class TransdingsSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public TransdingsSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, TransdingsSource.class.getResourceAsStream("/fonts/transdings/Transdings-WaoO.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Transformers Sector7", Category.SIGNS_AND_SYMBOLS, font, ",", this));
        cliparts.add(new FontClipart("Transformers 02", Category.SIGNS_AND_SYMBOLS, font, ".", this));
        cliparts.add(new FontClipart("Transformers 03", Category.SIGNS_AND_SYMBOLS, font, "0", this));
        cliparts.add(new FontClipart("Transformers 04", Category.SIGNS_AND_SYMBOLS, font, "1", this));
        cliparts.add(new FontClipart("Transformers 05", Category.SIGNS_AND_SYMBOLS, font, "2", this));
        cliparts.add(new FontClipart("Transformers 06", Category.SIGNS_AND_SYMBOLS, font, "3", this));
        cliparts.add(new FontClipart("Transformers 07", Category.SIGNS_AND_SYMBOLS, font, "4", this));
        cliparts.add(new FontClipart("Transformers 09", Category.SIGNS_AND_SYMBOLS, font, "5", this));
        cliparts.add(new FontClipart("Transformers 10", Category.SIGNS_AND_SYMBOLS, font, ":", this));
        cliparts.add(new FontClipart("Transformers 11", Category.SIGNS_AND_SYMBOLS, font, ";", this));
        cliparts.add(new FontClipart("Transformers 12", Category.SIGNS_AND_SYMBOLS, font, "@", this));
        cliparts.add(new FontClipart("Transformers Autobot 1", Category.SIGNS_AND_SYMBOLS, font, "A", this));
        cliparts.add(new FontClipart("Transformers 14", Category.SIGNS_AND_SYMBOLS, font, "B", this));
        cliparts.add(new FontClipart("Transformers 15", Category.SIGNS_AND_SYMBOLS, font, "C", this));
        cliparts.add(new FontClipart("Transformers 16", Category.SIGNS_AND_SYMBOLS, font, "D", this));
        cliparts.add(new FontClipart("Transformers 17", Category.SIGNS_AND_SYMBOLS, font, "E", this));
        cliparts.add(new FontClipart("Transformers 18", Category.SIGNS_AND_SYMBOLS, font, "F", this));
        cliparts.add(new FontClipart("Transformers 19", Category.SIGNS_AND_SYMBOLS, font, "G", this));
        cliparts.add(new FontClipart("Transformers 20", Category.SIGNS_AND_SYMBOLS, font, "H", this));
        cliparts.add(new FontClipart("Transformers 21", Category.SIGNS_AND_SYMBOLS, font, "I", this));
        cliparts.add(new FontClipart("Transformers 22", Category.SIGNS_AND_SYMBOLS, font, "J", this));
        cliparts.add(new FontClipart("Transformers 23", Category.SIGNS_AND_SYMBOLS, font, "K", this));
        cliparts.add(new FontClipart("Transformers 24", Category.SIGNS_AND_SYMBOLS, font, "L", this));
        cliparts.add(new FontClipart("Transformers 25", Category.SIGNS_AND_SYMBOLS, font, "M", this));
        cliparts.add(new FontClipart("Transformers Autobot 2", Category.SIGNS_AND_SYMBOLS, font, "N", this));
        cliparts.add(new FontClipart("Transformers Autobot 3", Category.SIGNS_AND_SYMBOLS, font, "O", this));
        cliparts.add(new FontClipart("Transformers Wreckers", Category.SIGNS_AND_SYMBOLS, font, "P", this));
        cliparts.add(new FontClipart("Transformers Autobot 5", Category.SIGNS_AND_SYMBOLS, font, "Q", this));
        cliparts.add(new FontClipart("Transformers 30", Category.SIGNS_AND_SYMBOLS, font, "R", this));
        cliparts.add(new FontClipart("Transformers 31", Category.SIGNS_AND_SYMBOLS, font, "S", this));
        cliparts.add(new FontClipart("Transformers Autobot 5", Category.SIGNS_AND_SYMBOLS, font, "T", this));
        cliparts.add(new FontClipart("Transformers 33", Category.SIGNS_AND_SYMBOLS, font, "V", this));
        cliparts.add(new FontClipart("Transformers 34", Category.SIGNS_AND_SYMBOLS, font, "W", this));
        cliparts.add(new FontClipart("Transformers 35", Category.SIGNS_AND_SYMBOLS, font, "X", this));
        cliparts.add(new FontClipart("Transformers 36", Category.SIGNS_AND_SYMBOLS, font, "Y", this));
        cliparts.add(new FontClipart("Transformers 37", Category.SIGNS_AND_SYMBOLS, font, "Z", this));
        cliparts.add(new FontClipart("Transformers 38", Category.SIGNS_AND_SYMBOLS, font, "[", this));
        cliparts.add(new FontClipart("Transformers 39", Category.SIGNS_AND_SYMBOLS, font, "\\", this));
        cliparts.add(new FontClipart("Transformers Decepticons 4", Category.SIGNS_AND_SYMBOLS, font, "]", this));
        cliparts.add(new FontClipart("Transformers 41", Category.SIGNS_AND_SYMBOLS, font, "`", this));
        cliparts.add(new FontClipart("Transformers Decepticons 1", Category.SIGNS_AND_SYMBOLS, font, "a", this));
        cliparts.add(new FontClipart("Transformers 43", Category.SIGNS_AND_SYMBOLS, font, "b", this));
        cliparts.add(new FontClipart("Transformers 44", Category.SIGNS_AND_SYMBOLS, font, "c", this));
        cliparts.add(new FontClipart("Transformers 45", Category.SIGNS_AND_SYMBOLS, font, "d", this));
        cliparts.add(new FontClipart("Transformers 46", Category.SIGNS_AND_SYMBOLS, font, "e", this));
        cliparts.add(new FontClipart("Transformers 47", Category.SIGNS_AND_SYMBOLS, font, "f", this));
        cliparts.add(new FontClipart("Transformers 48", Category.SIGNS_AND_SYMBOLS, font, "g", this));
        cliparts.add(new FontClipart("Transformers 49", Category.SIGNS_AND_SYMBOLS, font, "h", this));
        cliparts.add(new FontClipart("Transformers 50", Category.SIGNS_AND_SYMBOLS, font, "i", this));
        cliparts.add(new FontClipart("Transformers 51", Category.SIGNS_AND_SYMBOLS, font, "j", this));
        cliparts.add(new FontClipart("Transformers 52", Category.SIGNS_AND_SYMBOLS, font, "k", this));
        cliparts.add(new FontClipart("Transformers 53", Category.SIGNS_AND_SYMBOLS, font, "l", this));
        cliparts.add(new FontClipart("Transformers 54", Category.SIGNS_AND_SYMBOLS, font, "m", this));
        cliparts.add(new FontClipart("Transformers 55", Category.SIGNS_AND_SYMBOLS, font, "n", this));
        cliparts.add(new FontClipart("Transformers 56", Category.SIGNS_AND_SYMBOLS, font, "o", this));
        cliparts.add(new FontClipart("Transformers Decepticons 2", Category.SIGNS_AND_SYMBOLS, font, "p", this));
        cliparts.add(new FontClipart("Transformers 58", Category.SIGNS_AND_SYMBOLS, font, "q", this));
        cliparts.add(new FontClipart("Transformers 59", Category.SIGNS_AND_SYMBOLS, font, "r", this));
        cliparts.add(new FontClipart("Transformers 60", Category.SIGNS_AND_SYMBOLS, font, "s", this));
        cliparts.add(new FontClipart("Transformers Ultracon", Category.SIGNS_AND_SYMBOLS, font, "t", this));
        cliparts.add(new FontClipart("Transformers 62", Category.SIGNS_AND_SYMBOLS, font, "u", this));
        cliparts.add(new FontClipart("Transformers 63", Category.SIGNS_AND_SYMBOLS, font, "v", this));
        cliparts.add(new FontClipart("Transformers 64", Category.SIGNS_AND_SYMBOLS, font, "w", this));
        cliparts.add(new FontClipart("Transformers Decepticons 3", Category.SIGNS_AND_SYMBOLS, font, "x", this));
        cliparts.add(new FontClipart("Transformers 66", Category.SIGNS_AND_SYMBOLS, font, "y", this));
        cliparts.add(new FontClipart("Transformers 67", Category.SIGNS_AND_SYMBOLS, font, "z", this));
        cliparts.add(new FontClipart("Transformers 69", Category.SIGNS_AND_SYMBOLS, font, "~", this));
        cliparts.add(new FontClipart("Transformers 70", Category.SIGNS_AND_SYMBOLS, font, ";", this));
    }

    @Override
    public String getName() {
        return "Transdings";
    }

    @Override
    public String getCredits() {
        return "Pixel Sagas";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/transdings-font-f18144";
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
