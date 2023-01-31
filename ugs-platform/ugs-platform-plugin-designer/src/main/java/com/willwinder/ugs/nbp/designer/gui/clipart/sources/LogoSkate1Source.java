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
public class LogoSkate1Source implements ClipartSource {

    private final List<FontClipart> cliparts = new ArrayList<>();

    public LogoSkate1Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, LogoSkate1Source.class.getResourceAsStream("/fonts/logoskate-1/Logoskate-Md4x.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("DC", Category.LOGOS, font, "0", this));
        cliparts.add(new FontClipart("sign2", Category.LOGOS, font, "1", this));
        cliparts.add(new FontClipart("sign3", Category.LOGOS, font, "2", this));
        cliparts.add(new FontClipart("sign4", Category.LOGOS, font, "3", this));
        cliparts.add(new FontClipart("sign5", Category.LOGOS, font, "4", this));
        cliparts.add(new FontClipart("X games", Category.LOGOS, font, "5", this));
        cliparts.add(new FontClipart("Royal", Category.LOGOS, font, "6", this));
        cliparts.add(new FontClipart("Think", Category.LOGOS, font, "7", this));
        cliparts.add(new FontClipart("Vans off the wall", Category.LOGOS, font, "8", this));
        cliparts.add(new FontClipart("sign11", Category.LOGOS, font, "A", this));
        cliparts.add(new FontClipart("Flip", Category.LOGOS, font, "B", this));
        cliparts.add(new FontClipart("Osiris", Category.LOGOS, font, "C", this));
        cliparts.add(new FontClipart("Independent Truck Company", Category.LOGOS, font, "D", this));
        cliparts.add(new FontClipart("Adio", Category.LOGOS, font, "E", this));
        cliparts.add(new FontClipart("Darkstar", Category.LOGOS, font, "F", this));
        cliparts.add(new FontClipart("Globe", Category.LOGOS, font, "G", this));
        cliparts.add(new FontClipart("Fallen", Category.LOGOS, font, "H", this));
        cliparts.add(new FontClipart("Baker", Category.LOGOS, font, "I", this));
        cliparts.add(new FontClipart("DVS", Category.LOGOS, font, "J", this));
        cliparts.add(new FontClipart("Zero", Category.LOGOS, font, "K", this));
        cliparts.add(new FontClipart("sign22", Category.LOGOS, font, "L", this));
        cliparts.add(new FontClipart("sign23", Category.LOGOS, font, "M", this));
        cliparts.add(new FontClipart("Adio", Category.LOGOS, font, "N", this));
        cliparts.add(new FontClipart("Burton", Category.LOGOS, font, "O", this));
        cliparts.add(new FontClipart("Mystery", Category.LOGOS, font, "P", this));
        cliparts.add(new FontClipart("Shorty's", Category.LOGOS, font, "Q", this));
        cliparts.add(new FontClipart("Ricta", Category.LOGOS, font, "R", this));
        cliparts.add(new FontClipart("Santa Cruz", Category.LOGOS, font, "S", this));
        cliparts.add(new FontClipart("Tensor", Category.LOGOS, font, "T", this));
        cliparts.add(new FontClipart("Thrasher", Category.LOGOS, font, "U", this));
        cliparts.add(new FontClipart("sign32", Category.LOGOS, font, "V", this));
        cliparts.add(new FontClipart("Volcom", Category.LOGOS, font, "W", this));
        cliparts.add(new FontClipart("sign34", Category.LOGOS, font, "X", this));
        cliparts.add(new FontClipart("Toy machine", Category.LOGOS, font, "Y", this));
        cliparts.add(new FontClipart("sign36", Category.LOGOS, font, "Z", this));
        cliparts.add(new FontClipart("Vans", Category.LOGOS, font, "a", this));
        cliparts.add(new FontClipart("sign38", Category.LOGOS, font, "b", this));
        cliparts.add(new FontClipart("Sector 9", Category.LOGOS, font, "c", this));
        cliparts.add(new FontClipart("Rip Curl", Category.LOGOS, font, "d", this));
        cliparts.add(new FontClipart("sign41", Category.LOGOS, font, "e", this));
        cliparts.add(new FontClipart("sign42", Category.LOGOS, font, "f", this));
        cliparts.add(new FontClipart("Es", Category.LOGOS, font, "g", this));
        cliparts.add(new FontClipart("Path", Category.LOGOS, font, "h", this));
        cliparts.add(new FontClipart("Bones", Category.LOGOS, font, "i", this));
        cliparts.add(new FontClipart("Termite", Category.LOGOS, font, "j", this));
        cliparts.add(new FontClipart("sign47", Category.LOGOS, font, "k", this));
        cliparts.add(new FontClipart("Supra", Category.LOGOS, font, "l", this));
        cliparts.add(new FontClipart("Black label", Category.LOGOS, font, "m", this));
        cliparts.add(new FontClipart("Billabong", Category.LOGOS, font, "n", this));
        cliparts.add(new FontClipart("Hurley", Category.LOGOS, font, "o", this));
        cliparts.add(new FontClipart("Chocolate", Category.LOGOS, font, "p", this));
        cliparts.add(new FontClipart("Habitat", Category.LOGOS, font, "q", this));
        cliparts.add(new FontClipart("sign54", Category.LOGOS, font, "r", this));
        cliparts.add(new FontClipart("Gangsta", Category.LOGOS, font, "s", this));
        cliparts.add(new FontClipart("sign56", Category.LOGOS, font, "t", this));
        cliparts.add(new FontClipart("Old industries", Category.LOGOS, font, "u", this));
        cliparts.add(new FontClipart("sign58", Category.LOGOS, font, "v", this));
        cliparts.add(new FontClipart("Alien workshop", Category.LOGOS, font, "w", this));
    }

    @Override
    public String getName() {
        return "Logoskate 1";
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
