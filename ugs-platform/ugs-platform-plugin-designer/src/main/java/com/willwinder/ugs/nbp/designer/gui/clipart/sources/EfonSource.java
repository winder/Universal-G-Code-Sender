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

        cliparts.add(new FontClipart("Flower", Category.PLANTS, font, "\"", this));
        cliparts.add(new FontClipart("Petals 1", Category.PLANTS, font, "!", this));
        cliparts.add(new FontClipart("Petals 2", Category.PLANTS, font, "#", this));
        cliparts.add(new FontClipart("Petals 3", Category.PLANTS, font, "$", this));
        cliparts.add(new FontClipart("Plant", Category.PLANTS, font, ")", this));
        cliparts.add(new FontClipart("Leaf 1", Category.PLANTS, font, ":", this));
        cliparts.add(new FontClipart("Leaf 2", Category.PLANTS, font, "[", this));
        cliparts.add(new FontClipart("Cactus", Category.PLANTS, font, "@", this));
        cliparts.add(new FontClipart("Mouse 3", Category.ANIMALS, font, "^", this));
        cliparts.add(new FontClipart("Rabbit", Category.ANIMALS, font, "_", this));
        cliparts.add(new FontClipart("Crab", Category.ANIMALS, font, "~", this));
        cliparts.add(new FontClipart("Bread", Category.FOOD, font, "+", this));
        cliparts.add(new FontClipart("Carrot", Category.FOOD, font, ",", this));
        cliparts.add(new FontClipart("Strawberry", Category.FOOD, font, "<", this));
        cliparts.add(new FontClipart("Frog", Category.ANIMALS, font, "'", this));
        cliparts.add(new FontClipart("Hamster", Category.ANIMALS, font, "*", this));
        cliparts.add(new FontClipart("Fish 1", Category.ANIMALS, font, "3", this));
        cliparts.add(new FontClipart("Polliwog", Category.ANIMALS, font, "A", this));
        cliparts.add(new FontClipart("Egg", Category.FOOD, font, "B", this));
        cliparts.add(new FontClipart("Fox", Category.ANIMALS, font, "D", this));
        cliparts.add(new FontClipart("Telephone", Category.ELECTRONICS, font, "F", this));
        cliparts.add(new FontClipart("Tram", Category.TRANSPORTATION, font, "G", this));
        cliparts.add(new FontClipart("Bus", Category.TRANSPORTATION, font, "H", this));
        cliparts.add(new FontClipart("Car", Category.TRANSPORTATION, font, "I", this));
        cliparts.add(new FontClipart("Umbrella", Category.WEATHER, font, "N", this));
        cliparts.add(new FontClipart("Plane", Category.TRANSPORTATION, font, "L", this));
        cliparts.add(new FontClipart("Sun", Category.WEATHER, font, "M", this));
        cliparts.add(new FontClipart("Cat", Category.ANIMALS, font, "P", this));
        cliparts.add(new FontClipart("Cat angry", Category.ANIMALS, font, "P", this));
        cliparts.add(new FontClipart("Octopus", Category.ANIMALS, font, "Q", this));
        cliparts.add(new FontClipart("Fish 2", Category.ANIMALS, font, "S", this));
        cliparts.add(new FontClipart("Ant", Category.ANIMALS, font, "T", this));
        cliparts.add(new FontClipart("Duck", Category.ANIMALS, font, "U", this));
        cliparts.add(new FontClipart("Duckling", Category.ANIMALS, font, "V", this));
        cliparts.add(new FontClipart("Mouse 1", Category.ANIMALS, font, "X", this));
        cliparts.add(new FontClipart("Mouse 2", Category.ANIMALS, font, "Y", this));
        cliparts.add(new FontClipart("Ghost", Category.MYTHICAL, font, "Z", this));
        cliparts.add(new FontClipart("Robot", Category.ELECTRONICS, font, "x", this));
        cliparts.add(new FontClipart("Moon", Category.WEATHER, font, "e", this));
        cliparts.add(new FontClipart("Cellphone", Category.ELECTRONICS, font, "f", this));
        cliparts.add(new FontClipart("Television", Category.ELECTRONICS, font, "h", this));
        cliparts.add(new FontClipart("Coffey", Category.FOOD, font, "r", this));
        cliparts.add(new FontClipart("Rain", Category.WEATHER, font, "n", this));
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

    @Override
    public String getLicense() {
        return "Free for commercial use";
    }
}
