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
public class ToolSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public ToolSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, ToolSource.class.getResourceAsStream("/fonts/tool/tool.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Allen key", Category.TOOLS, font, "A", this));
        cliparts.add(new FontClipart("Axe", Category.TOOLS, font, "B", this));
        cliparts.add(new FontClipart("Crow bar", Category.TOOLS, font, "C", this));
        cliparts.add(new FontClipart("Screw driver 1", Category.TOOLS, font, "D", this));
        cliparts.add(new FontClipart("Calipers", Category.TOOLS, font, "E", this));
        cliparts.add(new FontClipart("Hammer", Category.TOOLS, font, "F", this));
        cliparts.add(new FontClipart("Clamp", Category.TOOLS, font, "G", this));
        cliparts.add(new FontClipart("Saw 1", Category.TOOLS, font.deriveFont(font.getSize() * 0.8f), "H", this));
        cliparts.add(new FontClipart("Sledge", Category.TOOLS, font, "I", this));
        cliparts.add(new FontClipart("Poly grip", Category.TOOLS, font, "J", this));
        cliparts.add(new FontClipart("Pliers 1", Category.TOOLS, font, "K", this));
        cliparts.add(new FontClipart("Ring spanner", Category.TOOLS, font, "L", this));
        cliparts.add(new FontClipart("Hammer", Category.TOOLS, font, "M", this));
        cliparts.add(new FontClipart("Pliers 2", Category.TOOLS, font, "N", this));
        cliparts.add(new FontClipart("Spatula", Category.TOOLS, font, "O", this));
        cliparts.add(new FontClipart("Pipe wrench", Category.TOOLS, font, "P", this));
        cliparts.add(new FontClipart("Fixed spanner", Category.TOOLS, font, "Q", this));
        cliparts.add(new FontClipart("Carpet knife", Category.TOOLS, font, "R", this));
        cliparts.add(new FontClipart("Saw 2", Category.TOOLS, font.deriveFont(font.getSize() * 0.8f), "S", this));
        cliparts.add(new FontClipart("Tape", Category.TOOLS, font, "T", this));
        cliparts.add(new FontClipart("Glue gun", Category.TOOLS, font, "U", this));
        cliparts.add(new FontClipart("Planer", Category.TOOLS, font, "V", this));
        cliparts.add(new FontClipart("Fixed spanner 2", Category.TOOLS, font, "W", this));
        cliparts.add(new FontClipart("Screw driver 2", Category.TOOLS, font, "X", this));
        cliparts.add(new FontClipart("Pliers 3", Category.TOOLS, font, "Y", this));
        cliparts.add(new FontClipart("Scissor", Category.TOOLS, font, "Z", this));
        cliparts.add(new FontClipart("Screw", Category.TOOLS, font, "0", this));
        cliparts.add(new FontClipart("Pallet", Category.TOOLS, font, "2", this));
        cliparts.add(new FontClipart("Multi tool", Category.TOOLS, font, "3", this));
        cliparts.add(new FontClipart("Saw 3", Category.TOOLS, font.deriveFont(font.getSize() * 0.7f), "\"", this));
        cliparts.add(new FontClipart("Screw driver 3", Category.TOOLS, font, "5", this));
        cliparts.add(new FontClipart("Screw driver 4", Category.TOOLS, font, "6", this));
        cliparts.add(new FontClipart("Wrench", Category.TOOLS, font, "7", this));
        cliparts.add(new FontClipart("Tweezers", Category.TOOLS, font, "9", this));
        cliparts.add(new FontClipart("Flashlight", Category.TOOLS, font, "?", this));
        cliparts.add(new FontClipart("Nut", Category.TOOLS, font, "+", this));
        cliparts.add(new FontClipart("Tape measure", Category.TOOLS, font, "<", this));
        cliparts.add(new FontClipart("Angle hook", Category.TOOLS, font, "[", this));
        cliparts.add(new FontClipart("Ladder", Category.TOOLS, font, ".", this));
        cliparts.add(new FontClipart("Hook", Category.TOOLS, font, ";", this));
        cliparts.add(new FontClipart("Pliers", Category.TOOLS, font, "\\", this));
        cliparts.add(new FontClipart("Nail", Category.TOOLS, font, "_", this));
        cliparts.add(new FontClipart("Multi spanner", Category.TOOLS, font, "~", this));

    }

    @Override
    public String getName() {
        return "Tool";
    }

    @Override
    public String getCredits() {
        return "Daniel Zadorozny";
    }

    @Override
    public String getUrl() {
        return "https://www.dafont.com/tool.font";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    public String getLicense() {
        return "Free for non-commercial use";
    }
}
