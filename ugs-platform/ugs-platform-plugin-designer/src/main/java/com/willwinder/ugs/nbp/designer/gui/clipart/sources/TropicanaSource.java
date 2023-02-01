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
public class TropicanaSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public TropicanaSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, TropicanaSource.class.getResourceAsStream("/fonts/tropicana/TropicanaBv-83M.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("hibiskus", Category.PLANTS, font, "A", this));
        cliparts.add(new FontClipart("leaf", Category.PLANTS, font, "B", this));
        cliparts.add(new FontClipart("palm tree", Category.PLANTS, font, "F", this));
        cliparts.add(new FontClipart("palm tree", Category.PLANTS, font, "G", this));
        cliparts.add(new FontClipart("flower", Category.PLANTS, font, "V", this));
        cliparts.add(new FontClipart("lanterns", Category.MYTHICAL, font, "H", this));
        cliparts.add(new FontClipart("tiki mask", Category.MYTHICAL, font, "I", this));
        cliparts.add(new FontClipart("pineapple", Category.FOOD, font, "J", this));
        cliparts.add(new FontClipart("coconuts", Category.FOOD, font, "K", this));
        cliparts.add(new FontClipart("drink", Category.FOOD, font, "L", this));
        cliparts.add(new FontClipart("hulu woman", Category.PEOPLE_AND_CHARACTERS, font, "M", this));
        cliparts.add(new FontClipart("surfer", Category.PEOPLE_AND_CHARACTERS, font, "N", this));
        cliparts.add(new FontClipart("hulu woman", Category.PEOPLE_AND_CHARACTERS, font, "U", this));
        cliparts.add(new FontClipart("flower necklace", Category.UNSORTED, font, "C", this));
        cliparts.add(new FontClipart("ukulele", Category.UNSORTED, font, "D", this));
        cliparts.add(new FontClipart("barrel", Category.UNSORTED, font, "E", this));
        cliparts.add(new FontClipart("sandals", Category.UNSORTED, font, "S", this));
        cliparts.add(new FontClipart("pendant", Category.UNSORTED, font, "T", this));
        cliparts.add(new FontClipart("fish", Category.ANIMALS, font, "O", this));
        cliparts.add(new FontClipart("sea star", Category.ANIMALS, font, "P", this));
        cliparts.add(new FontClipart("shell", Category.ANIMALS, font, "Q", this));
        cliparts.add(new FontClipart("parrot", Category.ANIMALS, font, "R", this));
        cliparts.add(new FontClipart("cabin", Category.BUILDINGS, font, "W", this));
        cliparts.add(new FontClipart("machete", Category.TOOLS, font, "X", this));
        cliparts.add(new FontClipart("bamboo", Category.DECORATIONS, font, "Y", this));
        cliparts.add(new FontClipart("bamboo", Category.DECORATIONS, font, "Z", this));
    }

    @Override
    public String getName() {
        return "Tropicana";
    }

    @Override
    public String getCredits() {
        return "Blue Vinyl";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/tropicana-bv-font-f984";
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
