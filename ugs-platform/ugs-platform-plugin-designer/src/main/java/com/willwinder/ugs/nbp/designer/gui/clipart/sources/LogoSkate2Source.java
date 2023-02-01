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

        cliparts.add(new FontClipart("sign1", Category.LOGOS, font, "A", this));
        cliparts.add(new FontClipart("Circa", Category.LOGOS, font, "B", this));
        cliparts.add(new FontClipart("Blind", Category.LOGOS, font, "C", this));
        cliparts.add(new FontClipart("Hawk", Category.LOGOS, font, "D", this));
        cliparts.add(new FontClipart("sign5", Category.LOGOS, font, "E", this));
        cliparts.add(new FontClipart("Bones", Category.LOGOS, font, "F", this));
        cliparts.add(new FontClipart("DGK", Category.LOGOS, font, "G", this));
        cliparts.add(new FontClipart("Popwar", Category.LOGOS, font, "H", this));
        cliparts.add(new FontClipart("Organika", Category.LOGOS, font, "I", this));
        cliparts.add(new FontClipart("Lost", Category.LOGOS, font, "J", this));
        cliparts.add(new FontClipart("Roxy", Category.LOGOS, font, "K", this));
        cliparts.add(new FontClipart("sign12", Category.LOGOS, font, "L", this));
        cliparts.add(new FontClipart("Adio", Category.LOGOS, font, "M", this));
        cliparts.add(new FontClipart("sign14", Category.LOGOS, font, "N", this));
        cliparts.add(new FontClipart("sign15", Category.LOGOS, font, "O", this));
        cliparts.add(new FontClipart("sign16", Category.LOGOS, font, "P", this));
        cliparts.add(new FontClipart("Krooked", Category.LOGOS, font, "Q", this));
        cliparts.add(new FontClipart("sign18", Category.LOGOS, font, "R", this));
        cliparts.add(new FontClipart("Fox", Category.LOGOS, font, "S", this));
        cliparts.add(new FontClipart("Duffed", Category.LOGOS, font, "T", this));
        cliparts.add(new FontClipart("sign21", Category.LOGOS, font, "U", this));
        cliparts.add(new FontClipart("Matix", Category.LOGOS, font, "V", this));
        cliparts.add(new FontClipart("Kana Beach", Category.LOGOS, font, "W", this));
        cliparts.add(new FontClipart("Atticus", Category.LOGOS, font, "X", this));
        cliparts.add(new FontClipart("Creature", Category.LOGOS, font, "Y", this));
        cliparts.add(new FontClipart("LRG", Category.LOGOS, font, "Z", this));
        cliparts.add(new FontClipart("sign27", Category.LOGOS, font, "a", this));
        cliparts.add(new FontClipart("VC", Category.LOGOS, font, "b", this));
        cliparts.add(new FontClipart("Circa", Category.LOGOS, font, "c", this));
        cliparts.add(new FontClipart("sign30", Category.LOGOS, font, "d", this));
        cliparts.add(new FontClipart("Chimson", Category.LOGOS, font, "e", this));
        cliparts.add(new FontClipart("Sk8mafia", Category.LOGOS, font, "f", this));
        cliparts.add(new FontClipart("Hookups", Category.LOGOS, font, "g", this));
        cliparts.add(new FontClipart("sign34", Category.LOGOS, font, "h", this));
        cliparts.add(new FontClipart("GvR", Category.LOGOS, font, "i", this));
        cliparts.add(new FontClipart("Jart", Category.LOGOS, font, "j", this));
        cliparts.add(new FontClipart("Enjoi", Category.LOGOS, font, "k", this));
        cliparts.add(new FontClipart("Jart", Category.LOGOS, font, "l", this));
        cliparts.add(new FontClipart("Mini LOGO", Category.LOGOS, font, "m", this));
        cliparts.add(new FontClipart("Riviera", Category.LOGOS, font, "n", this));
        cliparts.add(new FontClipart("Soeed Demon", Category.LOGOS, font, "o", this));
        cliparts.add(new FontClipart("City Stars", Category.LOGOS, font, "p", this));
        cliparts.add(new FontClipart("Dooks", Category.LOGOS, font, "q", this));
        cliparts.add(new FontClipart("Inees", Category.LOGOS, font, "r", this));
        cliparts.add(new FontClipart("Gravis", Category.LOGOS, font, "s", this));
        cliparts.add(new FontClipart("sign46", Category.LOGOS, font, "t", this));
        cliparts.add(new FontClipart("Elwood", Category.LOGOS, font, "u", this));
        cliparts.add(new FontClipart("Diamond", Category.LOGOS, font, "v", this));
        cliparts.add(new FontClipart("Volcom", Category.LOGOS, font, "w", this));
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

    @Override
    public String getLicense() {
        return "Free for commercial use";
    }
}
