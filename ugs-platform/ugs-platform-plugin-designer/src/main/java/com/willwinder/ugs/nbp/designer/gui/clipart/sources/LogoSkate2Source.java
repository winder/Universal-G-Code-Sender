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
public class LogoSkate2Source implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public LogoSkate2Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, LogoSkate2Source.class.getResourceAsStream("/fonts/logoskate-2/Logoskate20-KEgD.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "A"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "B"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "C"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "D"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "E"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "F"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "G"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "H"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "I"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "J"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "K"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "L"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "M"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "N"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "O"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "P"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "Q"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "R"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "S"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "T"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "U"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "V"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "W"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "X"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "Y"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "Z"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "a"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "b"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "c"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "d"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "e"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "f"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "g"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "h"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "i"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "j"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "k"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "l"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "m"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "n"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "o"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "p"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "q"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "r"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "s"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "t"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "u"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "v"));
        cliparts.add(new FontClipart("sign", Category.LOGOS, font, "w"));
    }

    @Override
    public String getName() {
        return "Logoskate 2";
    }

    @Override
    public String getCredits() {
        return "RASDESIGN";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/logoskate-font-f13141";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
