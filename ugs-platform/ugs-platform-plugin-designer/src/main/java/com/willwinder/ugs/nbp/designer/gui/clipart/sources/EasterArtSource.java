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
public class EasterArtSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public EasterArtSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, EasterArtSource.class.getResourceAsStream("/fonts/easterart/EasterArt.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Could not load font", e);
        }
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "A"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "B"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "C"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "D"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "E"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "F"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "G"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "H"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "I"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "J"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "K"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.6f), "L"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "M"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "N"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "O"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "P"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "Q"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "R"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "S"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "T"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "U"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "V"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "W"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "X"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "Y"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "Z"));

        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "a"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "b"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "c"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "d"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "e"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "f"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "g"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "h"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "i"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "j"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font.deriveFont(font.getSize() * 0.8f), "k"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "l"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "m"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "n"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "o"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "p"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "q"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "r"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "s"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "t"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "u"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "v"));
        cliparts.add(new FontClipart("sign", Category.HOLIDAY, font, "w"));
    }

    @Override
    public String getName() {
        return "EasterArt";
    }

    @Override
    public String getCredits() {
        return "GemFonts";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/easter-art-font-f4082";
    }

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
