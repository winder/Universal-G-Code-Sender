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
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class YourSignSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public YourSignSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, YourSignSource.class.getResourceAsStream("/fonts/your-sign/YourSign.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Astrology - Aquarius 1", Category.MYTHICAL, font, "A", this));
        cliparts.add(new FontClipart("Astrology - Pisces 1", Category.MYTHICAL, font, "B ", this));
        cliparts.add(new FontClipart("Astrology - Aries 1", Category.MYTHICAL, font, "C ", this));
        cliparts.add(new FontClipart("Astrology - Taurus 1", Category.MYTHICAL, font, "D ", this));
        cliparts.add(new FontClipart("Astrology - Gemeni 1", Category.MYTHICAL, font, "E ", this));
        cliparts.add(new FontClipart("Astrology - Cancer 1", Category.MYTHICAL, font, "F ", this));
        cliparts.add(new FontClipart("Astrology - Leo 1", Category.MYTHICAL, font, "G ", this));
        cliparts.add(new FontClipart("Astrology - Virgo 1", Category.MYTHICAL, font, "H ", this));
        cliparts.add(new FontClipart("Astrology - Libra 1", Category.MYTHICAL, font, "I ", this));
        cliparts.add(new FontClipart("Astrology - Scorpio 1", Category.MYTHICAL, font, "J ", this));
        cliparts.add(new FontClipart("Astrology - Sagittarius 1", Category.MYTHICAL, font, "K ", this));
        cliparts.add(new FontClipart("Astrology - Capricorn 1", Category.MYTHICAL, font, "L ", this));
        cliparts.add(new FontClipart("Astrology - Aquarius 2", Category.MYTHICAL, font, "a ", this));
        cliparts.add(new FontClipart("Astrology - Pisces 2", Category.MYTHICAL, font, "b ", this));
        cliparts.add(new FontClipart("Astrology - Aries 2", Category.MYTHICAL, font, "c ", this));
        cliparts.add(new FontClipart("Astrology - Taurus 2", Category.MYTHICAL, font, "d ", this));
        cliparts.add(new FontClipart("Astrology - Gemeni 2", Category.MYTHICAL, font, "e ", this));
        cliparts.add(new FontClipart("Astrology - Cancer 2", Category.MYTHICAL, font.deriveFont(font.getSize() * 0.7f), "f ", this));
        cliparts.add(new FontClipart("Astrology - Leo 2", Category.MYTHICAL, font, "g ", this));
        cliparts.add(new FontClipart("Astrology - Virgo 2", Category.MYTHICAL, font, "h ", this));
        cliparts.add(new FontClipart("Astrology - Libra 2", Category.MYTHICAL, font, "i ", this));
        cliparts.add(new FontClipart("Astrology - Scorpio 2", Category.MYTHICAL, font, "j ", this));
        cliparts.add(new FontClipart("Astrology - Sagittarius 2", Category.MYTHICAL, font, "k ", this));
        cliparts.add(new FontClipart("Astrology - Capricorn 2", Category.MYTHICAL, font, "l ", this));
    }

    @Override
    public String getName() {
        return "Your sign";
    }

    @Override
    public String getCredits() {
        return "Lime";
    }

    @Override
    public String getUrl() {
        return "https://www.1001fonts.com/your-sign-font.html";
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
