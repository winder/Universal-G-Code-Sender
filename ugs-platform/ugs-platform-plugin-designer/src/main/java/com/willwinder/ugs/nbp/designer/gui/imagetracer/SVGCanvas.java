/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.imagetracer;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * @author Joacim Breiler
 */
public class SVGCanvas extends JSVGCanvas {
    public SVGCanvas() {
        super();
    }

    @Override
    protected AffineTransform calculateViewingTransform(String svgElementIdentifier, SVGSVGElement svgElement) {
        SVGRect svgElementBounds = svgElement.getBBox();
        float[] svgElementBoundsVector = new float[]{
                svgElementBounds.getX(),
                svgElementBounds.getY(),
                svgElementBounds.getWidth(),
                svgElementBounds.getHeight()
        };

        return ViewBox.getPreserveAspectRatioTransform(
                svgElementBoundsVector,
                SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID,
                true,
                getWidth(),
                getHeight()
        );
    }

    public void setSvgData(String generatedSvgData) {
        try{
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            SVGDocument document = factory.createSVGDocument("", new ByteArrayInputStream(generatedSvgData.getBytes(Charset.defaultCharset())));
            setSVGDocument(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}