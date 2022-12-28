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
public class MythicalSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public MythicalSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, MythicalSource.class.getResourceAsStream("/fonts/mythical/MythicalAndHopliteNoodgies-gwOP.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "A"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "B"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "C"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "D"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "E"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "F"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "G"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "H"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "I"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "J"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "K"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "L"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "M"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "N"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "O"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "P"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "Q"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "R"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "S"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "T"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "U"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "V"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "W"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "X"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "Y"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "Z"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "a"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "b"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "c"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "d"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "e"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "f"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "g"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "h"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "i"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "j"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "k"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "l"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "m"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "n"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "o"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "p"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "q"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "r"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "s"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "t"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "u"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "v"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "w"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "x"));
        cliparts.add(new FontClipart("mythical 1", Category.MYTHICAL, font, "y"));
    }

    @Override
    public String getName() {
        return "Mythical & Hoplite Noodgies Font\n";
    }

    @Override
    public String getCredits() {
        return "Walter Velez";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/mythical-and-hoplite-noodgies-font-f3780";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
