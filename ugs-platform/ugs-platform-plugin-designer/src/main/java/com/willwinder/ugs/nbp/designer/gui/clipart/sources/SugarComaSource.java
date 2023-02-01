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
public class SugarComaSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public SugarComaSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, SugarComaSource.class.getResourceAsStream("/fonts/sugar-coma-font/SugarComa-nVV.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Soda", Category.FOOD, font, "A", this));
        cliparts.add(new FontClipart("Cookie", Category.FOOD, font, "B", this));
        cliparts.add(new FontClipart("Muffin", Category.FOOD, font, "C", this));
        cliparts.add(new FontClipart("Cookies", Category.FOOD, font, "D", this));
        cliparts.add(new FontClipart("Hot coco", Category.FOOD, font, "E", this));
        cliparts.add(new FontClipart("Ice cream 2", Category.FOOD, font, "F", this));
        cliparts.add(new FontClipart("Chocolate kiss", Category.FOOD, font, "G", this));
        cliparts.add(new FontClipart("Lollipop 2", Category.FOOD, font, "H", this));
        cliparts.add(new FontClipart("M&M", Category.FOOD, font, "I", this));
        cliparts.add(new FontClipart("Pie", Category.FOOD, font, "J", this));
        cliparts.add(new FontClipart("Ice cream", Category.FOOD, font, "K", this));
        cliparts.add(new FontClipart("Milk shake", Category.FOOD, font, "L", this));
        cliparts.add(new FontClipart("Strawberry", Category.FOOD, font, "M", this));
        cliparts.add(new FontClipart("Food 12", Category.FOOD, font, "N", this));
        cliparts.add(new FontClipart("Chocolate bar", Category.FOOD, font.deriveFont(font.getSize() * 0.8f), "O", this));
        cliparts.add(new FontClipart("Cake", Category.FOOD, font, "P", this));
        cliparts.add(new FontClipart("Doughnut", Category.FOOD, font, "Q", this));
        cliparts.add(new FontClipart("Candy 3", Category.FOOD, font, "R", this));
        cliparts.add(new FontClipart("Soda 3", Category.FOOD, font, "S", this));
        cliparts.add(new FontClipart("Candy", Category.FOOD, font, "T", this));
        cliparts.add(new FontClipart("Sugar", Category.FOOD, font, "U", this));
        cliparts.add(new FontClipart("Lollipop", Category.FOOD, font, "V", this));
        cliparts.add(new FontClipart("Ice cream cone", Category.FOOD, font, "W", this));
        cliparts.add(new FontClipart("Soda 2", Category.FOOD, font, "X", this));
        cliparts.add(new FontClipart("Cookie 2", Category.FOOD, font, "Y", this));
    }

    @Override
    public String getName() {
        return "Sugar Coma";
    }

    @Override
    public String getCredits() {
        return "Blue Vinyl";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/sugar-coma-font-f980";
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
