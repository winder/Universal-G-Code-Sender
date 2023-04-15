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
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractCuttable extends AbstractEntity implements Cuttable {
    private CutType cutType = CutType.NONE;
    private double targetDepth;
    private double startDepth;
    private boolean isHidden = false;

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
        if (isHidden) {
            return;
        }

        float strokeWidth = 1.2f / (float) drawing.getScale();
        float dashWidth = 2f / (float) drawing.getScale();
        BasicStroke dashedStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{dashWidth, dashWidth}, 0);

        Shape shape = getShape();
        if (getCutType() != CutType.NONE && getTargetDepth() == 0) {
            drawShape(graphics, dashedStroke, Colors.SHAPE_HINT, shape);
        } else if (getCutType() == CutType.POCKET) {
            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.setColor(getCutColor());
            graphics.fill(shape);
            graphics.draw(shape);
        } else if (getCutType() == CutType.INSIDE_PATH || getCutType() == CutType.ON_PATH || getCutType() == CutType.OUTSIDE_PATH) {
            drawShape(graphics, new BasicStroke(strokeWidth), getCutColor(), shape);
        } else if (getCutType() == CutType.CENTER_DRILL) {
            drawShape(graphics, new BasicStroke(strokeWidth), Colors.SHAPE_HINT, shape);
            double centerX = shape.getBounds2D().getCenterX();
            double centerY = shape.getBounds2D().getCenterY();
            graphics.setColor(getCutColor());
            graphics.draw(new Line2D.Double(shape.getBounds2D().getX() + 1, centerY, shape.getBounds2D().getX() + shape.getBounds2D().getWidth() - 1.0, centerY));
            graphics.draw(new Line2D.Double(centerX, shape.getBounds2D().getY() + 1, centerX, shape.getBounds2D().getY() + shape.getBounds2D().getHeight() - 1.0));
        } else {
            drawShape(graphics, dashedStroke, Colors.SHAPE_OUTLINE, shape);
        }
    }

    private void drawShape(Graphics2D graphics, BasicStroke strokeWidth, Color shapeHint, Shape shape) {
        graphics.setStroke(strokeWidth);
        graphics.setColor(shapeHint);
        graphics.draw(shape);
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        isHidden = hidden;
        notifyEvent(new EntityEvent(this, EventType.HIDDEN));
    }

    @Override
    public Rectangle2D getBounds() {
        // Make sure that the shape bounds are not zero to make it possible to select the entity
        Rectangle2D bounds = super.getBounds();
        return new Rectangle2D.Double(bounds.getX(), bounds.getY(), Math.max(bounds.getWidth(), 0.001), Math.max(bounds.getHeight(), 0.001));
    }

    private Color getCutColor() {
        int color = Math.max(0, Math.min(255, (int) Math.round(255d * getCutAlpha()) - 25));
        return new Color(color, color, color);
    }

    private double getCutAlpha() {
        Controller controller = ControllerFactory.getController();
        if (getTargetDepth() == 0) {
            return 1d;
        }
        return 1d - Math.max(Float.MIN_VALUE, getTargetDepth() / controller.getSettings().getStockThickness());
    }

    protected void copyPropertiesTo(Cuttable copy) {
        super.copyPropertiesTo(copy);
        copy.setStartDepth(getStartDepth());
        copy.setTargetDepth(getTargetDepth());
        copy.setCutType(getCutType());
        copy.setHidden(isHidden());
    }
}
