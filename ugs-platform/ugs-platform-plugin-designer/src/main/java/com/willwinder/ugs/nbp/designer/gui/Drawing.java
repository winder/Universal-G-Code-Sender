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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.controls.GridControl;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.awt.RenderingHints.*;

/**
 * @author Joacim Breiler
 */
public class Drawing extends JPanel {

    private static final long serialVersionUID = 1298712398723987873L;
    private final transient Controller controller;
    private final transient EntityGroup globalRoot;
    private final transient EntityGroup entitiesRoot;
    private double scale;
    private final transient Set<DrawingListener> listeners = new HashSet<>();
    private int margin = 100;

    public Drawing(Controller controller) {
        this.controller = controller;
        globalRoot = new EntityGroup();
        globalRoot.addChild(new GridControl(controller));
        entitiesRoot = new EntityGroup();
        globalRoot.addChild(entitiesRoot);
        globalRoot.addChild(controller.getSelectionManager());

        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.WHITE);
        setScale(1);
    }

    public BufferedImage getImage() {
        BufferedImage bi = new BufferedImage(getPreferredSize().width,
                getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        this.print(g);
        return bi;
    }

    public List<Entity> getEntitiesAt(Point2D p) {
        return globalRoot.getChildrenAt(p);
    }

    public void insertEntity(Entity s) {
        entitiesRoot.addChild(s);
        listeners.forEach(l -> l.onDrawingEvent(DrawingEvent.ENTITY_ADDED));
    }

    public List<Entity> getEntities() {
        List<Entity> result = new ArrayList<>();
        entitiesRoot.getChildren().forEach(shape -> recursiveCollectEntities(shape, result));
        return result;
    }

    public Entity getRootEntity() {
        return entitiesRoot;
    }

    private void recursiveCollectEntities(Entity shape, List<Entity> result) {
        if (shape instanceof EntityGroup) {
            List<Entity> shapes = ((EntityGroup) shape).getChildren();
            shapes.forEach(s -> recursiveCollectEntities(s, result));
        } else {
            result.add(shape);
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform previousTransform = g2.getTransform();

        AffineTransform affineTransform = new AffineTransform(g2.getTransform());
        affineTransform.concatenate(getTransform());
        g2.setTransform(affineTransform);

        RenderingHints rh = ((Graphics2D) g).getRenderingHints();
        rh.put(KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        rh.put(KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(rh);
        globalRoot.render(g2);
        g2.setTransform(previousTransform);
    }

    public void removeEntity(Entity s) {
        globalRoot.removeChild(s);
        listeners.forEach(l -> l.onDrawingEvent(DrawingEvent.ENTITY_REMOVED));
    }

    /**
     * Returns the scale of a pixel, if set to 1 means that one pixel is equal to one millimeter
     *
     * @return the scale of a pixel
     */
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
        listeners.forEach(l -> l.onDrawingEvent(DrawingEvent.SCALE_CHANGED));
        Size stockSize = controller.getSettings().getStockSize();
        setPreferredSize(new Dimension((int) ((stockSize.getWidth() + (margin * 2)) * scale), (int) ((stockSize.getHeight() + (margin * 2)) * scale)));
        repaint();
    }

    public void addListener(DrawingListener listener) {
        listeners.add(listener);
    }

    public AffineTransform getTransform() {
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -getHeight());
        transform.translate(margin, margin);
        transform.scale(scale, scale);
        return transform;
    }

    public void removeListener(DrawingListener drawingListener) {
        listeners.remove(drawingListener);
    }
}
