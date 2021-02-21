package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.gui.controls.GridControl;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.entities.Group;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;

public class Drawing extends JPanel {

    private static final long serialVersionUID = 0;
    private Group globalRoot;
    private Group entitiesRoot;
    private AffineTransform scaleTransform;
    private double scale;
    private final Set<DrawingListener> listeners;

    public Drawing() {
        scale = 1;
        scaleTransform = AffineTransform.getScaleInstance(scale, scale);
        listeners = new HashSet<>();

        globalRoot = new Group();
        globalRoot.addChild(new GridControl(this));

        entitiesRoot = new Group();
        globalRoot.addChild(entitiesRoot);

        SelectionManager selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        globalRoot.addChild(selectionManager);

        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.WHITE);
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
        s.setParent(entitiesRoot);
        entitiesRoot.addChild(s);
        listeners.forEach(l -> l.onDrawingEvent(DrawingEvent.ENTITY_ADDED));
    }

    public List<Entity> getEntities() {
        List<Entity> result = new ArrayList<>();
        entitiesRoot.getChildren().forEach(shape -> recursiveCollectEntities(shape, result));
        return result;
    }

    private void recursiveCollectEntities(Entity shape, List<Entity> result) {
        result.add(shape);

        if (shape instanceof Group) {
            List<Entity> shapes = ((Group) shape).getChildren();
            shapes.forEach(s -> recursiveCollectEntities(s, result));
        }
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = ((Graphics2D) g).getRenderingHints();
        rh.put(KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        rh.put(KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(rh);
        globalRoot.render(g2);
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
        scaleTransform = AffineTransform.getScaleInstance(scale, scale);
        globalRoot.setTransform(scaleTransform);
        listeners.forEach(l -> l.onDrawingEvent(DrawingEvent.SCALE_CHANGED));
    }

    public void addListener(DrawingListener listener) {
        listeners.add(listener);
    }
}
