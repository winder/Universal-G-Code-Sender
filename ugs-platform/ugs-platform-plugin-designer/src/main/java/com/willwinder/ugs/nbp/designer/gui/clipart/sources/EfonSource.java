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
public class EfonSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public EfonSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, EfonSource.class.getResourceAsStream("/fonts/efon/Efon-Gl4q.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("flower", Category.PLANTS, font, "\""));
        cliparts.add(new FontClipart("petals 1", Category.PLANTS, font, "!"));
        cliparts.add(new FontClipart("petals 2", Category.PLANTS, font, "#"));
        cliparts.add(new FontClipart("petals 3", Category.PLANTS, font, "$"));
        cliparts.add(new FontClipart("plant", Category.PLANTS, font, ")"));
        cliparts.add(new FontClipart("leaf 1", Category.PLANTS, font, ":"));
        cliparts.add(new FontClipart("leaf 2", Category.PLANTS, font, "["));
        cliparts.add(new FontClipart("cactus", Category.PLANTS, font, "@"));
        cliparts.add(new FontClipart("mouse 3", Category.ANIMALS, font, "^"));
        cliparts.add(new FontClipart("rabbit", Category.ANIMALS, font, "_"));
        cliparts.add(new FontClipart("crab", Category.ANIMALS, font, "~"));
        cliparts.add(new FontClipart("bread", Category.FOOD, font, "+"));
        cliparts.add(new FontClipart("carrot", Category.FOOD, font, ","));
        cliparts.add(new FontClipart("strawberry", Category.FOOD, font, "<"));
        cliparts.add(new FontClipart("frog", Category.ANIMALS, font, "'"));
        cliparts.add(new FontClipart("hamster", Category.ANIMALS, font, "*"));
        cliparts.add(new FontClipart("fish 1", Category.ANIMALS, font, "3"));
        cliparts.add(new FontClipart("polliwog", Category.ANIMALS, font, "A"));
        cliparts.add(new FontClipart("egg", Category.FOOD, font, "B"));
        cliparts.add(new FontClipart("fox", Category.ANIMALS, font, "D"));
        cliparts.add(new FontClipart("telephone", Category.ELECTRONICS, font, "F"));
        cliparts.add(new FontClipart("tram", Category.TRANSPORTATION, font, "G"));
        cliparts.add(new FontClipart("bus", Category.TRANSPORTATION, font, "H"));
        cliparts.add(new FontClipart("car", Category.TRANSPORTATION, font, "I"));
        cliparts.add(new FontClipart("umbrella", Category.WEATHER, font, "N"));
        cliparts.add(new FontClipart("plane", Category.TRANSPORTATION, font, "L"));
        cliparts.add(new FontClipart("sun", Category.WEATHER, font, "M"));
        cliparts.add(new FontClipart("cat", Category.ANIMALS, font, "P"));
        cliparts.add(new FontClipart("cat angry", Category.ANIMALS, font, "P"));
        cliparts.add(new FontClipart("octopus", Category.ANIMALS, font, "Q"));
        cliparts.add(new FontClipart("fish 2", Category.ANIMALS, font, "S"));
        cliparts.add(new FontClipart("ant", Category.ANIMALS, font, "T"));
        cliparts.add(new FontClipart("duck", Category.ANIMALS, font, "U"));
        cliparts.add(new FontClipart("duckling", Category.ANIMALS, font, "V"));
        cliparts.add(new FontClipart("mouse 1", Category.ANIMALS, font, "X"));
        cliparts.add(new FontClipart("mouse 2", Category.ANIMALS, font, "Y"));
        cliparts.add(new FontClipart("ghost", Category.ANIMALS, font, "Z"));
        cliparts.add(new FontClipart("robot", Category.ELECTRONICS, font, "x"));
        cliparts.add(new FontClipart("moon", Category.WEATHER, font, "e"));
        cliparts.add(new FontClipart("cellphone", Category.ELECTRONICS, font, "f"));
        cliparts.add(new FontClipart("television", Category.ELECTRONICS, font, "h"));
        cliparts.add(new FontClipart("coffey", Category.FOOD, font, "r"));
        cliparts.add(new FontClipart("rain", Category.WEATHER, font, "n"));
    }

    @Override
    public String getName() {
        return "Efon Font";
    }

    @Override
    public String getCredits() {
        return "Sakurai Nan";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/efon-font-f4531";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
