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

        cliparts.add(new FontClipart("hibiskus", Category.PLANTS, font, "A"));
        cliparts.add(new FontClipart("leaf", Category.PLANTS, font, "B"));
        cliparts.add(new FontClipart("palm tree", Category.PLANTS, font, "F"));
        cliparts.add(new FontClipart("palm tree", Category.PLANTS, font, "G"));
        cliparts.add(new FontClipart("flower", Category.PLANTS, font, "V"));
        cliparts.add(new FontClipart("lanterns", Category.MYTHICAL, font, "H"));
        cliparts.add(new FontClipart("tiki mask", Category.MYTHICAL, font, "I"));
        cliparts.add(new FontClipart("pineapple", Category.FOOD, font, "J"));
        cliparts.add(new FontClipart("coconuts", Category.FOOD, font, "K"));
        cliparts.add(new FontClipart("drink", Category.FOOD, font, "L"));
        cliparts.add(new FontClipart("hulu woman", Category.PEOPLE, font, "M"));
        cliparts.add(new FontClipart("surfer", Category.PEOPLE, font, "N"));
        cliparts.add(new FontClipart("hulu woman", Category.PEOPLE, font, "U"));
        cliparts.add(new FontClipart("flower necklace", Category.UNSORTED, font, "C"));
        cliparts.add(new FontClipart("ukulele", Category.UNSORTED, font, "D"));
        cliparts.add(new FontClipart("barrel", Category.UNSORTED, font, "E"));
        cliparts.add(new FontClipart("sandals", Category.UNSORTED, font, "S"));
        cliparts.add(new FontClipart("pendant", Category.UNSORTED, font, "T"));
        cliparts.add(new FontClipart("fish", Category.ANIMALS, font, "O"));
        cliparts.add(new FontClipart("sea star", Category.ANIMALS, font, "P"));
        cliparts.add(new FontClipart("shell", Category.ANIMALS, font, "Q"));
        cliparts.add(new FontClipart("parrot", Category.ANIMALS, font, "R"));
        cliparts.add(new FontClipart("cabin", Category.BUILDINGS, font, "W"));
        cliparts.add(new FontClipart("machete", Category.TOOLS, font, "X"));
        cliparts.add(new FontClipart("bamboo", Category.DECORATIONS, font, "Y"));
        cliparts.add(new FontClipart("bamboo", Category.DECORATIONS, font, "Z"));
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
}
