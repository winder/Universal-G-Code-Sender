package com.willwinder.ugs.nbp.designer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

public class Surface extends JPanel implements MouseListener {

    private final Rectangle shape;
    private double rotation = 1;
    private AffineTransform affineTransform;
    private int x = 0;
    private int y = 0;
    public Surface() {
        shape = new Rectangle(0,0, 100, 100);
        affineTransform = new AffineTransform();
        addMouseListener(this);
        update();
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setPaint(new Color(150, 150, 150));
        AffineTransform transform = g2d.getTransform();


        g2d.transform(affineTransform);
        Shape transformedShape = affineTransform.createTransformedShape(shape);

        AffineTransform move = new AffineTransform();
        move.translate(x, y);
        g2d.transform(move);
        g2d.draw(transformedShape);

        g2d.setTransform(transform);
        g2d.dispose();
    }

    void update() {
        affineTransform.setToIdentity();
        affineTransform.rotate(rotation, 50, 50);
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        x += 10;
        System.out.println(affineTransform.createTransformedShape(shape).contains(e.getPoint()));
        update();
        revalidate();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

