package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.Colors;

import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class AbstractCuttable extends AbstractEntity implements Cuttable {

    private CutType cutType = CutType.NONE;

    public AbstractCuttable() {
        this(0, 0);
    }

    public AbstractCuttable(double relativeX, double relativeY) {
        super(relativeX, relativeY);
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
    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1));

        if (getCutType() == CutType.POCKET) {
            graphics.setColor(Color.BLACK);
            graphics.fill(getShape());
            graphics.draw(getShape());
        } else if (getCutType() == CutType.INSIDE_PATH ||getCutType() == CutType.ON_PATH ||getCutType() == CutType.OUTSIDE_PATH) {
            graphics.setColor(Color.BLACK);
            graphics.draw(getShape());
        }
        else {
            graphics.setColor(Color.GRAY);
            graphics.draw(getShape());
        }
    }
}
