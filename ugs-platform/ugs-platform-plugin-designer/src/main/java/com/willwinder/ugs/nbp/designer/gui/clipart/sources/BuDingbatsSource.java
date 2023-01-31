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
public class BuDingbatsSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public BuDingbatsSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, BuDingbatsSource.class.getResourceAsStream("/fonts/bu-dingbats/BuDingbatsSansPurpose-njmY.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Yin Yang", Category.SIGNS_AND_SYMBOLS, font, "\uF030", this));
        cliparts.add(new FontClipart("Wage slave", Category.UNSORTED, font, "\uF031", this));
        cliparts.add(new FontClipart("Gas mask", Category.SIGNS_AND_SYMBOLS, font, "\uF032", this));
        cliparts.add(new FontClipart("Recycle", Category.SIGNS_AND_SYMBOLS, font, "\uF033", this));
        cliparts.add(new FontClipart("Grin", Category.UNSORTED, font, "\uF034", this));
        cliparts.add(new FontClipart("Medical", Category.SCIENCE, font, "\uF038", this));
        cliparts.add(new FontClipart("Radioactive", Category.SCIENCE, font, "\uF039", this));
        cliparts.add(new FontClipart("Anonymous", Category.SIGNS_AND_SYMBOLS, font, "\uF041", this));
        cliparts.add(new FontClipart("Betty", Category.PEOPLE_AND_CHARACTERS, font, "\uF042", this));
        cliparts.add(new FontClipart("Groucho Marx", Category.PEOPLE_AND_CHARACTERS, font, "\uF043", this));
        cliparts.add(new FontClipart("Edgar Allen Poe", Category.PEOPLE_AND_CHARACTERS, font, "\uF044", this));
        cliparts.add(new FontClipart("Einstein", Category.PEOPLE_AND_CHARACTERS, font, "\uF045", this));
        cliparts.add(new FontClipart("Frankensteins monster", Category.PEOPLE_AND_CHARACTERS, font, "\uF046", this));
        cliparts.add(new FontClipart("Sheep evolution", Category.UNSORTED, font, "\uF047", this));
        cliparts.add(new FontClipart("Sherlock Holmes", Category.PEOPLE_AND_CHARACTERS, font, "\uF048", this));
        cliparts.add(new FontClipart("Uncle Sam", Category.PEOPLE_AND_CHARACTERS, font, "\uF049", this));
        cliparts.add(new FontClipart("Abraham Lincoln", Category.PEOPLE_AND_CHARACTERS, font, "\uF04A", this));
        cliparts.add(new FontClipart("TV Zombie", Category.UNSORTED, font, "\uF04B", this));
        cliparts.add(new FontClipart("Mona Lisa", Category.PEOPLE_AND_CHARACTERS, font, "\uF04C", this));
        cliparts.add(new FontClipart("Groucho Marx", Category.PEOPLE_AND_CHARACTERS, font, "\uF04D", this));
        cliparts.add(new FontClipart("Popeye", Category.PEOPLE_AND_CHARACTERS, font, "\uF04E", this));
        cliparts.add(new FontClipart("Grin 2", Category.UNSORTED, font, "\uF04F", this));
        cliparts.add(new FontClipart("Pipe", Category.UNSORTED, font, "\uF050", this));
        cliparts.add(new FontClipart("Elderly", Category.PEOPLE_AND_CHARACTERS, font, "\uF051", this));
        cliparts.add(new FontClipart("Duck frame", Category.DECORATIONS, font, "\uF052", this));
        cliparts.add(new FontClipart("Sun", Category.DECORATIONS, font, "\uF053", this));
        cliparts.add(new FontClipart("Shoe", Category.UNSORTED, font, "\uF071", this));
        cliparts.add(new FontClipart("Shoe print", Category.UNSORTED, font, "\uF072", this));
        cliparts.add(new FontClipart("Cobra", Category.ANIMALS, font, "\uF073", this));
        cliparts.add(new FontClipart("Briefcase with money", Category.UNSORTED, font, "\uF061", this));
        cliparts.add(new FontClipart("Bio hazard 1", Category.SCIENCE, font, "\uF062", this));
        cliparts.add(new FontClipart("Bio hazard 2", Category.SCIENCE, font, "\uF063", this));
        cliparts.add(new FontClipart("Diamond bloody", Category.UNSORTED, font, "\uF064", this));
        cliparts.add(new FontClipart("Laundry", Category.UNSORTED, font, "\uF065", this));
        cliparts.add(new FontClipart("Crayons", Category.UNSORTED, font, "\uF066", this));
        cliparts.add(new FontClipart("Radioactive 2", Category.SCIENCE, font, "\uF067", this));
        cliparts.add(new FontClipart("Wolf", Category.ANIMALS, font, "\uF068", this));
        cliparts.add(new FontClipart("Medical 2", Category.SCIENCE, font, "\uF069", this));
        cliparts.add(new FontClipart("Lamp Lava", Category.UNSORTED, font, "\uF06A", this));
        cliparts.add(new FontClipart("Medical 3", Category.SCIENCE, font, "\uF06B", this));
        cliparts.add(new FontClipart("Fish", Category.ANIMALS, font, "\uF06C", this));
        cliparts.add(new FontClipart("Impossible", Category.UNSORTED, font, "\uF06D", this));
        cliparts.add(new FontClipart("Bombs", Category.UNSORTED, font, "\uF06E", this));
        cliparts.add(new FontClipart("Yin Yang 2", Category.SIGNS_AND_SYMBOLS, font, "\uF06F", this));
        cliparts.add(new FontClipart("Shoe", Category.UNSORTED, font, "\uF070", this));
        cliparts.add(new FontClipart("TV", Category.ELECTRONICS, font, "\uF074", this));
        cliparts.add(new FontClipart("Robot", Category.ELECTRONICS, font, "\uF075", this));
        cliparts.add(new FontClipart("Body", Category.PEOPLE_AND_CHARACTERS, font, "\uF076", this));
        cliparts.add(new FontClipart("Cleopatra", Category.PEOPLE_AND_CHARACTERS, font, "\uF078", this));
        cliparts.add(new FontClipart("Meditating", Category.PEOPLE_AND_CHARACTERS, font, "\uF079", this));
        cliparts.add(new FontClipart("Turtle", Category.ANIMALS, font, "\uF07A", this));
    }

    @Override
    public String getName() {
        return "BuDingbats";
    }

    @Override
    public String getCredits() {
        return "Bosil unique fonts";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/bu-dingbats-sans-purpose-font-f17451";
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
