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
public class TransdingsSource implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public TransdingsSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, TransdingsSource.class.getResourceAsStream("/fonts/transdings/Transdings-WaoO.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, ","));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "."));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "0"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "1"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "2"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "3"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "4"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "5"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, ":"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, ";"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "@"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "A"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "B"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "C"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "D"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "E"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "F"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "G"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "H"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "I"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "J"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "K"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "L"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "M"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "N"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "O"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "P"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "Q"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "R"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "S"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "T"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "V"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "W"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "X"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "Y"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "Z"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "["));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "\\"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "]"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "`"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "a"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "b"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "c"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "d"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "e"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "f"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "g"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "h"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "i"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "j"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "k"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "l"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "m"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "n"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "o"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "p"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "q"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "r"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "s"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "t"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "u"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "v"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "w"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "x"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "y"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "z"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, "~"));
        cliparts.add(new FontClipart("sign", Category.SIGNS_AND_SYMBOLS, font, ";"));
    }

    @Override
    public String getName() {
        return "Transdings";
    }

    @Override
    public String getCredits() {
        return "Pixel Sagas";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/transdings-font-f18144";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }
}
