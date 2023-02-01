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
public class ChristmasSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public ChristmasSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, ChristmasSource.class.getResourceAsStream("/fonts/christmas/ChristmasRegular.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Lamp 1", Category.HOLIDAY, font, "#", this));
        cliparts.add(new FontClipart("Lamp 2", Category.HOLIDAY, font, "%", this));
        cliparts.add(new FontClipart("Lamp 3", Category.HOLIDAY, font, "@", this));
        cliparts.add(new FontClipart("Star 1", Category.HOLIDAY, font, "*", this));
        cliparts.add(new FontClipart("Star 2", Category.HOLIDAY, font, "+", this));
        cliparts.add(new FontClipart("Star shooting", Category.HOLIDAY, font, "_", this));
        cliparts.add(new FontClipart("Christmas bell 1", Category.HOLIDAY, font, "[", this));
        cliparts.add(new FontClipart("Christmas bell 2", Category.HOLIDAY, font, "\\", this));
        cliparts.add(new FontClipart("Christmas bell 3", Category.HOLIDAY, font, "]", this));
        cliparts.add(new FontClipart("Candle 1", Category.HOLIDAY, font, "!", this));
        cliparts.add(new FontClipart("Candle 2", Category.HOLIDAY, font, "^", this));
        cliparts.add(new FontClipart("Candle 5", Category.HOLIDAY, font, "$", this));
        cliparts.add(new FontClipart("Candle christmas cake", Category.HOLIDAY, font, "`", this));
        cliparts.add(new FontClipart("Candles", Category.HOLIDAY, font, "&", this));
        cliparts.add(new FontClipart("Cross 1", Category.SIGNS_AND_SYMBOLS, font, "-", this));
        cliparts.add(new FontClipart("Cross 2", Category.SIGNS_AND_SYMBOLS, font, "=", this));
        cliparts.add(new FontClipart("Cross 3", Category.SIGNS_AND_SYMBOLS, font, "1", this));
        cliparts.add(new FontClipart("Cross 4", Category.SIGNS_AND_SYMBOLS, font, "2", this));
        cliparts.add(new FontClipart("Cross 5", Category.SIGNS_AND_SYMBOLS, font, "3", this));
        cliparts.add(new FontClipart("Cross 6", Category.SIGNS_AND_SYMBOLS, font, "4", this));
        cliparts.add(new FontClipart("Cross 7", Category.SIGNS_AND_SYMBOLS, font, "5", this));
        cliparts.add(new FontClipart("Cross 8", Category.SIGNS_AND_SYMBOLS, font, "6", this));
        cliparts.add(new FontClipart("Cross 9", Category.SIGNS_AND_SYMBOLS, font, "7", this));
        cliparts.add(new FontClipart("Cross 10", Category.SIGNS_AND_SYMBOLS, font, "8", this));
        cliparts.add(new FontClipart("Cross 11", Category.SIGNS_AND_SYMBOLS, font, "9", this));
        cliparts.add(new FontClipart("Christmas ball 1", Category.HOLIDAY, font, ":", this));
        cliparts.add(new FontClipart("Christmas ball 2", Category.HOLIDAY, font, "\"", this));
        cliparts.add(new FontClipart("Christmas ball 3", Category.HOLIDAY, font, "A", this));
        cliparts.add(new FontClipart("Christmas ball 4", Category.HOLIDAY, font, "D", this));
        cliparts.add(new FontClipart("Christmas ball 5", Category.HOLIDAY, font, "F", this));
        cliparts.add(new FontClipart("Christmas ball 6", Category.HOLIDAY, font, "G", this));
        cliparts.add(new FontClipart("Christmas ball 7", Category.HOLIDAY, font, "H", this));
        cliparts.add(new FontClipart("Christmas ball 8", Category.HOLIDAY, font, "K", this));
        cliparts.add(new FontClipart("Christmas ball 9", Category.HOLIDAY, font, "S", this));
        cliparts.add(new FontClipart("Christmas ball 10", Category.HOLIDAY, font, "L", this));
        cliparts.add(new FontClipart("Christmas ball star", Category.HOLIDAY, font, "J", this));
        cliparts.add(new FontClipart("Present 1", Category.HOLIDAY, font, "{", this));
        cliparts.add(new FontClipart("Present 2", Category.HOLIDAY, font, "}", this));
        cliparts.add(new FontClipart("Present 3", Category.HOLIDAY, font, "I", this));
        cliparts.add(new FontClipart("Present 4", Category.HOLIDAY, font, "O", this));
        cliparts.add(new FontClipart("Present 5", Category.HOLIDAY, font, "P", this));
        cliparts.add(new FontClipart("Present 6", Category.HOLIDAY, font, "U", this));
        cliparts.add(new FontClipart("Snow man 1", Category.HOLIDAY, font, "<", this));
        cliparts.add(new FontClipart("Snow man 2", Category.HOLIDAY, font, ">", this));
        cliparts.add(new FontClipart("Snow man 3", Category.HOLIDAY, font, "?", this));
        cliparts.add(new FontClipart("Snow man 4", Category.HOLIDAY, font, "B", this));
        cliparts.add(new FontClipart("Snow man 5", Category.HOLIDAY, font, "C", this));
        cliparts.add(new FontClipart("Snow man 6", Category.HOLIDAY, font, "M", this));
        cliparts.add(new FontClipart("Snow man 7", Category.HOLIDAY, font, "N", this));
        cliparts.add(new FontClipart("Christmas sock 1", Category.HOLIDAY, font, "Q", this));
        cliparts.add(new FontClipart("Christmas sock 2", Category.HOLIDAY, font, "R", this));
        cliparts.add(new FontClipart("Christmas sock 3", Category.HOLIDAY, font, "T", this));
        cliparts.add(new FontClipart("Christmas sock 4", Category.HOLIDAY, font, "W", this));
        cliparts.add(new FontClipart("Christmas sock 5", Category.HOLIDAY, font, "Y", this));
        cliparts.add(new FontClipart("Christmas sock 6", Category.HOLIDAY, font, "E", this));
        cliparts.add(new FontClipart("Snow man candy cane", Category.HOLIDAY, font, "V", this));
        cliparts.add(new FontClipart("Snow man 8", Category.HOLIDAY, font, "X", this));
        cliparts.add(new FontClipart("Snow man 9", Category.HOLIDAY, font, "Z", this));
        cliparts.add(new FontClipart("Christmas bell 3", Category.HOLIDAY, font, "e", this));
        cliparts.add(new FontClipart("Christmas bell 4", Category.HOLIDAY, font, "o", this));
        cliparts.add(new FontClipart("Christmas bell 5", Category.HOLIDAY, font, "p", this));
        cliparts.add(new FontClipart("Christmas bell 6", Category.HOLIDAY, font, "q", this));
        cliparts.add(new FontClipart("Christmas bell 7", Category.HOLIDAY, font, "r", this));
        cliparts.add(new FontClipart("Christmas bell 8", Category.HOLIDAY, font, "t", this));
        cliparts.add(new FontClipart("Christmas bell 9", Category.HOLIDAY, font, "u", this));
        cliparts.add(new FontClipart("Christmas bell 10", Category.HOLIDAY, font, "w", this));
        cliparts.add(new FontClipart("Christmas bell 11", Category.HOLIDAY, font, "y", this));
        cliparts.add(new FontClipart("Christmas bell 12", Category.HOLIDAY, font, "i", this));
        cliparts.add(new FontClipart("Christmas leaves 1", Category.HOLIDAY, font, ";", this));
        cliparts.add(new FontClipart("Christmas leaves 2", Category.HOLIDAY, font, "'", this));
        cliparts.add(new FontClipart("Christmas leaves 3", Category.HOLIDAY, font, "a", this));
        cliparts.add(new FontClipart("Christmas leaves 4", Category.HOLIDAY, font, "d", this));
        cliparts.add(new FontClipart("Christmas leaves 5", Category.HOLIDAY, font, "f", this));
        cliparts.add(new FontClipart("Christmas leaves 6", Category.HOLIDAY, font, "g", this));
        cliparts.add(new FontClipart("Christmas leaves 7", Category.HOLIDAY, font, "h", this));
        cliparts.add(new FontClipart("Christmas leaves 8", Category.HOLIDAY, font, "j", this));
        cliparts.add(new FontClipart("Christmas leaves 9", Category.HOLIDAY, font, "k", this));
        cliparts.add(new FontClipart("Christmas leaves 10", Category.HOLIDAY, font, "l", this));
        cliparts.add(new FontClipart("Christmas leaves 11", Category.HOLIDAY, font, "s", this));
        cliparts.add(new FontClipart("Christmas tree simple", Category.HOLIDAY, font, "b", this));
        cliparts.add(new FontClipart("Christmas tree 1", Category.HOLIDAY, font, ",", this));
        cliparts.add(new FontClipart("Christmas tree 2", Category.HOLIDAY, font, ".", this));
        cliparts.add(new FontClipart("Christmas tree 3", Category.HOLIDAY, font, "/", this));
        cliparts.add(new FontClipart("Christmas tree 4", Category.HOLIDAY, font, "c", this));
        cliparts.add(new FontClipart("Christmas tree 5", Category.HOLIDAY, font, "m", this));
        cliparts.add(new FontClipart("Christmas tree 6", Category.HOLIDAY, font, "n", this));
        cliparts.add(new FontClipart("Christmas tree 7", Category.HOLIDAY, font, "v", this));
        cliparts.add(new FontClipart("Christmas tree 8", Category.HOLIDAY, font, "x", this));
        cliparts.add(new FontClipart("Christmas tree 9", Category.HOLIDAY, font, "z", this));
    }

    @Override
    public String getName() {
        return "Christmas";
    }

    @Override
    public String getCredits() {
        return "Vivek Kambli";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/christmas-font-f4808";
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
