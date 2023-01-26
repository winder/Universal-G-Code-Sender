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

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.Throttler;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.entities.controls.CreateEllipseControl;
import com.willwinder.ugs.nbp.designer.entities.controls.CreatePointControl;
import com.willwinder.ugs.nbp.designer.entities.controls.CreateRectangleControl;
import com.willwinder.ugs.nbp.designer.entities.controls.CreateTextControl;
import com.willwinder.ugs.nbp.designer.entities.controls.EditTextControl;
import com.willwinder.ugs.nbp.designer.entities.controls.GridControl;
import com.willwinder.ugs.nbp.designer.entities.controls.HighlightModelControl;
import com.willwinder.ugs.nbp.designer.entities.controls.Location;
import com.willwinder.ugs.nbp.designer.entities.controls.MoveControl;
import com.willwinder.ugs.nbp.designer.entities.controls.ResizeControl;
import com.willwinder.ugs.nbp.designer.entities.controls.RotationControl;
import com.willwinder.ugs.nbp.designer.entities.controls.SelectionControl;
import com.willwinder.ugs.nbp.designer.entities.controls.ZoomControl;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;

/**
 * @author Joacim Breiler
 */
public class Drawing extends JPanel {

    public static final double MIN_SCALE = 0.05;
    private static final long serialVersionUID = 1298712398723987873L;
    private static final int MARGIN = 100;
    private final transient EntityGroup globalRoot;
    private final transient EntityGroup entitiesRoot;
    private final transient EntityGroup controlsRoot;
    private final transient Set<DrawingListener> listeners = Sets.newConcurrentHashSet();
    private final transient Throttler refreshThrottler;
    private double scale;

    public Drawing(Controller controller) {
        refreshThrottler = new Throttler(this::refresh, 2000);

        globalRoot = new EntityGroup();
        globalRoot.addChild(new GridControl(controller));
        globalRoot.addListener(event -> refreshThrottler.run());

        entitiesRoot = new EntityGroup();
        globalRoot.addChild(entitiesRoot);
        globalRoot.addChild(controller.getSelectionManager());

        controlsRoot = new EntityGroup();
        globalRoot.addChild(controlsRoot);
        controlsRoot.addChild(new ResizeControl(controller, Location.TOP));
        controlsRoot.addChild(new ResizeControl(controller, Location.LEFT));
        controlsRoot.addChild(new ResizeControl(controller, Location.RIGHT));
        controlsRoot.addChild(new ResizeControl(controller, Location.BOTTOM));
        controlsRoot.addChild(new ResizeControl(controller, Location.BOTTOM_LEFT));
        controlsRoot.addChild(new ResizeControl(controller, Location.BOTTOM_RIGHT));
        controlsRoot.addChild(new ResizeControl(controller, Location.TOP_LEFT));
        controlsRoot.addChild(new ResizeControl(controller, Location.TOP_RIGHT));
        controlsRoot.addChild(new HighlightModelControl(controller.getSelectionManager()));
        controlsRoot.addChild(new MoveControl(controller));
        controlsRoot.addChild(new RotationControl(controller.getSelectionManager()));
        controlsRoot.addChild(new SelectionControl(controller));
        controlsRoot.addChild(new CreatePointControl(controller));
        controlsRoot.addChild(new CreateRectangleControl(controller));
        controlsRoot.addChild(new CreateEllipseControl(controller));
        controlsRoot.addChild(new CreateTextControl(controller));
        controlsRoot.addChild(new EditTextControl(controller));
        controlsRoot.addChild(new ZoomControl(controller));

        setBackground(Colors.BACKGROUND);
        setScale(2);
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

    public List<Entity> getEntitiesIntersecting(Shape shape) {
        return globalRoot.getChildrenIntersecting(shape);
    }

    public void insertEntity(Entity entity) {
        entitiesRoot.addChild(entity);
        notifyListeners(DrawingEvent.ENTITY_ADDED);
    }

    public void notifyListeners(DrawingEvent event) {
        listeners.forEach(l -> l.onDrawingEvent(event));
        refresh();
    }

    public void insertEntities(List<Entity> entities) {
        entities.forEach(entitiesRoot::addChild);
        notifyListeners(DrawingEvent.ENTITY_ADDED);
    }

    public List<Entity> getEntities() {
        List<Entity> result = new ArrayList<>();
        entitiesRoot.getChildren().forEach(shape -> recursiveCollectEntities(shape, result));
        return result;
    }

    public EntityGroup getRootEntity() {
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
        globalRoot.render(g2, this);
        g2.setTransform(previousTransform);
    }

    public void removeEntity(Entity entity) {
        removeEntities(Collections.singletonList(entity));
    }

    public void removeEntities(List<Entity> entities) {
        removeEntitiesRecursively(globalRoot, entities);
        ThreadHelper.invokeLater(() -> listeners.forEach(l -> l.onDrawingEvent(DrawingEvent.ENTITY_REMOVED)));
        refresh();
    }

    private void removeEntitiesRecursively(EntityGroup parent, List<Entity> entities) {
        parent.getChildren().forEach(child -> {
            if (child instanceof EntityGroup) {
                removeEntitiesRecursively((EntityGroup) child, entities);
            }
        });

        entities.forEach(parent::removeChild);
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
        double newScale = Math.max(Math.abs(scale), MIN_SCALE);
        if (this.scale != newScale) {
            this.scale = newScale;
            notifyListeners(DrawingEvent.SCALE_CHANGED);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        Rectangle2D bounds = globalRoot.getBounds();
        return new Dimension((int) (bounds.getMaxX() * scale) + (MARGIN * 2), (int) (bounds.getMaxY() * scale) + (MARGIN * 2));
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    private void refresh() {
        repaint();
        Dimension minimumSize = getMinimumSize();
        firePropertyChange("minimumSize", minimumSize.width, minimumSize.height);
        firePropertyChange("preferredSize", minimumSize.width, minimumSize.height);
        revalidate();
    }

    public void addListener(DrawingListener listener) {
        listeners.add(listener);
    }

    public AffineTransform getTransform() {
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -getHeight());
        transform.translate(MARGIN, MARGIN);
        transform.scale(scale, scale);
        return transform;
    }

    public void removeListener(DrawingListener drawingListener) {
        listeners.remove(drawingListener);
    }

    public List<Control> getControls() {
        return controlsRoot.getAllChildren().stream()
                .filter(Control.class::isInstance)
                .map(Control.class::cast)
                .collect(Collectors.toList());
    }

    public void clear() {
        entitiesRoot.removeAll();
    }
}
