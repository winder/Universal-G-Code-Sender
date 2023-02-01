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

        cliparts.add(new FontClipart("Chariot", Category.MYTHICAL, font, "A", this));
        cliparts.add(new FontClipart("Musician", Category.PEOPLE_AND_CHARACTERS, font, "D", this));
        cliparts.add(new FontClipart("Feet", Category.SIGNS_AND_SYMBOLS, font, "E", this));
        cliparts.add(new FontClipart("Warrior 1", Category.PEOPLE_AND_CHARACTERS, font, "J", this));
        cliparts.add(new FontClipart("Warrior 2", Category.PEOPLE_AND_CHARACTERS, font, "K", this));
        cliparts.add(new FontClipart("Warrior 3", Category.PEOPLE_AND_CHARACTERS, font, "L", this));
        cliparts.add(new FontClipart("Warrior 4", Category.PEOPLE_AND_CHARACTERS, font, "M", this));
        cliparts.add(new FontClipart("Warrior 5", Category.PEOPLE_AND_CHARACTERS, font, "N", this));
        cliparts.add(new FontClipart("Warrior 6", Category.PEOPLE_AND_CHARACTERS, font, "O", this));
        cliparts.add(new FontClipart("Warrior 7", Category.PEOPLE_AND_CHARACTERS, font, "P", this));
        cliparts.add(new FontClipart("Warrior 8", Category.PEOPLE_AND_CHARACTERS, font, "G", this));
        cliparts.add(new FontClipart("Warrior 9", Category.PEOPLE_AND_CHARACTERS, font, "H", this));
        cliparts.add(new FontClipart("Warrior 10", Category.PEOPLE_AND_CHARACTERS, font, "I", this));
        cliparts.add(new FontClipart("Warrior 11", Category.PEOPLE_AND_CHARACTERS, font, "F", this));
        cliparts.add(new FontClipart("Warrior 12", Category.PEOPLE_AND_CHARACTERS, font, "C", this));
        cliparts.add(new FontClipart("Warrior 13", Category.PEOPLE_AND_CHARACTERS, font, "B", this));
        cliparts.add(new FontClipart("Snake", Category.ANIMALS, font, "Q", this));
        cliparts.add(new FontClipart("Sphinx 1", Category.MYTHICAL, font, "R", this));
        cliparts.add(new FontClipart("Antelope", Category.ANIMALS, font, "S", this));
        cliparts.add(new FontClipart("Lion", Category.ANIMALS, font, "T", this));
        cliparts.add(new FontClipart("Pegasus", Category.MYTHICAL, font, "U", this));
        cliparts.add(new FontClipart("Leopard", Category.ANIMALS, font, "V", this));
        cliparts.add(new FontClipart("Rooster", Category.ANIMALS, font, "W", this));
        cliparts.add(new FontClipart("Octopus", Category.ANIMALS, font, "X", this));
        cliparts.add(new FontClipart("Face plate", Category.SIGNS_AND_SYMBOLS, font, "Y", this));
        cliparts.add(new FontClipart("Dolphin", Category.ANIMALS, font, "Z", this));
        cliparts.add(new FontClipart("Pegasus", Category.MYTHICAL, font, "a", this));
        cliparts.add(new FontClipart("?", Category.SIGNS_AND_SYMBOLS, font, "b", this));
        cliparts.add(new FontClipart("Minotaur", Category.MYTHICAL, font, "c", this));
        cliparts.add(new FontClipart("Skull and helment", Category.SIGNS_AND_SYMBOLS, font, "d", this));
        cliparts.add(new FontClipart("Centaur", Category.MYTHICAL, font, "e", this));
        cliparts.add(new FontClipart("Cyclops", Category.MYTHICAL, font, "f", this));
        cliparts.add(new FontClipart("Medusa", Category.MYTHICAL, font, "g", this));
        cliparts.add(new FontClipart("Mermaid", Category.MYTHICAL, font, "h", this));
        cliparts.add(new FontClipart("Winged man", Category.PEOPLE_AND_CHARACTERS, font, "i", this));
        cliparts.add(new FontClipart("Sphinx 2", Category.MYTHICAL, font, "j", this));
        cliparts.add(new FontClipart("Cherub", Category.MYTHICAL, font, "k", this));
        cliparts.add(new FontClipart("Ship ancient greece", Category.TRANSPORTATION, font, "l", this));
        cliparts.add(new FontClipart("Runner", Category.PEOPLE_AND_CHARACTERS, font, "m", this));
        cliparts.add(new FontClipart("mythical 41", Category.MYTHICAL, font, "n", this));
        cliparts.add(new FontClipart("mythical 42", Category.MYTHICAL, font, "o", this));
        cliparts.add(new FontClipart("mythical 43", Category.MYTHICAL, font, "p", this));
        cliparts.add(new FontClipart("Worker", Category.PEOPLE_AND_CHARACTERS, font, "q", this));
        cliparts.add(new FontClipart("Cherberus", Category.MYTHICAL, font, "r", this));
        cliparts.add(new FontClipart("Harp", Category.TOOLS, font, "s", this));
        cliparts.add(new FontClipart("mythical 47", Category.MYTHICAL, font, "t", this));
        cliparts.add(new FontClipart("mythical 48", Category.MYTHICAL, font, "u", this));
        cliparts.add(new FontClipart("Zeus", Category.MYTHICAL, font, "v", this));
        cliparts.add(new FontClipart("Trojan horse", Category.MYTHICAL, font, "w", this));
        cliparts.add(new FontClipart("Griffin", Category.MYTHICAL, font, "x", this));
        cliparts.add(new FontClipart("Harpies", Category.MYTHICAL, font, "y", this));
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

    @Override
    public String getLicense() {
        return "Free for commercial use";
    }
}
