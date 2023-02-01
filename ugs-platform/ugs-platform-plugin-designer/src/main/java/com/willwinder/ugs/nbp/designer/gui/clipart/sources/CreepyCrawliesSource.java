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
public class CreepyCrawliesSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public CreepyCrawliesSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, CreepyCrawliesSource.class.getResourceAsStream("/fonts/creepy-crawlies-font/CreepyCrawlies-GOxxy.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }


        cliparts.add(new FontClipart("Ant", Category.ANIMALS, font, "a", this));
        cliparts.add(new FontClipart("Wasp", Category.ANIMALS, font, "b", this));
        cliparts.add(new FontClipart("Fly", Category.ANIMALS, font, "c", this));
        cliparts.add(new FontClipart("Dragon fly", Category.ANIMALS, font, "d", this));
        cliparts.add(new FontClipart("Scorpion", Category.ANIMALS, font, "e", this));
        cliparts.add(new FontClipart("Frog", Category.ANIMALS, font, "f", this));
        cliparts.add(new FontClipart("Cockroach", Category.ANIMALS, font, "g", this));
        cliparts.add(new FontClipart("Centipede", Category.ANIMALS, font, "h", this));
        cliparts.add(new FontClipart("Spider", Category.ANIMALS, font, "i", this));
        cliparts.add(new FontClipart("Bug", Category.ANIMALS, font, "j", this));
        cliparts.add(new FontClipart("Snake", Category.ANIMALS, font, "k", this));
        cliparts.add(new FontClipart("Mantis", Category.ANIMALS, font, "l", this));
        cliparts.add(new FontClipart("Tick", Category.ANIMALS, font, "m", this));
        cliparts.add(new FontClipart("Centipede", Category.ANIMALS, font, "n", this));
        cliparts.add(new FontClipart("Wood louse", Category.ANIMALS, font, "o", this));
        cliparts.add(new FontClipart("Caterpillar", Category.ANIMALS, font.deriveFont(font.getSize() * 0.7f), "p", this));
        cliparts.add(new FontClipart("Mosquito", Category.ANIMALS, font, "q", this));
        cliparts.add(new FontClipart("Grass hopper", Category.ANIMALS, font.deriveFont(font.getSize() * 0.8f), "r", this));
        cliparts.add(new FontClipart("Spider 2", Category.ANIMALS, font, "s", this));
        cliparts.add(new FontClipart("Flee", Category.ANIMALS, font, "t", this));
        cliparts.add(new FontClipart("Beetle", Category.ANIMALS, font, "u", this));
        cliparts.add(new FontClipart("Ant 2", Category.ANIMALS, font, "v", this));
        cliparts.add(new FontClipart("Worm", Category.ANIMALS, font, "w", this));
        cliparts.add(new FontClipart("Slug", Category.ANIMALS, font, "x", this));
        cliparts.add(new FontClipart("Fly 2", Category.ANIMALS, font, "y", this));
        cliparts.add(new FontClipart("Gecko", Category.ANIMALS, font, "z", this));
        cliparts.add(new FontClipart("Crow", Category.ANIMALS, font, "0", this));
        cliparts.add(new FontClipart("Rat", Category.ANIMALS, font, "1", this));
        cliparts.add(new FontClipart("Frog", Category.ANIMALS, font, "2", this));
        cliparts.add(new FontClipart("Worm 2", Category.ANIMALS, font, "3", this));
        cliparts.add(new FontClipart("Spider 4", Category.ANIMALS, font, "4", this));
        cliparts.add(new FontClipart("Tick 2", Category.ANIMALS, font, "5", this));
        cliparts.add(new FontClipart("Raven", Category.ANIMALS, font, "6", this));
        cliparts.add(new FontClipart("Mouse", Category.ANIMALS, font.deriveFont(font.getSize() * 0.8f), "7", this));
        cliparts.add(new FontClipart("Iguana", Category.ANIMALS, font.deriveFont(font.getSize() * 0.8f), "8", this));
        cliparts.add(new FontClipart("Frog", Category.ANIMALS, font, "9", this));
    }

    @Override
    public String getName() {
        return "Creepy Crawlies";
    }

    @Override
    public String getCredits() {
        return "Iconian Fonts";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/creepy-crawlies-font-f86435";
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
