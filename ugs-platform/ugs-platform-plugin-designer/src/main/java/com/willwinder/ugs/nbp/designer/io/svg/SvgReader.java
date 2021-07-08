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
package com.willwinder.ugs.nbp.designer.io.svg;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * @author Joacim Breiler
 */
public class SvgReader implements GVTTreeBuilderListener, DesignReader {

    private static final Logger LOGGER = Logger.getLogger(SvgReader.class.getSimpleName());

    private JSVGCanvas svgCanvas;
    private Group result;

    @Override
    public Optional<Design> read(File f) {
        if (EventQueue.isDispatchThread()) {
            throw new RuntimeException("Method can not be executed in dispatch thread");
        }
        result = null;
        svgCanvas = new JSVGCanvas();
        svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC);
        svgCanvas.addGVTTreeBuilderListener(this);
        svgCanvas.setURI(f.toURI().toString());

        try {
            // Wait for svg loader to finish processing the SVG
            ThreadHelper.waitUntil(() -> result != null, 10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("It took to long to load file");
            // Never mind
        }

        Design design = new Design();
        design.setEntities(result != null ? result.getChildren() : new ArrayList<>());
        return Optional.of(design);
    }

    @Override
    public Optional<Design> read(InputStream inputStream) {
        if (EventQueue.isDispatchThread()) {
            throw new RuntimeException("Method can not be executed in dispatch thread");
        }

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        SVGDocument doc = null;

        try {
            doc = f.createSVGDocument(null, inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't load stream");
        }

        result = null;
        svgCanvas = new JSVGCanvas();
        svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC);
        svgCanvas.addGVTTreeBuilderListener(this);
        svgCanvas.setSVGDocument(doc);


        try {
            // Wait for svg loader to finish processing the SVG
            ThreadHelper.waitUntil(() -> result != null, 10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("It took to long to load file");
        }

        Design design = new Design();
        design.setEntities(result != null ? result.getChildren() : new ArrayList<>());
        return Optional.of(design);
    }


    private void walk(Node node, Group group, AffineTransform transform, int level) {
        GraphicsNode graphicsNode = svgCanvas.getUpdateManager().getBridgeContext().getGraphicsNode(node);

        if (graphicsNode != null) {
            LOGGER.finest(StringUtils.leftPad("", level, "\t") + graphicsNode);

            AffineTransform groupTransform = new AffineTransform(transform);
            if (graphicsNode.getTransform() != null) {
                groupTransform.concatenate(graphicsNode.getTransform());
            }

            if (graphicsNode instanceof CompositeGraphicsNode) {
                NodeList childNodes = node.getChildNodes();

                Group childGroup = group;
                if (((CompositeGraphicsNode) graphicsNode).getChildren().size() > 1) {
                    childGroup = new Group();
                    if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                        Node id = node.getAttributes().getNamedItem("id");
                        childGroup.setName(childGroup.getName() + " (" + id.getFirstChild().getNodeValue() + ")");
                    }
                    group.addChild(childGroup);
                }

                for (int i = 0; i < childNodes.getLength(); i++) {
                    walk(childNodes.item(i), childGroup, groupTransform, level + 1);
                }
            } else if (graphicsNode instanceof ShapeNode) {
                ShapeNode shapeNode = (ShapeNode) graphicsNode;
                Shape shape = shapeNode.getShape();
                AbstractEntity createdShape = null;

                if (shape instanceof ExtendedGeneralPath) {
                    createdShape = parsePath((ExtendedGeneralPath) shape);
                } else if (shape instanceof Rectangle2D) {
                    createdShape = parseRectangle((Rectangle2D) shape);
                } else if (shape instanceof Ellipse2D) {
                    createdShape = parseEllipse((Ellipse2D) shape);
                } else {
                    LOGGER.finest(shape.toString());
                }

                if (createdShape != null) {
                    Node id = node.getAttributes().getNamedItem("id");
                    if (id != null) {
                        createdShape.setName(createdShape.getName() + " (" + id.getFirstChild().getNodeValue() + ")");
                    }
                    createdShape.setTransform(groupTransform);
                    group.addChild(createdShape);
                }
            }
        }
    }

    private AbstractEntity parseEllipse(Ellipse2D shape) {
        Ellipse circle = new Ellipse((int) shape.getX(), (int) shape.getY());
        circle.setSize(new Dimension((int) shape.getWidth(), (int) shape.getHeight()));
        return circle;
    }

    private AbstractEntity parseRectangle(Rectangle2D shape) {
        Rectangle rectangle = new Rectangle(shape.getX(), shape.getY());
        rectangle.setWidth(shape.getWidth());
        rectangle.setHeight(shape.getHeight());
        return rectangle;
    }

    private AbstractEntity parsePath(ExtendedGeneralPath shape) {
        ExtendedPathIterator extendedPathIterator = shape.getExtendedPathIterator();
        double[] coords = new double[8];
        double[] lastMoveTo = new double[2];
        Path line = new Path();

        while (!extendedPathIterator.isDone()) {
            int i = extendedPathIterator.currentSegment();
            switch (i) {
                case ExtendedPathIterator.SEG_MOVETO:
                    extendedPathIterator.currentSegment(coords);
                    line.moveTo(coords[0], coords[1]);

                    lastMoveTo[0] = coords[0];
                    lastMoveTo[1] = coords[1];
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
                    // TODO arcs are parsed as lines, this should be translated to some sort of path
                    extendedPathIterator.currentSegment(coords);
                    line.lineTo(coords[5], coords[6]);
                    break;

                case PathIterator.SEG_CUBICTO:
                    extendedPathIterator.currentSegment(coords);
                    line.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;

                case PathIterator.SEG_CLOSE:
                    extendedPathIterator.currentSegment(coords);
                    line.lineTo(lastMoveTo[0], lastMoveTo[1]);
                    break;

                default:
                    LOGGER.warning("Missing handler for path segment: " + i);
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

        // We need to invert the Y coordinate
        AffineTransform transform = new AffineTransform();
        transform.translate(0, group.getSize().height);
        transform.scale(1, -1);
        group.applyTransform(transform);

        this.result = group;
    }

    @Override
    public void gvtBuildCancelled(GVTTreeBuilderEvent e) {

    }

    @Override
    public void gvtBuildFailed(GVTTreeBuilderEvent e) {

    }
}
