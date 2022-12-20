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

        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "A"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "B"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "C"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "D"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "E"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "F"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "G"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "H"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "I"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "J"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "K"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "L"));

        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "a"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "b"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "c"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "d"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "e"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "f"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "g"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "h"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "i"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "j"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "k"));
        cliparts.add(new FontClipart("sign", Category.MYTHICAL, font, "l"));
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
}
