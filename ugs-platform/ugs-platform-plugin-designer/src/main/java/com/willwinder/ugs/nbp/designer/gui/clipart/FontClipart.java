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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * A clipart type based on a font and a letter
 *
 * @author Joacim Breiler
 */
public class FontClipart implements Clipart {
    private final Category category;
    private final JLabel label;
    private final String name;
    private final String text;
    private final Font font;
    private final ClipartSource source;

    public FontClipart(String name, Category category, Font font, String text, ClipartSource source) {
        this.name = name;
        this.category = category;
        this.text = text;
        this.font = font;
        this.source = source;

        label = new JLabel(text);
        label.setFont(font);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(ThemeColors.VERY_DARK_GREY);
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Component getPreview() {
        return label;
    }

    @Override
    public Cuttable getCuttable() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        GlyphVector glyphVector = font.createGlyphVector(g2.getFontRenderContext(), text);

        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        Shape shape = transform.createTransformedShape(glyphVector.getOutline(0, 0));
        Path path = new Path();
        path.append(shape);
        return path;
    }

    @Override
    public String toString() {
        return "FontClipart{" +
                "category=" + category +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", font=" + font +
                '}';
    }

    public ClipartSource getSource() {
        return source;
    }
}
