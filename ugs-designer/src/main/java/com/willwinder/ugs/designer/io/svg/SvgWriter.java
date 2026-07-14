/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.svg;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.io.DesignWriter;
import com.willwinder.ugs.designer.io.DesignWriterException;
import com.willwinder.ugs.designer.logic.Controller;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Writes a design as a Scalable Vector Graphics (SVG) file.
 * <p>
 * The document uses millimeters as unit, with a viewBox in the same scale so that one user unit
 * equals one millimeter. All geometry is wrapped in a group that flips the Y-axis, mirroring the
 * coordinate handling in {@link SvgReader}, so that a written design can be read back again.
 *
 * @author Joacim Breiler
 */
public class SvgWriter implements DesignWriter {
    public static final String SVG_EXTENSION = ".svg";
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private final DecimalFormat decimalFormat = new DecimalFormat("0.####", new DecimalFormatSymbols(Locale.US));

    @Override
    public void write(File file, Controller controller) {
        try {
            write(new FileOutputStream(file), controller);
        } catch (FileNotFoundException e) {
            throw new DesignWriterException("Could not write to file", e);
        }
    }

    @Override
    public void write(OutputStream outputStream, Controller controller) {
        try {
            EntityGroup root = controller.getDrawing().getRootEntity();
            Rectangle2D bounds = root.getBounds();

            Document document = createDocument();
            Element svg = createSvgElement(document, bounds);
            document.appendChild(svg);

            Element group = document.createElementNS(SVG_NAMESPACE, "g");
            group.setAttribute("transform", "translate(" + format(-bounds.getMinX()) + "," + format(bounds.getMaxY()) + ") scale(1,-1)");
            svg.appendChild(group);

            root.getChildren().forEach(child -> appendEntity(document, group, child));

            writeDocument(document, outputStream);
        } catch (DesignWriterException e) {
            throw e;
        } catch (Exception e) {
            throw new DesignWriterException("Could not write to file", e);
        }
    }

    private Element createSvgElement(Document document, Rectangle2D bounds) {
        double width = normalizeDimension(bounds.getWidth());
        double height = normalizeDimension(bounds.getHeight());

        Element svg = document.createElementNS(SVG_NAMESPACE, "svg");
        svg.setAttribute("version", "1.1");
        svg.setAttribute("width", format(width) + "mm");
        svg.setAttribute("height", format(height) + "mm");
        svg.setAttribute("viewBox", "0 0 " + format(width) + " " + format(height));
        return svg;
    }

    private void appendEntity(Document document, Element parent, Entity entity) {
        if (entity instanceof EntityGroup entityGroup) {
            Element groupElement = document.createElementNS(SVG_NAMESPACE, "g");
            applyName(groupElement, entity);
            parent.appendChild(groupElement);
            entityGroup.getChildren().forEach(child -> appendEntity(document, groupElement, child));
            return;
        }

        String pathData = toPathData(entity.getShape());
        if (StringUtils.isBlank(pathData)) {
            return;
        }

        Element path = document.createElementNS(SVG_NAMESPACE, "path");
        applyName(path, entity);
        path.setAttribute("fill", "none");
        path.setAttribute("stroke", "#000000");
        path.setAttribute("d", pathData);
        parent.appendChild(path);
    }

    private void applyName(Element element, Entity entity) {
        if (StringUtils.isNotBlank(entity.getName())) {
            element.setAttribute("desc", entity.getName());
        }
    }

    private String toPathData(Shape shape) {
        StringBuilder builder = new StringBuilder();
        PathIterator iterator = shape.getPathIterator(null);
        double[] coordinates = new double[6];
        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coordinates);
            switch (type) {
                case PathIterator.SEG_MOVETO -> builder.append("M ").append(format(coordinates[0])).append(" ").append(format(coordinates[1])).append(" ");
                case PathIterator.SEG_LINETO -> builder.append("L ").append(format(coordinates[0])).append(" ").append(format(coordinates[1])).append(" ");
                case PathIterator.SEG_QUADTO -> builder.append("Q ").append(format(coordinates[0])).append(" ").append(format(coordinates[1])).append(" ").append(format(coordinates[2])).append(" ").append(format(coordinates[3])).append(" ");
                case PathIterator.SEG_CUBICTO -> builder.append("C ").append(format(coordinates[0])).append(" ").append(format(coordinates[1])).append(" ").append(format(coordinates[2])).append(" ").append(format(coordinates[3])).append(" ").append(format(coordinates[4])).append(" ").append(format(coordinates[5])).append(" ");
                case PathIterator.SEG_CLOSE -> builder.append("Z ");
                default -> { }
            }
            iterator.next();
        }
        return builder.toString().trim();
    }

    private double normalizeDimension(double value) {
        return Double.isFinite(value) && value > 0 ? value : 0;
    }

    private String format(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        String result = decimalFormat.format(value);
        return "-0".equals(result) ? "0" : result;
    }

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    private void writeDocument(Document document, OutputStream outputStream) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(document), new StreamResult(outputStream));
    }
}
