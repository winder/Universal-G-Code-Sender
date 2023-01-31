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
public class GardenSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public GardenSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, GardenSource.class.getResourceAsStream("/fonts/garden/garden.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Plant", Category.PLANTS, font, "A", this));
        cliparts.add(new FontClipart("Ladder trimming", Category.PEOPLE_AND_CHARACTERS, font, "B", this));
        cliparts.add(new FontClipart("Mowing lawn", Category.PEOPLE_AND_CHARACTERS, font, "C", this));
        cliparts.add(new FontClipart("Raking 1", Category.PEOPLE_AND_CHARACTERS, font, "D", this));
        cliparts.add(new FontClipart("Garden tools", Category.TOOLS, font, "F", this));
        cliparts.add(new FontClipart("Rake", Category.TOOLS, font, "G", this));
        cliparts.add(new FontClipart("Raking 2", Category.PEOPLE_AND_CHARACTERS, font, "H", this));
        cliparts.add(new FontClipart("pruning shears", Category.TOOLS, font, "I", this));
        cliparts.add(new FontClipart("Watering can", Category.TOOLS, font, "J", this));
        cliparts.add(new FontClipart("Gloves", Category.TOOLS, font, "K", this));
        cliparts.add(new FontClipart("Pitch fork", Category.TOOLS, font, "L", this));
        cliparts.add(new FontClipart("Watering can 2", Category.TOOLS, font, "M", this));
        cliparts.add(new FontClipart("Plant 1", Category.PLANTS, font, "N", this));
        cliparts.add(new FontClipart("Spade", Category.TOOLS, font, "O", this));
        cliparts.add(new FontClipart("Hedge shears", Category.TOOLS, font, "P", this));
        cliparts.add(new FontClipart("Plant", Category.SIGNS_AND_SYMBOLS, font, "Q", this));
        cliparts.add(new FontClipart("Flower", Category.PLANTS, font, "R", this));
        cliparts.add(new FontClipart("Garden fork", Category.TOOLS, font, "S", this));
        cliparts.add(new FontClipart("Garden hose", Category.TOOLS, font, "T", this));
        cliparts.add(new FontClipart("Wheel borrow", Category.TOOLS, font, "U", this));
        cliparts.add(new FontClipart("Water tap", Category.TOOLS, font, "V", this));
        cliparts.add(new FontClipart("Water tap with hose", Category.TOOLS, font, "W", this));
        cliparts.add(new FontClipart("Lawn mower", Category.TOOLS, font, "X", this));
        cliparts.add(new FontClipart("Flower seeds 1", Category.UNSORTED, font, "Y", this));
        cliparts.add(new FontClipart("Flower seeds 2", Category.UNSORTED, font, "Z", this));
        cliparts.add(new FontClipart("Bug", Category.ANIMALS, font, "a", this));
        cliparts.add(new FontClipart("Tree", Category.PLANTS, font, "b", this));
        cliparts.add(new FontClipart("Lawn mower", Category.TOOLS, font, "c", this));
        cliparts.add(new FontClipart("Garden spade", Category.TOOLS, font, "d", this));
        cliparts.add(new FontClipart("Spray bottle", Category.TOOLS, font, "e", this));
        cliparts.add(new FontClipart("Grass cutter", Category.TOOLS, font, "f", this));
        cliparts.add(new FontClipart("Bucket", Category.TOOLS, font, "g", this));
        cliparts.add(new FontClipart("Water hose 2", Category.TOOLS, font, "g", this));
        cliparts.add(new FontClipart("Gloves 2", Category.TOOLS, font, "i", this));
        cliparts.add(new FontClipart("Hose nozzle", Category.TOOLS, font, "j", this));
        cliparts.add(new FontClipart("Garden knife", Category.TOOLS, font, "k", this));
        cliparts.add(new FontClipart("Fence", Category.UNSORTED, font, "s", this));
        cliparts.add(new FontClipart("Gloves 3", Category.TOOLS, font, "t", this));
        cliparts.add(new FontClipart("Watering can 3", Category.TOOLS, font, "u", this));
        cliparts.add(new FontClipart("Pots", Category.UNSORTED, font, "x", this));
        cliparts.add(new FontClipart("Basket", Category.UNSORTED, font, "y", this));
        cliparts.add(new FontClipart("Chainsaw", Category.TOOLS, font, "$", this));
        cliparts.add(new FontClipart("Apple", Category.FOOD, font, "£", this));
    }

    @Override
    public String getName() {
        return "Garden";
    }

    @Override
    public String getCredits() {
        return "Woodcutter";
    }

    @Override
    public String getUrl() {
        return "https://www.dafont.com/garden-icons.font";
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
