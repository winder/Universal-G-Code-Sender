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
public class WwfreebieSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public WwfreebieSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, WwfreebieSource.class.getResourceAsStream("/fonts/wwfreebie/Wwfreebie.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Teddy bear", Category.ANIMALS, font, "'", this));
        cliparts.add(new FontClipart("Bow", Category.DECORATIONS, font, ",", this));
        cliparts.add(new FontClipart("sign3", Category.SIGNS_AND_SYMBOLS, font, ".", this));
        cliparts.add(new FontClipart("sign4", Category.DECORATIONS, font, "/", this));
        cliparts.add(new FontClipart("Clover", Category.SIGNS_AND_SYMBOLS, font, "0", this));
        cliparts.add(new FontClipart("Clover 3", Category.SIGNS_AND_SYMBOLS, font, "1", this));
        cliparts.add(new FontClipart("Cards - Diamonds", Category.SIGNS_AND_SYMBOLS, font, "2", this));
        cliparts.add(new FontClipart("Cards - Hearts", Category.SIGNS_AND_SYMBOLS, font, "3", this));
        cliparts.add(new FontClipart("Cards - Clubs", Category.SIGNS_AND_SYMBOLS, font, "4", this));
        cliparts.add(new FontClipart("Cards - Spades", Category.SIGNS_AND_SYMBOLS, font, "5", this));
        cliparts.add(new FontClipart("Lips 1", Category.SIGNS_AND_SYMBOLS, font, "6", this));
        cliparts.add(new FontClipart("Lips 2", Category.SIGNS_AND_SYMBOLS, font, "7", this));
        cliparts.add(new FontClipart("Lips 3", Category.SIGNS_AND_SYMBOLS, font, "8", this));
        cliparts.add(new FontClipart("Dove", Category.SIGNS_AND_SYMBOLS, font, "9", this));
        cliparts.add(new FontClipart("sign15", Category.DECORATIONS, font, "A", this));
        cliparts.add(new FontClipart("sign16", Category.DECORATIONS, font, "B", this));
        cliparts.add(new FontClipart("sign17", Category.DECORATIONS, font, "C", this));
        cliparts.add(new FontClipart("sign18", Category.DECORATIONS, font, "D", this));
        cliparts.add(new FontClipart("sign19", Category.DECORATIONS, font, "E", this));
        cliparts.add(new FontClipart("Diamond", Category.SIGNS_AND_SYMBOLS, font, "F", this));
        cliparts.add(new FontClipart("sign21", Category.DECORATIONS, font, "G", this));
        cliparts.add(new FontClipart("Trident", Category.SIGNS_AND_SYMBOLS, font, "H", this));
        cliparts.add(new FontClipart("sign23", Category.DECORATIONS, font, "I", this));
        cliparts.add(new FontClipart("sign24", Category.DECORATIONS, font, "J", this));
        cliparts.add(new FontClipart("sign25", Category.DECORATIONS, font, "K", this));
        cliparts.add(new FontClipart("Star 7", Category.SIGNS_AND_SYMBOLS, font, "L", this));
        cliparts.add(new FontClipart("Star 6", Category.SIGNS_AND_SYMBOLS, font, "M", this));
        cliparts.add(new FontClipart("Star 5", Category.SIGNS_AND_SYMBOLS, font, "N", this));
        cliparts.add(new FontClipart("Star 4", Category.SIGNS_AND_SYMBOLS, font, "O", this));
        cliparts.add(new FontClipart("sign30", Category.DECORATIONS, font, "P", this));
        cliparts.add(new FontClipart("sign31", Category.DECORATIONS, font, "Q", this));
        cliparts.add(new FontClipart("sign32", Category.DECORATIONS, font, "R", this));
        cliparts.add(new FontClipart("sign33", Category.DECORATIONS, font, "S", this));
        cliparts.add(new FontClipart("sign34", Category.DECORATIONS, font, "T", this));
        cliparts.add(new FontClipart("sign35", Category.DECORATIONS, font, "U", this));
        cliparts.add(new FontClipart("sign36", Category.DECORATIONS, font, "V", this));
        cliparts.add(new FontClipart("sign37", Category.SIGNS_AND_SYMBOLS, font, "W", this));
        cliparts.add(new FontClipart("sign38", Category.SIGNS_AND_SYMBOLS, font, "X", this));
        cliparts.add(new FontClipart("sign39", Category.SIGNS_AND_SYMBOLS, font, "Y", this));
        cliparts.add(new FontClipart("sign40", Category.SIGNS_AND_SYMBOLS, font, "Z", this));
        cliparts.add(new FontClipart("sign41", Category.DECORATIONS, font, "a", this));
        cliparts.add(new FontClipart("sign42", Category.DECORATIONS, font, "b", this));
        cliparts.add(new FontClipart("sign43", Category.DECORATIONS, font, "c", this));
        cliparts.add(new FontClipart("sign44", Category.DECORATIONS, font, "d", this));
        cliparts.add(new FontClipart("sign45", Category.DECORATIONS, font, "e", this));
        cliparts.add(new FontClipart("Star 3", Category.SIGNS_AND_SYMBOLS, font, "f", this));
        cliparts.add(new FontClipart("Star 2", Category.SIGNS_AND_SYMBOLS, font, "g", this));
        cliparts.add(new FontClipart("sign48", Category.DECORATIONS, font, "h", this));
        cliparts.add(new FontClipart("sign49", Category.DECORATIONS, font, "i", this));
        cliparts.add(new FontClipart("sign50", Category.DECORATIONS, font, "j", this));
        cliparts.add(new FontClipart("sign51", Category.DECORATIONS, font, "k", this));
        cliparts.add(new FontClipart("sign52", Category.DECORATIONS, font, "l", this));
        cliparts.add(new FontClipart("Clover 2", Category.SIGNS_AND_SYMBOLS, font, "m", this));
        cliparts.add(new FontClipart("sign54", Category.DECORATIONS, font, "n", this));
        cliparts.add(new FontClipart("Star 1", Category.SIGNS_AND_SYMBOLS, font, "o", this));
        cliparts.add(new FontClipart("sign56", Category.DECORATIONS, font, "p", this));
        cliparts.add(new FontClipart("sign57", Category.DECORATIONS, font, "q", this));
        cliparts.add(new FontClipart("sign58", Category.DECORATIONS, font, "r", this));
        cliparts.add(new FontClipart("Star 9", Category.SIGNS_AND_SYMBOLS, font, "s", this));
        cliparts.add(new FontClipart("Apperature", Category.SIGNS_AND_SYMBOLS, font, "t", this));
        cliparts.add(new FontClipart("sign61", Category.SIGNS_AND_SYMBOLS, font, "u", this));
        cliparts.add(new FontClipart("sign62", Category.SIGNS_AND_SYMBOLS, font, "v", this));
        cliparts.add(new FontClipart("sign63", Category.DECORATIONS, font, "w", this));
        cliparts.add(new FontClipart("sign64", Category.DECORATIONS, font, "x", this));
        cliparts.add(new FontClipart("sign65", Category.DECORATIONS, font, "y", this));
    }

    @Override
    public String getName() {
        return "WWFreebie";
    }

    @Override
    public String getCredits() {
        return "WindWalker64";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/wwfreebie-font-f3394";
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
