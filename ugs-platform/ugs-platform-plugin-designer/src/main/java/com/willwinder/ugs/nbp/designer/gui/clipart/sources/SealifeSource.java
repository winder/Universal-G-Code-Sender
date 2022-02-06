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
import com.willwinder.ugs.nbp.designer.gui.clipart.FontClipart;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class SealifeSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public SealifeSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, SealifeSource.class.getResourceAsStream("/fonts/sealife/Sealife-o140.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Could not load font", e);
        }

        cliparts.add(new FontClipart("turtle", Category.ANIMALS, font.deriveFont(font.getSize() * 0.7f), "A"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "B"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font.deriveFont(font.getSize() * 0.4f), "C"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "D"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "E"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "F"));
        cliparts.add(new FontClipart("seahorse", Category.ANIMALS, font, "G"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "H"));
        cliparts.add(new FontClipart("mythical 2", Category.MYTHICAL, font, "I"));
        cliparts.add(new FontClipart("shrimp", Category.ANIMALS, font, "J"));
        cliparts.add(new FontClipart("flower", Category.DECORATIONS, font.deriveFont(font.getSize() * 0.5f), "K"));
        cliparts.add(new FontClipart("shell 1", Category.ANIMALS, font, "L"));
        cliparts.add(new FontClipart("shell", Category.ANIMALS, font, "M"));
        cliparts.add(new FontClipart("shell 1", Category.ANIMALS, font, "N"));
        cliparts.add(new FontClipart("shell 1", Category.ANIMALS, font, "O"));
        cliparts.add(new FontClipart("shell 1", Category.ANIMALS, font, "P"));
        cliparts.add(new FontClipart("shell 1", Category.ANIMALS, font, "Q"));
        cliparts.add(new FontClipart("octopus", Category.ANIMALS, font.deriveFont(font.getSize() * 0.9f), "R"));
        cliparts.add(new FontClipart("lobster", Category.ANIMALS, font, "S"));
        cliparts.add(new FontClipart("shrimp", Category.ANIMALS, font.deriveFont(font.getSize() * 0.8f), "T"));
        cliparts.add(new FontClipart("crab", Category.ANIMALS, font.deriveFont(font.getSize() * 0.8f), "U"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "V"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "W"));
        cliparts.add(new FontClipart("decorations", Category.DECORATIONS, font, "X"));
    }

    @Override
    public String getName() {
        return "Sealife";
    }

    @Override
    public String getCredits() {
        return "Sassy Graphics";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/sealife-font-f9793";
    }

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
