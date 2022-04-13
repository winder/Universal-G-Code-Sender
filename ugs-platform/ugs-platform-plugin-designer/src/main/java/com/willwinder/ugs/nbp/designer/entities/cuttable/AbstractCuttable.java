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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractCuttable extends AbstractEntity implements Cuttable {
    private CutType cutType = CutType.NONE;
    private double targetDepth;
    private double startDepth;

    protected AbstractCuttable() {
        this(0, 0);
    }

    protected AbstractCuttable(double relativeX, double relativeY) {
        super(relativeX, relativeY);
    }

    @Override
    public CutType getCutType() {
        return cutType;
    }

    @Override
    public void setCutType(CutType cutType) {
        this.cutType = cutType;
    }

    @Override
    public double getStartDepth() {
        return startDepth;
    }

    @Override
    public void setStartDepth(double startDepth) {
        this.startDepth = Math.abs(startDepth);
    }

    @Override
    public double getTargetDepth() {
        return targetDepth;
    }

    @Override
    public void setTargetDepth(double targetDepth) {
        this.targetDepth = Math.abs(targetDepth);
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        float strokeWidth = Double.valueOf(1.2 / drawing.getScale()).floatValue();
        float dashWidth = Double.valueOf(2 / drawing.getScale()).floatValue();

        if (getCutType() != CutType.NONE && getTargetDepth() == 0) {
            graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{dashWidth, dashWidth}, 0));
            graphics.setColor(Colors.SHAPE_HINT);
            graphics.draw(getShape());
        } else if (getCutType() == CutType.POCKET) {
            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.setColor(getCutColor());
            graphics.fill(getShape());
            graphics.draw(getShape());
        } else if (getCutType() == CutType.INSIDE_PATH || getCutType() == CutType.ON_PATH || getCutType() == CutType.OUTSIDE_PATH) {
            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.setColor(getCutColor());
            graphics.draw(getShape());
        } else if (getCutType() == CutType.CENTER_DRILL) {
            Shape shape = getShape();
            double centerX = getShape().getBounds2D().getCenterX();
            double centerY = getShape().getBounds2D().getCenterY();
            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.setColor(Colors.SHAPE_HINT);
            graphics.draw(shape);
            graphics.setColor(getCutColor());
            graphics.draw(new Line2D.Double(shape.getBounds2D().getX() + 1, centerY, shape.getBounds2D().getX() + shape.getBounds2D().getWidth() - 1.0, centerY));
            graphics.draw(new Line2D.Double(centerX, shape.getBounds2D().getY() + 1, centerX, shape.getBounds2D().getY() + shape.getBounds2D().getHeight() - 1.0));
        } else {
            graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{dashWidth, dashWidth}, 0));
            graphics.setColor(Colors.SHAPE_OUTLINE);
            graphics.draw(getShape());
        }
    }

    private Color getCutColor() {
        int color = Math.max(0, Math.min(255, (int) Math.round(255d * getCutAlpha()) - 25));
        return new Color(color, color, color);
    }

    private double getCutAlpha() {
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        if (getTargetDepth() == 0) {
            return 1d;
        }
        return 1d - Math.max(Float.MIN_VALUE, getTargetDepth() / controller.getSettings().getStockThickness());
    }
}
