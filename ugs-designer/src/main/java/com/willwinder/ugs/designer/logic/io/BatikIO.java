package com.willwinder.ugs.designer.logic.io;


import com.willwinder.ugs.designer.logic.Controller;

import com.willwinder.ugs.designer.entities.Ellipse;
import com.willwinder.ugs.designer.entities.Group;
import com.willwinder.ugs.designer.entities.Path;
import com.willwinder.ugs.designer.entities.Rectangle;
import com.willwinder.ugs.designer.entities.Entity;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.logging.Logger;

public class BatikIO implements GVTTreeBuilderListener, Reader {

    private JSVGCanvas svgCanvas;
    private Controller controller;
    private static final Logger LOGGER = Logger.getLogger(BatikIO.class.getSimpleName());

    @Override
    public void open(File f, Controller c) {
        this.controller = c;
        svgCanvas = new JSVGCanvas();
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        svgCanvas.addGVTTreeBuilderListener(this);
        svgCanvas.setURI(f.toURI().toString());
    }

    private void walk(Node node, Entity parent, AffineTransform transform, int level) {
        GraphicsNode graphicsNode = svgCanvas.getUpdateManager().getBridgeContext().getGraphicsNode(node);

        if (graphicsNode != null) {
            LOGGER.finest(StringUtils.leftPad("", level, "\t") + graphicsNode);

            AffineTransform groupTransform = new AffineTransform(transform);
            if(graphicsNode.getTransform() != null) {
                groupTransform.concatenate(graphicsNode.getTransform());
            }

            if (graphicsNode instanceof CompositeGraphicsNode ) {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    walk(childNodes.item(i), parent, groupTransform, level + 1);
                }
            } else if (graphicsNode instanceof ShapeNode) {
                ShapeNode shapeNode = (ShapeNode) graphicsNode;
                java.awt.Shape shape = shapeNode.getShape();
                Entity createdShape = null;

                if (shape instanceof ExtendedGeneralPath) {
                    createdShape = parsePath((ExtendedGeneralPath) shape);
                } else if (shape instanceof Rectangle2D) {
                    createdShape = parseRectangle((Rectangle2D) shape);
                } else if (shape instanceof Ellipse2D) {
                    createdShape = parseEllipse((Ellipse2D) shape);
                } else {
                    LOGGER.finest(shape.toString());
                }

                if(createdShape != null) {
                    createdShape.setParent(parent);
                    createdShape.setTransform(groupTransform);
                    parent.add(createdShape);
                }
            }
        }
    }

    private Entity parseEllipse(Ellipse2D shape) {
        Ellipse circle = new Ellipse((int) shape.getX(), (int) shape.getY());
        circle.setSize(new Point((int) shape.getWidth(), (int) shape.getHeight()));
        return circle;
    }

    private Entity parseRectangle(Rectangle2D shape) {
        Rectangle rectangle = new Rectangle(shape.getX(), shape.getY());
        rectangle.setWidth(shape.getWidth());
        rectangle.setHeight(shape.getHeight());
        return rectangle;
    }

    private Entity parsePath(ExtendedGeneralPath shape) {
        ExtendedPathIterator extendedPathIterator = shape.getExtendedPathIterator();
        double[] coords = new double[8];

        Path line = new Path();

        while (!extendedPathIterator.isDone()) {
            int i = extendedPathIterator.currentSegment();
            switch (i) {
                case ExtendedPathIterator.SEG_MOVETO:
                    extendedPathIterator.currentSegment(coords);
                    line.moveTo(coords[0], coords[1]);
                    break;

                case ExtendedPathIterator.SEG_LINETO:
                    extendedPathIterator.currentSegment(coords);
                    line.lineTo(coords[0], coords[1]);
                    break;

                case ExtendedPathIterator.SEG_QUADTO:
                    extendedPathIterator.currentSegment(coords);
                    line.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;

                case ExtendedPathIterator.SEG_ARCTO:
                    extendedPathIterator.currentSegment(coords);
                    line.lineTo(coords[5], coords[6]);
                    break;

                case PathIterator.SEG_CUBICTO:
                    extendedPathIterator.currentSegment(coords);
                    line.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;

                case PathIterator.SEG_CLOSE:
            }
            extendedPathIterator.next();
        }
        return line;
    }

    @Override
    public void gvtBuildStarted(GVTTreeBuilderEvent e) {

    }

    @Override
    public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
        Group group = new Group();
        walk(svgCanvas.getSVGDocument(), group, new AffineTransform(), 0);

        controller.newDrawing();
        controller.getDrawing().insertShape(group);
        controller.getDrawing().repaint();
    }

    @Override
    public void gvtBuildCancelled(GVTTreeBuilderEvent e) {

    }

    @Override
    public void gvtBuildFailed(GVTTreeBuilderEvent e) {

    }
}
