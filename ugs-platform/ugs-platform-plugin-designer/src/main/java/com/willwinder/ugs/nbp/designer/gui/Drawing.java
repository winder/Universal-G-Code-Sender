package com.willwinder.ugs.nbp.designer.gui;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.Group;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.RenderingHints.*;

public class Drawing extends JPanel implements Iterable<Entity> {

    private static final long serialVersionUID = 0;

    private Group root;

    public Drawing() {
        root = new Group();
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

    public List<Entity> getShapesAt(Point2D p) {
        return getShapesAt(root.getShapes(), p);
    }

    private List<Entity> getShapesAt(Collection<Entity> shapes, Point2D p) {
        return shapes.stream()
                .flatMap(s -> s.getChildrenAt(p).stream()).collect(Collectors.toList());
    }

    public void insertShape(Entity s) {
        s.setParent(root);
        root.addChild(s);
    }

    @Override
    public Iterator<Entity> iterator() {
        return root.getShapes().iterator();
    }

    public List<Entity> getShapes() {
        List<Entity> result = new ArrayList<>();
        root.getShapes().forEach(shape -> collectAll(shape, result));
        return result;
    }

    public void collectAll(Entity shape, List<Entity> result) {
        result.add(shape);

        List<Entity> shapes = shape.getShapes();
        shapes.forEach(shape1 -> {
            collectAll(shape1, result);
        });
    }

    public void listShapes() {
        System.out.println("---");
        for (Entity s : root.getShapes()) {
            System.out.println(s);
        }
        System.out.println("---");
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.getTransform().concatenate(AffineTransform.getScaleInstance(1, -1));

        RenderingHints rh = ((Graphics2D) g).getRenderingHints();
        rh.put(KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        rh.put(KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(rh);

        for (Entity s : root.getShapes()) {
            s.draw(g2);
        }

        g2.getTransform().concatenate(AffineTransform.getScaleInstance(1, -1));
    }

    public void removeShape(Entity s) {
        root.removeChild(s);
    }
}
