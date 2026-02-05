/*
    Copyright 2021-2026 Joacim Breiler

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
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.model.Size;
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class Rectangle extends AbstractCuttable {

    private final RoundRectangle2D.Double shape;
    private double cornerRadiusPercent = 0.0;

    public Rectangle() {
        this(0, 0);
    }

    /**
     * Creates a rectangle with the relative position to the parent
     *
     * @param x the x position
     * @param y the y position
     */
    public Rectangle(double x, double y) {
        super(x, y);
        this.shape = new RoundRectangle2D.Double(0, 0, 1, 1, 0, 0);
        setName("Rectangle");
    }

    public Rectangle(double x, double y, double w, double h) {
        this(x, y);
        setSize(new Size(w, h));
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public Entity copy() {
        Rectangle rectangle = new Rectangle();
        copyPropertiesTo(rectangle);
        return rectangle;
    }

    @Override
    public List<EntitySetting> getSettings() {
        List<EntitySetting> settings = new ArrayList<>(super.getSettings());
        settings.add(EntitySetting.CORNER_RADIUS);
        return settings;
    }

    @Override
    public void scale(double sx, double sy) {
        super.scale(sx, sy);
        setCornerRadiusPercent(cornerRadiusPercent);
    }

    public void setCornerRadius(Double cornerRadius) {
        if (cornerRadius == null) return;

        double w = getSize().getWidth();
        double h = getSize().getHeight();
        double minSide = Math.min(w, h);
        double percent = Math.max(0d, Math.min(1d, cornerRadius / minSide));

        if (!isEqual(percent, this.cornerRadiusPercent, 0.01)) {
            setCornerRadiusPercent(percent);
            this.notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
        }
    }

    private void setCornerRadiusPercent(double percent) {
        double w = getSize().getWidth();
        double h = getSize().getHeight();
        this.cornerRadiusPercent = percent;

        // Avoid divide-by-zero; also no meaningful rounding for degenerate sizes
        if (w <= 0 || h <= 0) {
            shape.setRoundRect(0, 0, 1, 1, 0, 0);
        } else {
            // Desired arc diameter in "world space" (after transform)
            double worldArc = (Math.min(w, h) * 2) * percent;

            // Convert to local arc diameters so that after scaling they become worldArc
            double localArcW = worldArc / w;
            double localArcH = worldArc / h;

            shape.setRoundRect(0, 0, 1, 1, localArcW, localArcH);
        }
    }

    public double getCornerRadius() {
        double w = getSize().getWidth();
        double h = getSize().getHeight();
        return Math.min(w, h) * cornerRadiusPercent;
    }

    @Override
    public List<CutType> getAvailableCutTypes() {
        return List.of(CutType.POCKET, CutType.SURFACE, CutType.ON_PATH, CutType.INSIDE_PATH, CutType.OUTSIDE_PATH, CutType.LASER_ON_PATH, CutType.LASER_FILL, CutType.CENTER_DRILL);
    }
}
