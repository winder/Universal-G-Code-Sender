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
public class EvilzSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public EvilzSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, FredokaSource.class.getResourceAsStream("/fonts/evilz/Evilz-JJ1a.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Could not load font", e);
        }

        cliparts.add(new FontClipart("ghost", Category.ANIMALS, font, "*"));
        cliparts.add(new FontClipart("cat-1", Category.ANIMALS, font, "+"));
        cliparts.add(new FontClipart("witch", Category.ANIMALS, font, ","));
        cliparts.add(new FontClipart("cat-2", Category.ANIMALS, font, "-"));
        cliparts.add(new FontClipart("joker-1", Category.ANIMALS, font, "@"));
        cliparts.add(new FontClipart("snake-1", Category.ANIMALS, font, "["));
        cliparts.add(new FontClipart("prisoner", Category.ANIMALS, font, "\\"));
        cliparts.add(new FontClipart("snake-2", Category.ANIMALS, font, "]"));
        cliparts.add(new FontClipart("joker-2", Category.ANIMALS, font, "`"));
        cliparts.add(new FontClipart("snakes", Category.ANIMALS, font, "{"));
        cliparts.add(new FontClipart("track", Category.ANIMALS, font, "="));
        cliparts.add(new FontClipart("axe", Category.TOOLS, font, "\""));
        cliparts.add(new FontClipart("morning star", Category.TOOLS, font, "#"));
        cliparts.add(new FontClipart("medicine", Category.FOOD, font, "&"));
        cliparts.add(new FontClipart("love potion", Category.FOOD, font, "_"));
        cliparts.add(new FontClipart("key 1", Category.UNSORTED, font, "."));
        cliparts.add(new FontClipart("key 2", Category.UNSORTED, font, "/"));
        cliparts.add(new FontClipart("house haunted", Category.ANIMALS, font, "~"));
        cliparts.add(new FontClipart("key 2", Category.SIGNS_AND_SYMBOLS, font, "/"));
        cliparts.add(new FontClipart("grave", Category.SIGNS_AND_SYMBOLS, font, "^"));
        cliparts.add(new FontClipart("witch hat", Category.UNSORTED, font, "!"));
        cliparts.add(new FontClipart("sword", Category.TOOLS, font, "$"));
        cliparts.add(new FontClipart("swords", Category.TOOLS, font, "4"));
        cliparts.add(new FontClipart("bag with money", Category.UNSORTED, font, "5"));
        cliparts.add(new FontClipart("poison", Category.FOOD, font, "6"));
        cliparts.add(new FontClipart("candle", Category.UNSORTED, font, "7"));
        cliparts.add(new FontClipart("feather", Category.UNSORTED, font, "8"));
        cliparts.add(new FontClipart("bomb", Category.TOOLS, font, "9"));
        cliparts.add(new FontClipart("cross", Category.SIGNS_AND_SYMBOLS, font, "C"));
        cliparts.add(new FontClipart("skull and eye patch", Category.SIGNS_AND_SYMBOLS, font, "D"));
        cliparts.add(new FontClipart("heart and cross", Category.SIGNS_AND_SYMBOLS, font, "E"));
        cliparts.add(new FontClipart("heart and bones", Category.SIGNS_AND_SYMBOLS, font, "F"));
        cliparts.add(new FontClipart("star and bones", Category.SIGNS_AND_SYMBOLS, font, "G"));
        cliparts.add(new FontClipart("heart and wings", Category.SIGNS_AND_SYMBOLS, font, "H"));
        cliparts.add(new FontClipart("pumpkin", Category.ANIMALS, font, "I"));
        cliparts.add(new FontClipart("mouse", Category.ANIMALS, font, "J"));
        cliparts.add(new FontClipart("wolf", Category.ANIMALS, font, "K"));
        cliparts.add(new FontClipart("stich man 1", Category.ANIMALS, font, "L"));
        cliparts.add(new FontClipart("bat", Category.ANIMALS, font, "N"));
        cliparts.add(new FontClipart("raven", Category.ANIMALS, font, "O"));
        cliparts.add(new FontClipart("salamander", Category.ANIMALS, font, "P"));
        cliparts.add(new FontClipart("small ghost", Category.ANIMALS, font, "Q"));
        cliparts.add(new FontClipart("reaper", Category.ANIMALS, font, "R"));
        cliparts.add(new FontClipart("casket", Category.SIGNS_AND_SYMBOLS, font, "S"));
        cliparts.add(new FontClipart("elf", Category.ANIMALS, font, "T"));
        cliparts.add(new FontClipart("spider", Category.ANIMALS, font, "U"));
        cliparts.add(new FontClipart("bat flying 1", Category.ANIMALS, font, "V"));
        cliparts.add(new FontClipart("devil-1", Category.ANIMALS, font, "W"));
        cliparts.add(new FontClipart("devil-2", Category.ANIMALS, font, "X"));
        cliparts.add(new FontClipart("rabbit", Category.ANIMALS, font, "Y"));
        cliparts.add(new FontClipart("rabbit ghost", Category.ANIMALS, font, "Z"));
        cliparts.add(new FontClipart("dead cow", Category.SIGNS_AND_SYMBOLS, font, "a"));
        cliparts.add(new FontClipart("skull and bones", Category.SIGNS_AND_SYMBOLS, font, "b"));
        cliparts.add(new FontClipart("bones", Category.SIGNS_AND_SYMBOLS, font, "c"));
        cliparts.add(new FontClipart("dead king", Category.SIGNS_AND_SYMBOLS, font, "d"));
        cliparts.add(new FontClipart("heart and lightning", Category.SIGNS_AND_SYMBOLS, font, "e"));
        cliparts.add(new FontClipart("heart and skull", Category.SIGNS_AND_SYMBOLS, font, "f"));
        cliparts.add(new FontClipart("star and skull", Category.SIGNS_AND_SYMBOLS, font, "g"));
        cliparts.add(new FontClipart("pentagram", Category.SIGNS_AND_SYMBOLS, font, "h"));
        cliparts.add(new FontClipart("pumpkin", Category.ANIMALS, font, "i"));
        cliparts.add(new FontClipart("stitch man 2", Category.ANIMALS, font, "l"));
        cliparts.add(new FontClipart("thorn bush", Category.PLANTS, font, "m"));
        cliparts.add(new FontClipart("bat flying 2", Category.ANIMALS, font, "n"));
        cliparts.add(new FontClipart("spider web", Category.ANIMALS, font, "u"));
    }

    @Override
    public String getName() {
        return "Evilz Font";
    }

    @Override
    public String getCredits() {
        return "Sakurai Nan";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/evilz-font-f4530";
    }

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
