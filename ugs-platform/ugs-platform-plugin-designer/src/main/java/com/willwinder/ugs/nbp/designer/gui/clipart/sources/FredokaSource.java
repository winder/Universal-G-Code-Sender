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
public class FredokaSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public FredokaSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, FredokaSource.class.getResourceAsStream("/fonts/fredoka-one/Fredoka-dingbats.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Elephant", Category.ANIMALS, font, "\u0061", this));
        cliparts.add(new FontClipart("Bird", Category.ANIMALS, font, "\u0062", this));
        cliparts.add(new FontClipart("Fish", Category.ANIMALS, font, "\u0063", this));
        cliparts.add(new FontClipart("Owl", Category.ANIMALS, font, "\u0064", this));
        cliparts.add(new FontClipart("Cat", Category.ANIMALS, font, "\u0065", this));
        cliparts.add(new FontClipart("Butterfly", Category.ANIMALS, font, "\u0066", this));
        cliparts.add(new FontClipart("Rabbit", Category.ANIMALS, font, "\u0067", this));
        cliparts.add(new FontClipart("Fishbowl", Category.ANIMALS, font, "\u0068", this));
        cliparts.add(new FontClipart("Mouse", Category.ANIMALS, font, "\u0069", this));
        cliparts.add(new FontClipart("Ornament-left", Category.DECORATIONS, font, "\u0028", this));
        cliparts.add(new FontClipart("Ornament-right", Category.DECORATIONS, font, "\u0029", this));
        cliparts.add(new FontClipart("Ornament-1", Category.DECORATIONS, font, "\u0031", this));
        cliparts.add(new FontClipart("Ornament-2", Category.DECORATIONS, font, "\u0032", this));
        cliparts.add(new FontClipart("Ornament-3", Category.DECORATIONS, font, "\u0033", this));
        cliparts.add(new FontClipart("Ornament-4", Category.DECORATIONS, font, "\u0034", this));
        cliparts.add(new FontClipart("Ornament-5", Category.DECORATIONS, font, "\u0035", this));
        cliparts.add(new FontClipart("Heart", Category.DECORATIONS, font, "\u0036", this));
        cliparts.add(new FontClipart("Ornament-7", Category.DECORATIONS, font, "\u0038", this));
        cliparts.add(new FontClipart("Ornament-8", Category.DECORATIONS, font, "\u0039", this));
        cliparts.add(new FontClipart("Ornament-9", Category.DECORATIONS, font, "\u003C", this));
        cliparts.add(new FontClipart("Flower-1", Category.PLANTS, font, "\u0041", this));
        cliparts.add(new FontClipart("Flower-2", Category.PLANTS, font, "\u0042", this));
        cliparts.add(new FontClipart("Flower-3", Category.PLANTS, font, "\u0043", this));
        cliparts.add(new FontClipart("Leaf", Category.PLANTS, font, "\u0044", this));
        cliparts.add(new FontClipart("Barley", Category.PLANTS, font, "\u0045", this));
        cliparts.add(new FontClipart("Rye", Category.PLANTS, font, "\u0046", this));
        cliparts.add(new FontClipart("Yin Yang", Category.SIGNS_AND_SYMBOLS, font, "\u0048", this));
        cliparts.add(new FontClipart("Knot", Category.DECORATIONS, font, "\u004B", this));
        cliparts.add(new FontClipart("Flower", Category.DECORATIONS, font, "\u0054", this));
        cliparts.add(new FontClipart("Old-phone", Category.ELECTRONICS, font, "\u004A", this));
        cliparts.add(new FontClipart("Cellphone", Category.ELECTRONICS, font, "\u0051", this));
        cliparts.add(new FontClipart("TV", Category.ELECTRONICS, font, "\u0052", this));
    }

    @Override
    public String getName() {
        return "Fredoka One";
    }

    @Override
    public String getCredits() {
        return "Milena Brandao";
    }

    @Override
    public String getUrl() {
        return "https://www.1001fonts.com/fredoka-one-font.html";
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
