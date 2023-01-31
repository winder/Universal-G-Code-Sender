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
public class HouseIconsSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public HouseIconsSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, HouseIconsSource.class.getResourceAsStream("/fonts/house-icons/house-icons.otf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("House 01", Category.BUILDINGS, font, "A", this));
        cliparts.add(new FontClipart("House 02", Category.BUILDINGS, font, "B", this));
        cliparts.add(new FontClipart("House 03", Category.BUILDINGS, font, "C", this));
        cliparts.add(new FontClipart("House 04", Category.BUILDINGS, font, "D", this));
        cliparts.add(new FontClipart("House 05", Category.BUILDINGS, font, "E", this));
        cliparts.add(new FontClipart("House 06", Category.BUILDINGS, font, "F", this));
        cliparts.add(new FontClipart("House 07", Category.BUILDINGS, font, "G", this));
        cliparts.add(new FontClipart("House 08", Category.BUILDINGS, font, "H", this));
        cliparts.add(new FontClipart("House 09", Category.BUILDINGS, font, "I", this));
        cliparts.add(new FontClipart("House 10", Category.BUILDINGS, font, "J", this));
        cliparts.add(new FontClipart("House 11", Category.BUILDINGS, font, "K", this));
        cliparts.add(new FontClipart("House 12", Category.BUILDINGS, font, "L", this));
        cliparts.add(new FontClipart("House 13", Category.BUILDINGS, font, "M", this));
        cliparts.add(new FontClipart("House 14", Category.BUILDINGS, font, "N", this));
        cliparts.add(new FontClipart("House 15", Category.BUILDINGS, font, "O", this));
        cliparts.add(new FontClipart("House 16", Category.BUILDINGS, font, "P", this));
        cliparts.add(new FontClipart("House 17", Category.BUILDINGS, font, "Q", this));
        cliparts.add(new FontClipart("House 18", Category.BUILDINGS, font, "R", this));
        cliparts.add(new FontClipart("House 19", Category.BUILDINGS, font, "S", this));
        cliparts.add(new FontClipart("House 20", Category.BUILDINGS, font, "T", this));
        cliparts.add(new FontClipart("House 21", Category.BUILDINGS, font, "U", this));
        cliparts.add(new FontClipart("House 22", Category.BUILDINGS, font, "V", this));
        cliparts.add(new FontClipart("House 23", Category.BUILDINGS, font, "W", this));
        cliparts.add(new FontClipart("House 24", Category.BUILDINGS, font, "X", this));
        cliparts.add(new FontClipart("House 25", Category.BUILDINGS, font, "Y", this));
        cliparts.add(new FontClipart("House 26", Category.BUILDINGS, font, "Z", this));
        cliparts.add(new FontClipart("House 27", Category.BUILDINGS, font, "a", this));
        cliparts.add(new FontClipart("House 28", Category.BUILDINGS, font, "b", this));
        cliparts.add(new FontClipart("House 28", Category.BUILDINGS, font, "c", this));
        cliparts.add(new FontClipart("House 29", Category.BUILDINGS, font, "d", this));
        cliparts.add(new FontClipart("House 30", Category.BUILDINGS, font, "e", this));
        cliparts.add(new FontClipart("House 31", Category.BUILDINGS, font, "f", this));
        cliparts.add(new FontClipart("House 32", Category.BUILDINGS, font, "g", this));
        cliparts.add(new FontClipart("House 33", Category.BUILDINGS, font, "h", this));
        cliparts.add(new FontClipart("House 34", Category.BUILDINGS, font, "i", this));
        cliparts.add(new FontClipart("House 35", Category.BUILDINGS, font, "j", this));
        cliparts.add(new FontClipart("House 36", Category.BUILDINGS, font, "k", this));
        cliparts.add(new FontClipart("House 37", Category.BUILDINGS, font, "l", this));
        cliparts.add(new FontClipart("House 38", Category.BUILDINGS, font, "m", this));
        cliparts.add(new FontClipart("House 39", Category.BUILDINGS, font, "n", this));
        cliparts.add(new FontClipart("House 40", Category.BUILDINGS, font, "o", this));
        cliparts.add(new FontClipart("House 41", Category.BUILDINGS, font, "p", this));
        cliparts.add(new FontClipart("House 42", Category.BUILDINGS, font, "q", this));
        cliparts.add(new FontClipart("House 43", Category.BUILDINGS, font, "r", this));
        cliparts.add(new FontClipart("House 44", Category.BUILDINGS, font, "s", this));
        cliparts.add(new FontClipart("House 45", Category.BUILDINGS, font, "t", this));
        cliparts.add(new FontClipart("House 46", Category.BUILDINGS, font, "u", this));
        cliparts.add(new FontClipart("House 47", Category.BUILDINGS, font, "v", this));
        cliparts.add(new FontClipart("House 48", Category.BUILDINGS, font, "w", this));
        cliparts.add(new FontClipart("House 49", Category.BUILDINGS, font, "x", this));
        cliparts.add(new FontClipart("House 50", Category.BUILDINGS, font, "y", this));
        cliparts.add(new FontClipart("House 51", Category.BUILDINGS, font, "z", this));
        cliparts.add(new FontClipart("House 52", Category.BUILDINGS, font, "0", this));
        cliparts.add(new FontClipart("House 53", Category.BUILDINGS, font, "1", this));
        cliparts.add(new FontClipart("House 54", Category.BUILDINGS, font, "2", this));
        cliparts.add(new FontClipart("House 55", Category.BUILDINGS, font, "3", this));
        cliparts.add(new FontClipart("House 56", Category.BUILDINGS, font, "4", this));
        cliparts.add(new FontClipart("House 57", Category.BUILDINGS, font, "5", this));
        cliparts.add(new FontClipart("House 58", Category.BUILDINGS, font, "6", this));
        cliparts.add(new FontClipart("House 59", Category.BUILDINGS, font, "7", this));
        cliparts.add(new FontClipart("House 60", Category.BUILDINGS, font, "8", this));
        cliparts.add(new FontClipart("House 61", Category.BUILDINGS, font, "9", this));
    }

    @Override
    public String getName() {
        return "House Icons";
    }

    @Override
    public String getCredits() {
        return "Woodcutter";
    }

    @Override
    public String getUrl() {
        return "https://www.dafont.com/house-icons.font";
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
