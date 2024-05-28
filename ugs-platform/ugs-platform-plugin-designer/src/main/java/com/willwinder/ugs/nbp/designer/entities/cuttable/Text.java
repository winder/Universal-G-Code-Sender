/*
    Copyright 2021-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import org.apache.commons.lang3.StringUtils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A cuttable text shape
 *
 * @author Joacim Breiler
 */
public class Text extends AbstractCuttable {
    private String text;
    private String fontFamily;
    private Shape shape;

    private final AffineTransform transform = AffineTransform.getScaleInstance(1, -1);

    public Text(double x, double y) {
        super(x, y);
        setName("Text");
        text = "";
        fontFamily = Font.SANS_SERIF;
        regenerateShape();
    }

    public Text() {
        this(0, 0);
    }

    private void regenerateShape() {
        if (StringUtils.isNotEmpty(text)) {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            Font font = new Font(this.fontFamily, Font.PLAIN, 18);
            GlyphVector glyphVector = font.createGlyphVector(g2.getFontRenderContext(), text);
            shape = transform.createTransformedShape(glyphVector.getOutline(0, 0));
        } else {
            // Create a temporary shape
            shape = transform.createTransformedShape(new Rectangle2D.Double(0, 0, 2, 12));
        }

        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        regenerateShape();
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        regenerateShape();
    }

    @Override
    public Entity copy() {
        Text copy = new Text();
        copyPropertiesTo(copy);
        copy.setText(getText());
        copy.setFontFamily(getFontFamily());
        return copy;
    }

    @Override
    public List<EntitySetting> getSettings() {
        List<EntitySetting> entitySettings = new ArrayList<>(super.getSettings());
        entitySettings.add(EntitySetting.TEXT);
        return entitySettings;
    }
}
