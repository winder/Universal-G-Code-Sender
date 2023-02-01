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
public class EvilzSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public EvilzSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, FredokaSource.class.getResourceAsStream("/fonts/evilz/Evilz-JJ1a.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Ghost", Category.MYTHICAL, font, "*", this));
        cliparts.add(new FontClipart("Cat-1", Category.ANIMALS, font, "+", this));
        cliparts.add(new FontClipart("Witch", Category.MYTHICAL, font, ",", this));
        cliparts.add(new FontClipart("Cat-2", Category.ANIMALS, font, "-", this));
        cliparts.add(new FontClipart("Joker-1", Category.PEOPLE_AND_CHARACTERS, font, "@", this));
        cliparts.add(new FontClipart("Snake-1", Category.ANIMALS, font, "[", this));
        cliparts.add(new FontClipart("Prisoner", Category.PEOPLE_AND_CHARACTERS, font, "\\", this));
        cliparts.add(new FontClipart("Snake-2", Category.ANIMALS, font, "]", this));
        cliparts.add(new FontClipart("Joker-2", Category.PEOPLE_AND_CHARACTERS, font, "`", this));
        cliparts.add(new FontClipart("Snakes", Category.ANIMALS, font, "{", this));
        cliparts.add(new FontClipart("Track", Category.ANIMALS, font, "=", this));
        cliparts.add(new FontClipart("Axe", Category.TOOLS, font, "\"", this));
        cliparts.add(new FontClipart("Morning star", Category.TOOLS, font, "#", this));
        cliparts.add(new FontClipart("Medicine", Category.FOOD, font, "&", this));
        cliparts.add(new FontClipart("Love potion", Category.FOOD, font, "_", this));
        cliparts.add(new FontClipart("Key 1", Category.UNSORTED, font, ".", this));
        cliparts.add(new FontClipart("Key 2", Category.UNSORTED, font, "/", this));
        cliparts.add(new FontClipart("House haunted", Category.MYTHICAL, font, "~", this));
        cliparts.add(new FontClipart("Key 2", Category.SIGNS_AND_SYMBOLS, font, "/", this));
        cliparts.add(new FontClipart("Grave", Category.SIGNS_AND_SYMBOLS, font, "^", this));
        cliparts.add(new FontClipart("Witch hat", Category.UNSORTED, font, "!", this));
        cliparts.add(new FontClipart("Sword", Category.TOOLS, font, "$", this));
        cliparts.add(new FontClipart("Swords", Category.TOOLS, font, "4", this));
        cliparts.add(new FontClipart("Bag with money", Category.UNSORTED, font, "5", this));
        cliparts.add(new FontClipart("Poison", Category.FOOD, font, "6", this));
        cliparts.add(new FontClipart("Candle", Category.UNSORTED, font, "7", this));
        cliparts.add(new FontClipart("Feather", Category.UNSORTED, font, "8", this));
        cliparts.add(new FontClipart("Bomb", Category.TOOLS, font, "9", this));
        cliparts.add(new FontClipart("Cross", Category.SIGNS_AND_SYMBOLS, font, "C", this));
        cliparts.add(new FontClipart("Skull and eye patch", Category.SIGNS_AND_SYMBOLS, font, "D", this));
        cliparts.add(new FontClipart("Heart and cross", Category.SIGNS_AND_SYMBOLS, font, "E", this));
        cliparts.add(new FontClipart("Heart and bones", Category.SIGNS_AND_SYMBOLS, font, "F", this));
        cliparts.add(new FontClipart("Star and bones", Category.SIGNS_AND_SYMBOLS, font, "G", this));
        cliparts.add(new FontClipart("Heart and wings", Category.SIGNS_AND_SYMBOLS, font, "H", this));
        cliparts.add(new FontClipart("Pumpkin", Category.MYTHICAL, font, "I", this));
        cliparts.add(new FontClipart("Mouse", Category.ANIMALS, font, "J", this));
        cliparts.add(new FontClipart("Wolf", Category.ANIMALS, font, "K", this));
        cliparts.add(new FontClipart("Stitch man 1", Category.MYTHICAL, font, "L", this));
        cliparts.add(new FontClipart("Bat", Category.ANIMALS, font, "N", this));
        cliparts.add(new FontClipart("Raven", Category.ANIMALS, font, "O", this));
        cliparts.add(new FontClipart("Salamander", Category.ANIMALS, font, "P", this));
        cliparts.add(new FontClipart("Small ghost", Category.MYTHICAL, font, "Q", this));
        cliparts.add(new FontClipart("Reaper", Category.MYTHICAL, font, "R", this));
        cliparts.add(new FontClipart("Casket", Category.SIGNS_AND_SYMBOLS, font, "S", this));
        cliparts.add(new FontClipart("Elf", Category.MYTHICAL, font, "T", this));
        cliparts.add(new FontClipart("Spider", Category.ANIMALS, font, "U", this));
        cliparts.add(new FontClipart("Bat flying 1", Category.ANIMALS, font, "V", this));
        cliparts.add(new FontClipart("Devil-1", Category.MYTHICAL, font, "W", this));
        cliparts.add(new FontClipart("Devil-2", Category.MYTHICAL, font, "X", this));
        cliparts.add(new FontClipart("Rabbit", Category.ANIMALS, font, "Y", this));
        cliparts.add(new FontClipart("Rabbit ghost", Category.MYTHICAL, font, "Z", this));
        cliparts.add(new FontClipart("Dead cow", Category.SIGNS_AND_SYMBOLS, font, "a", this));
        cliparts.add(new FontClipart("Skull and bones", Category.SIGNS_AND_SYMBOLS, font, "b", this));
        cliparts.add(new FontClipart("Bones", Category.SIGNS_AND_SYMBOLS, font, "c", this));
        cliparts.add(new FontClipart("Dead king", Category.SIGNS_AND_SYMBOLS, font, "d", this));
        cliparts.add(new FontClipart("Heart and lightning", Category.SIGNS_AND_SYMBOLS, font, "e", this));
        cliparts.add(new FontClipart("Heart and skull", Category.SIGNS_AND_SYMBOLS, font, "f", this));
        cliparts.add(new FontClipart("Star and skull", Category.SIGNS_AND_SYMBOLS, font, "g", this));
        cliparts.add(new FontClipart("Pentagram", Category.SIGNS_AND_SYMBOLS, font, "h", this));
        cliparts.add(new FontClipart("Pumpkin", Category.MYTHICAL, font, "i", this));
        cliparts.add(new FontClipart("Stitch man 2", Category.MYTHICAL, font, "l", this));
        cliparts.add(new FontClipart("Thorn bush", Category.PLANTS, font, "m", this));
        cliparts.add(new FontClipart("Bat flying 2", Category.ANIMALS, font, "n", this));
        cliparts.add(new FontClipart("Spider web", Category.ANIMALS, font, "u", this));
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
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    public String getLicense() {
        return "Free for commercial use";
    }
}
