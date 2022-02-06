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
public class Corners2Source implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public Corners2Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, Corners2Source.class.getResourceAsStream("/fonts/corners2/Corners2.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Could not load font", e);
        }

        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "A"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "B"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "C"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "D"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "E"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "F"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "G"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font.deriveFont(font.getSize() * 0.8f), "H"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "I"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "J"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "K"));
        cliparts.add(new FontClipart("sign", Category.DECORATIONS, font, "L"));
    }

    @Override
    public String getName() {
        return "Corners2";
    }

    @Override
    public String getCredits() {
        return "Digital Magic";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/corners-2-font-f5917";
    }

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
