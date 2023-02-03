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
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Joacim Breiler
 */
public class SvgReader implements DesignReader {

    private static final Logger LOGGER = Logger.getLogger(SvgReader.class.getSimpleName());

    @Override
    public Optional<Design> read(File f) {
        if (EventQueue.isDispatchThread()) {
            throw new DesignReaderException("Method can not be executed in dispatch thread");
        }
        try {
            return read(new FileInputStream(f));
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't load stream");
        }
    }

    @Override
    public Optional<Design> read(InputStream inputStream) {
        if (EventQueue.isDispatchThread()) {
            throw new DesignReaderException("Method can not be executed in dispatch thread");
        }

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        Group result;

        try {
            SVGOMDocument doc = (SVGOMDocument) f.createSVGDocument(null, inputStream);
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
            bridgeContext.setDynamicState(BridgeContext.DYNAMIC);

            // Enable CSS- and SVG-specific enhancements.
            (new GVTBuilder()).build(bridgeContext, doc);
            result = parseGraphicsNode(doc, bridgeContext);
        } catch (IOException ex) {
            throw new DesignReaderException("Couldn't load stream");
        }

        Design design = new Design();
        design.setEntities(result.getChildren());
        return Optional.of(design);
    }

    private Group parseGraphicsNode(SVGOMDocument node, BridgeContext bridgeContext) {
        Group group = new Group();
        walk(node, group, 0, bridgeContext);

        // We need to invert the Y coordinate and apply global pixel scale
        double pixelUnitToMM = node.getSVGContext().getPixelUnitToMillimeter();
        AffineTransform transform = new AffineTransform();
        transform.scale(1, -1); // Invert Y-coordinate
        transform.scale(pixelUnitToMM, pixelUnitToMM);
        group.applyTransform(transform);
        group.move(new Point2D.Double(-group.getPosition().getX(), -group.getPosition().getY()));

        return group;
    }


    private void walk(Node node, Group parent, int level, BridgeContext bridgeContext) {
        GraphicsNode graphicsNode = bridgeContext.getGraphicsNode(node);

        if (graphicsNode != null) {
            LOGGER.finest(StringUtils.leftPad("", level, "\t") + graphicsNode);
            if (graphicsNode instanceof CompositeGraphicsNode) {
                parseGroupNode(node, parent, level, bridgeContext, (CompositeGraphicsNode) graphicsNode);
            } else if (graphicsNode instanceof ShapeNode) {
                parseShapeNode(node, parent, (ShapeNode) graphicsNode);
            }
        }
    }

    private void parseShapeNode(Node node, Group parent, ShapeNode shapeNode) {
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
            Node desc = node.getAttributes().getNamedItem("desc");
            if (desc != null) {
                createdShape.setDescription(desc.getFirstChild().getNodeValue());
            }


            if (shapeNode.getTransform() != null) {
                createdShape.applyTransform(shapeNode.getTransform());
            }
            parent.addChild(createdShape);
        }
    }

    private void parseGroupNode(Node node, Group parent, int level, BridgeContext bridgeContext, CompositeGraphicsNode graphicsNode) {
        NodeList childNodes = node.getChildNodes();

        // Only add a child group if the group contains more than 1 child
        Group group = parent;
        if (graphicsNode.getChildren().size() > 1) {
            group = new Group();
            if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                Node id = node.getAttributes().getNamedItem("id");
                group.setName(group.getName() + " (" + id.getFirstChild().getNodeValue() + ")");
            }
            parent.addChild(group);
        }

        for (int i = 0; i < childNodes.getLength(); i++) {
            walk(childNodes.item(i), group, level + 1, bridgeContext);
        }

        if (graphicsNode.getTransform() != null) {
            group.applyTransform(graphicsNode.getTransform());
        }
    }

    private AbstractEntity parseEllipse(Ellipse2D shape) {
        Ellipse circle = new Ellipse(shape.getX(), shape.getY());
        circle.setSize(new Size(shape.getWidth(), shape.getHeight()));
        return circle;
    }

    private AbstractEntity parseRectangle(Rectangle2D shape) {
        Rectangle rectangle = new Rectangle(shape.getX(), shape.getY());
        rectangle.setSize(new Size(shape.getWidth(), shape.getHeight()));
        return rectangle;
    }

    private AbstractEntity parsePath(ExtendedGeneralPath shape) {
        ExtendedPathIterator extendedPathIterator = shape.getExtendedPathIterator();
        double[] coords = new double[8];
        double[] lastMoveTo = new double[2];
        double[] lastPoint = new double[2];
        Path line = new Path();

        while (!extendedPathIterator.isDone()) {
            int i = extendedPathIterator.currentSegment();
            switch (i) {
                case ExtendedPathIterator.SEG_MOVETO:
                    extendedPathIterator.currentSegment(coords);
                    line.moveTo(coords[0], coords[1]);

                    lastMoveTo[0] = coords[0];
                    lastMoveTo[1] = coords[1];
                    lastPoint[0] = coords[0];
                    lastPoint[1] = coords[1];
                    break;

                case ExtendedPathIterator.SEG_LINETO:
                    extendedPathIterator.currentSegment(coords);
                    line.lineTo(coords[0], coords[1]);
                    lastPoint[0] = coords[0];
                    lastPoint[1] = coords[1];
                    break;

                case ExtendedPathIterator.SEG_QUADTO:
                    extendedPathIterator.currentSegment(coords);
                    line.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    lastPoint[0] = coords[2];
                    lastPoint[1] = coords[3];
                    break;

                /*
                 *  The segment type constant for an elliptical arc.  This consists of
                 *  Seven values [rx, ry, angle, largeArcFlag, sweepFlag, x, y].
                 *  rx, ry are the radius of the ellipse.
                 *  angle is angle of the x axis of the ellipse.
                 *  largeArcFlag is zero if the smaller of the two arcs are to be used.
                 *  sweepFlag is zero if the 'left' branch is taken one otherwise.
                 *  x and y are the destination for the ellipse.
                 */
                case ExtendedPathIterator.SEG_ARCTO:
                    extendedPathIterator.currentSegment(coords);
                    double rx = coords[0];
                    double ry = coords[1];
                    double angle = coords[2];
                    boolean largeArcFlag = coords[3] >= 1;
                    boolean sweepFlag = coords[4] >= 1;
                    double x = coords[5];
                    double y = coords[6];

                    // If the radius is zero, just make a line
                    if (rx == 0 || ry == 0) {
                        line.lineTo(x, y);
                        break;
                    }

                    // Get the current coordinates of the path
                    double x0 = lastPoint[0];
                    double y0 = lastPoint[1];

                    // If the endpoints (x, y) and (x0, y0) are identical, then this is not an arc
                    if (x0 == x && y0 == y) {
                        break;
                    }

                    Arc2D arc = ExtendedGeneralPath.computeArc(x0, y0, rx, ry, angle, largeArcFlag, sweepFlag, x, y);
                    line.append(arc);
                    lastPoint[0] = coords[5];
                    lastPoint[1] = coords[6];
                    break;

                case PathIterator.SEG_CUBICTO:
                    extendedPathIterator.currentSegment(coords);
                    line.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    lastPoint[0] = coords[4];
                    lastPoint[1] = coords[5];
                    break;

                case PathIterator.SEG_CLOSE:
                    extendedPathIterator.currentSegment(coords);
                    line.lineTo(lastMoveTo[0], lastMoveTo[1]);
                    lastPoint[0] = lastMoveTo[0];
                    lastPoint[1] = lastMoveTo[1];
                    line.close();
                    break;

                default:
                    LOGGER.warning("Missing handler for path segment: " + i);
            }
            extendedPathIterator.next();
        }
        return line;
    }
}
