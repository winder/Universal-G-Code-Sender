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
public class FredokaSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public FredokaSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, FredokaSource.class.getResourceAsStream("/fonts/fredoka-one/Fredoka-dingbats.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Could not load font", e);
        }

        cliparts.add(new FontClipart("elephant", Category.ANIMALS, font, "\u0061"));
        cliparts.add(new FontClipart("bird", Category.ANIMALS, font, "\u0062"));
        cliparts.add(new FontClipart("fish", Category.ANIMALS, font, "\u0063"));
        cliparts.add(new FontClipart("owl", Category.ANIMALS, font, "\u0064"));
        cliparts.add(new FontClipart("cat", Category.ANIMALS, font, "\u0065"));
        cliparts.add(new FontClipart("butterfly", Category.ANIMALS, font, "\u0066"));
        cliparts.add(new FontClipart("rabbit", Category.ANIMALS, font, "\u0067"));
        cliparts.add(new FontClipart("fishbowl", Category.ANIMALS, font, "\u0068"));
        cliparts.add(new FontClipart("mouse", Category.ANIMALS, font, "\u0069"));
        cliparts.add(new FontClipart("ornament-left", Category.DECORATIONS, font, "\u0028"));
        cliparts.add(new FontClipart("ornament-right", Category.DECORATIONS, font, "\u0029"));
        cliparts.add(new FontClipart("ornament-1", Category.DECORATIONS, font, "\u0031"));
        cliparts.add(new FontClipart("ornament-2", Category.DECORATIONS, font, "\u0032"));
        cliparts.add(new FontClipart("ornament-3", Category.DECORATIONS, font, "\u0033"));
        cliparts.add(new FontClipart("ornament-4", Category.DECORATIONS, font, "\u0034"));
        cliparts.add(new FontClipart("ornament-5", Category.DECORATIONS, font, "\u0035"));
        cliparts.add(new FontClipart("heart", Category.DECORATIONS, font, "\u0036"));
        cliparts.add(new FontClipart("ornament-7", Category.DECORATIONS, font, "\u0038"));
        cliparts.add(new FontClipart("ornament-8", Category.DECORATIONS, font, "\u0039"));
        cliparts.add(new FontClipart("ornament-9", Category.DECORATIONS, font, "\u003C"));
        cliparts.add(new FontClipart("flower-1", Category.DECORATIONS, font, "\u0041"));
        cliparts.add(new FontClipart("flower-2", Category.DECORATIONS, font, "\u0042"));
        cliparts.add(new FontClipart("flower-3", Category.DECORATIONS, font, "\u0043"));
        cliparts.add(new FontClipart("leaf", Category.DECORATIONS, font, "\u0044"));
        cliparts.add(new FontClipart("barley", Category.DECORATIONS, font, "\u0045"));
        cliparts.add(new FontClipart("rye", Category.DECORATIONS, font, "\u0046"));
        cliparts.add(new FontClipart("yinyang", Category.DECORATIONS, font, "\u0048"));
        cliparts.add(new FontClipart("knot", Category.DECORATIONS, font, "\u004B"));
        cliparts.add(new FontClipart("flower", Category.DECORATIONS, font, "\u0054"));
        cliparts.add(new FontClipart("old-phone", Category.DECORATIONS, font, "\u004A"));
        cliparts.add(new FontClipart("cellphone", Category.DECORATIONS, font, "\u0051"));
        cliparts.add(new FontClipart("tv", Category.DECORATIONS, font, "\u0052"));
    }

    @Override
    public String getName() {
        return "Fredoka One";
    }

    @Override
    public String getCredits() {
        return "Milena Brandao";
    }

    @Override
    public String getUrl() {
        return "https://www.1001fonts.com/fredoka-one-font.html";
    }

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
