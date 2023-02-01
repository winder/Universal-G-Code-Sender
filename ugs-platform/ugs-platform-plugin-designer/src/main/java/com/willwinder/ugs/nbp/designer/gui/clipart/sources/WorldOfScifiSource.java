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
public class WorldOfScifiSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public WorldOfScifiSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, WorldOfScifiSource.class.getResourceAsStream("/fonts/world-of-sci-fi-font/WorldOfScifi-K7DvZ.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Stargate", Category.SIGNS_AND_SYMBOLS, font, "a", this));
        cliparts.add(new FontClipart("Cylon", Category.PEOPLE_AND_CHARACTERS, font, "c", this));
        cliparts.add(new FontClipart("Dalek", Category.PEOPLE_AND_CHARACTERS, font, "d", this));
        cliparts.add(new FontClipart("Star Wars - Storm trooper", Category.PEOPLE_AND_CHARACTERS, font, "e", this));
        cliparts.add(new FontClipart("Star Wars - Boba Fett", Category.PEOPLE_AND_CHARACTERS, font, "f", this));
        cliparts.add(new FontClipart("Alien", Category.SIGNS_AND_SYMBOLS, font, "g", this));
        cliparts.add(new FontClipart("Borg", Category.PEOPLE_AND_CHARACTERS, font, "h", this));
        cliparts.add(new FontClipart("Harvester", Category.PEOPLE_AND_CHARACTERS, font, "i", this));
        cliparts.add(new FontClipart("Superman", Category.SIGNS_AND_SYMBOLS, font, "j", this));
        cliparts.add(new FontClipart("Star Trek - Klingon", Category.SIGNS_AND_SYMBOLS, font, "k", this));
        cliparts.add(new FontClipart("Live long and prosper", Category.SIGNS_AND_SYMBOLS, font, "l", this));
        cliparts.add(new FontClipart("Alien", Category.PEOPLE_AND_CHARACTERS, font, "n", this));
        cliparts.add(new FontClipart("TRON light cycle", Category.TRANSPORTATION, font, "o", this));
        cliparts.add(new FontClipart("Star Trek - Phaser", Category.TOOLS, font, "p", this));
        cliparts.add(new FontClipart("Alien vs Predator", Category.SIGNS_AND_SYMBOLS, font, "q", this));
        cliparts.add(new FontClipart("Robinson Robot.", Category.PEOPLE_AND_CHARACTERS, font, "r", this));
        cliparts.add(new FontClipart("Star Wars", Category.SIGNS_AND_SYMBOLS, font, "s", this));
        cliparts.add(new FontClipart("Star Trek", Category.SIGNS_AND_SYMBOLS, font, "t", this));
        cliparts.add(new FontClipart("Blaster", Category.TOOLS, font, "u", this));
        cliparts.add(new FontClipart("Star Trek - Spock", Category.PEOPLE_AND_CHARACTERS, font, "v", this));
        cliparts.add(new FontClipart("Star Wars - Darth Vader", Category.PEOPLE_AND_CHARACTERS, font, "w", this));
        cliparts.add(new FontClipart("?", Category.SIGNS_AND_SYMBOLS, font, "y", this));
        cliparts.add(new FontClipart("Star Trek - Bajor", Category.SIGNS_AND_SYMBOLS, font, "B", this));
        cliparts.add(new FontClipart("Petnagon", Category.SIGNS_AND_SYMBOLS, font, "C", this));
        cliparts.add(new FontClipart("E.T.", Category.PEOPLE_AND_CHARACTERS, font, "E", this));
        cliparts.add(new FontClipart("Godzilla", Category.PEOPLE_AND_CHARACTERS, font, "G", this));
        cliparts.add(new FontClipart("Star Trek - Cardassian", Category.SIGNS_AND_SYMBOLS, font, "H", this));
        cliparts.add(new FontClipart("Star Wars - Dark empire", Category.SIGNS_AND_SYMBOLS, font, "I", this));
        cliparts.add(new FontClipart("Star Wars - Jedi Order", Category.SIGNS_AND_SYMBOLS, font, "J", this));
        cliparts.add(new FontClipart("Star Trek - Klingon", Category.SIGNS_AND_SYMBOLS, font, "K", this));
        cliparts.add(new FontClipart("Star Trek - Terran empire", Category.SIGNS_AND_SYMBOLS, font, "L", this));
        cliparts.add(new FontClipart("Star Wars - Mandalorians", Category.SIGNS_AND_SYMBOLS, font, "M", this));
        cliparts.add(new FontClipart("Star Wars - Sand warrior", Category.PEOPLE_AND_CHARACTERS, font, "O", this));
        cliparts.add(new FontClipart("Jupiter", Category.UNSORTED, font, "P", this));
        cliparts.add(new FontClipart("Star Wars - Strike fighter", Category.TRANSPORTATION, font, "Q", this));
        cliparts.add(new FontClipart("Star Wars - Rebel Alliance", Category.SIGNS_AND_SYMBOLS, font, "R", this));
        cliparts.add(new FontClipart("???", Category.SIGNS_AND_SYMBOLS, font.deriveFont(font.getSize() * 0.8f), "S", this));
        cliparts.add(new FontClipart("Star Trek - Star Federation", Category.SIGNS_AND_SYMBOLS, font, "U", this));
        cliparts.add(new FontClipart("??", Category.SIGNS_AND_SYMBOLS, font, "V", this));
        cliparts.add(new FontClipart("Star Wars - ATAT", Category.TRANSPORTATION, font, "W", this));
        cliparts.add(new FontClipart("Star Wars - C3P0", Category.PEOPLE_AND_CHARACTERS, font, "X", this));
        cliparts.add(new FontClipart("Dave", Category.PEOPLE_AND_CHARACTERS, font, "1", this));
        cliparts.add(new FontClipart("Star Wars - R2D2", Category.PEOPLE_AND_CHARACTERS, font, "2", this));
        cliparts.add(new FontClipart("Arachnid", Category.PEOPLE_AND_CHARACTERS, font, "3", this));
        cliparts.add(new FontClipart("Independence day", Category.SIGNS_AND_SYMBOLS, font, "4", this));
        cliparts.add(new FontClipart("Babylon 5", Category.SIGNS_AND_SYMBOLS, font, "5", this));
        cliparts.add(new FontClipart("Buck Rogers - Twiki", Category.PEOPLE_AND_CHARACTERS, font, "6", this));
        cliparts.add(new FontClipart("Space man", Category.PEOPLE_AND_CHARACTERS, font, "8", this));
        cliparts.add(new FontClipart("Moonbase Alpha", Category.SIGNS_AND_SYMBOLS, font, "9", this));
        cliparts.add(new FontClipart("Star Wars - Blaster", Category.TOOLS, font.deriveFont(font.getSize() * 0.8f), "0", this));
    }

    @Override
    public String getName() {
        return "World of Sci Fi Font";
    }

    @Override
    public String getCredits() {
        return "Iconian Fonts";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/world-of-sci-fi-font-f87545";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    public String getLicense() {
        return "Free for non-commercial use";
    }
}
