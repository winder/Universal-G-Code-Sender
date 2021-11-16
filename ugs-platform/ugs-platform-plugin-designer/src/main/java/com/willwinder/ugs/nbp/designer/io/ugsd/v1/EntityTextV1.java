/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.geom.Point2D;

/**
 * @author Joacim Breiler
 */
public class EntityTextV1 extends CuttableEntityV1 {
    private double x;
    private double y;
    private double width;
    private double height;
    private double rotation;
    private String text;
    private String fontName;

    public EntityTextV1() {
        super(EntityTypeV1.TEXT);
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    @Override
    public Entity toInternal() {
        Text text = new Text();
        text.setFontFamily(fontName);
        text.setText(this.text);
        text.setRotation(rotation);
        text.setSize(new Size(width, height));
        text.setPosition(new Point2D.Double(x, y));
        applyCommonAttributes(text);
        return text;
    }
}
