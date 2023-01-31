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
public class DarriansFrames2Source implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public DarriansFrames2Source() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, DarriansFrames2Source.class.getResourceAsStream("/fonts/darrians-frames-font/DarriansFramesTwo-8M5M.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Frames 01", Category.DECORATIONS, font, "a", this));
        cliparts.add(new FontClipart("Frames 02", Category.DECORATIONS, font, "b", this));
        cliparts.add(new FontClipart("Frames 03", Category.DECORATIONS, font, "c", this));
        cliparts.add(new FontClipart("Frames 04", Category.DECORATIONS, font, "d", this));
        cliparts.add(new FontClipart("Frames 05", Category.DECORATIONS, font, "e", this));
        cliparts.add(new FontClipart("Frames 06", Category.DECORATIONS, font, "f", this));
        cliparts.add(new FontClipart("Frames 07", Category.DECORATIONS, font, "g", this));
        cliparts.add(new FontClipart("Frames 08", Category.DECORATIONS, font, "h", this));
        cliparts.add(new FontClipart("Frames 09", Category.DECORATIONS, font, "i", this));
        cliparts.add(new FontClipart("Frames 10", Category.DECORATIONS, font, "j", this));
        cliparts.add(new FontClipart("Frames 11", Category.DECORATIONS, font, "k", this));
        cliparts.add(new FontClipart("Frames 12", Category.DECORATIONS, font, "l", this));
        cliparts.add(new FontClipart("Frames 13", Category.DECORATIONS, font, "m", this));
        cliparts.add(new FontClipart("Frames 14", Category.DECORATIONS, font, "n", this));
        cliparts.add(new FontClipart("Frames 15", Category.DECORATIONS, font, "o", this));
        cliparts.add(new FontClipart("Frames 16", Category.DECORATIONS, font, "p", this));
        cliparts.add(new FontClipart("Frames 17", Category.DECORATIONS, font, "q", this));
        cliparts.add(new FontClipart("Frames 18", Category.DECORATIONS, font, "r", this));
        cliparts.add(new FontClipart("Frames 19", Category.DECORATIONS, font, "s", this));
        cliparts.add(new FontClipart("Frames 20", Category.DECORATIONS, font, "t", this));
        cliparts.add(new FontClipart("Frames 21", Category.DECORATIONS, font, "u", this));
        cliparts.add(new FontClipart("Frames 22", Category.DECORATIONS, font, "v", this));
        cliparts.add(new FontClipart("Frames 23", Category.DECORATIONS, font, "w", this));
        cliparts.add(new FontClipart("Frames 24", Category.DECORATIONS, font, "x", this));
        cliparts.add(new FontClipart("Frames 25", Category.DECORATIONS, font, "y", this));
    }

    @Override
    public String getName() {
        return "DarriansFrames2";
    }

    @Override
    public String getCredits() {
        return "Darrian Lynx";
    }

    @Override
    public String getUrl() {
        return "https://www.fontspace.com/darrians-frames-font-f1770";
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
