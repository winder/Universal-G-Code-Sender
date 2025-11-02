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

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractCuttable extends AbstractEntity implements Cuttable {
    private final CuttableEntitySettings entitySettings;
    private CutType cutType = CutType.NONE;
    private double targetDepth;
    private double startDepth;
    private int leadInPercent;
    private int leadOutPercent;
    private int spindleSpeed;
    private int passes;
    private int feedRate;
    private boolean isHidden = false;
    private boolean includeInExport = true;    
    
    protected AbstractCuttable() {
        this(0, 0);
    }

    protected AbstractCuttable(double relativeX, double relativeY) {
        super(relativeX, relativeY);
        entitySettings = new CuttableEntitySettings(this);
    }

    @Override
    public CutType getCutType() {
        return cutType;
    }

    @Override
    public void setCutType(CutType cutType) {
        this.cutType = cutType;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public double getStartDepth() {
        return startDepth;
    }

    @Override
    public void setStartDepth(double startDepth) {
        this.startDepth = startDepth;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public double getTargetDepth() {
        return targetDepth;
    }

    @Override
    public void setTargetDepth(double targetDepth) {
        this.targetDepth = targetDepth;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public int getSpindleSpeed() {
        return spindleSpeed;
    }

    @Override
    public void setSpindleSpeed(int speed) {
        this.spindleSpeed = Math.abs(speed);
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public int getPasses() {
        return passes;
    }

    @Override
    public void setPasses(int passes) {
        this.passes = Math.abs(passes);
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public int getFeedRate() {
        return feedRate;
    }

    @Override
    public void setFeedRate(int feedRate) {
        this.feedRate = Math.abs(feedRate);
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public void setLeadInPercent(int percent) {
        this.leadInPercent = percent;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public int getLeadInPercent() {
        return leadInPercent;
    }


    @Override
    public void setLeadOutPercent(int percent) {
        this.leadOutPercent = percent;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public int getLeadOutPercent() {
        return leadOutPercent;
    }

    @Override
    public boolean isWithin(Point2D point) {
        if (cutType != CutType.SURFACE) {
            return super.isWithin(point);
        }

        return getSurfacingShape().contains(point) || getSurfacingShape().intersects(point.getX() - 1, point.getY() - 1, 2, 2);
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
        if (getCutType() == CutType.NONE) {
            drawShape(graphics, dashedStroke, Colors.SHAPE_HINT, shape);
        } else if (getCutType() == CutType.POCKET) {
            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.setColor(getCutColor());
            graphics.fill(shape);
            graphics.draw(shape);
        } else if (getCutType() == CutType.SURFACE) {
            Shape surfacingShape = getSurfacingShape();
            graphics.setColor(getCutColor());
            graphics.fill(surfacingShape);
            drawShape(graphics, dashedStroke, Colors.SHAPE_HINT, surfacingShape);

            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.draw(shape);
        } else if (getCutType() == CutType.INSIDE_PATH || getCutType() == CutType.ON_PATH || getCutType() == CutType.OUTSIDE_PATH) {
            drawShape(graphics, new BasicStroke(strokeWidth), getCutColor(), shape);
        } else if (getCutType() == CutType.LASER_ON_PATH) {
            drawShape(graphics, new BasicStroke(strokeWidth), getLaserCutColor(), shape);
        } else if (getCutType() == CutType.LASER_FILL) {
            graphics.setStroke(new BasicStroke(strokeWidth));
            graphics.setColor(getLaserCutColor());
            graphics.fill(shape);
            graphics.draw(shape);
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

    private Shape getBufferedShape(double leadInMillimeters, double leadOutMillimeters) {
        Rectangle2D shape = getShape().getBounds2D();
        shape.setFrame(shape.getX() - leadInMillimeters, shape.getY(), shape.getWidth() + leadInMillimeters + leadOutMillimeters, shape.getHeight());
        return new Area(shape);
    }

    private Shape getSurfacingShape() {
        double leadInMillimeters = ControllerFactory.getController().getSettings().getToolDiameter() * ((double) getLeadInPercent() / 100d);
        double leadOutMillimeters = ControllerFactory.getController().getSettings().getToolDiameter() * ((double) getLeadOutPercent() / 100d);
        return getBufferedShape(leadInMillimeters, leadOutMillimeters);
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
        Rectangle2D bounds = super.getBounds();
        return new Rectangle2D.Double(bounds.getX(), bounds.getY(), Math.max(bounds.getWidth(), 0.001), Math.max(bounds.getHeight(), 0.001));
    }

    @Override
    public List<EntitySetting> getSettings() {
        return Arrays.asList(
                EntitySetting.ANCHOR,
                EntitySetting.POSITION_X,
                EntitySetting.POSITION_Y,
                EntitySetting.WIDTH,
                EntitySetting.HEIGHT,
                EntitySetting.ROTATION,
                EntitySetting.CUT_TYPE,
                EntitySetting.START_DEPTH,
                EntitySetting.TARGET_DEPTH,
                EntitySetting.SPINDLE_SPEED,
                EntitySetting.PASSES,
                EntitySetting.FEED_RATE,
                EntitySetting.LEAD_IN_PERCENT,
                EntitySetting.LEAD_OUT_PERCENT,
                EntitySetting.INCLUDE_IN_EXPORT
        );
    }

    private Color getLaserCutColor() {
        int color = Math.max(0, Math.min(255, (int) Math.round(255d * getLaserCutAlpha()) - 50));
        return new Color(color, color, color);
    }

    private double getLaserCutAlpha() {
        return 1d - Math.max(Float.MIN_VALUE, getEntitySetting(EntitySetting.SPINDLE_SPEED)
                .map(v -> (Integer) v / 100d).orElse(0d));
    }


    private Color getCutColor() {
        int color = Math.max(0, Math.min(255, (int) Math.round(255d * getCutAlpha()) - 50));
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
        copy.setSpindleSpeed(getSpindleSpeed());
        copy.setPasses(getPasses());
        copy.setHidden(isHidden());
        copy.setIncludeInExport(getIncludeInExport());
    }

    @Override
    public Optional<Object> getEntitySetting(EntitySetting entitySetting) {
        return entitySettings.getEntitySetting(entitySetting);
    }

    @Override
    public void setEntitySetting(EntitySetting entitySetting, Object value) {
        entitySettings.setEntitySetting(entitySetting, value);
    }
    
    @Override
    public boolean getIncludeInExport() {
        return includeInExport;
    }

    @Override
    public void setIncludeInExport(boolean value) {
        includeInExport = value;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }
}
