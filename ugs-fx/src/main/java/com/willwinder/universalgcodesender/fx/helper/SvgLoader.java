package com.willwinder.universalgcodesender.fx.helper;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.awt.NullPlatformSupport;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Optional;

public class SvgLoader {
    public static Optional<Image> loadIcon(String icon, int size) {
        if (StringUtils.isEmpty(icon)) {
            return Optional.empty();
        }
        InputStream resourceAsStream = ActionButton.class.getResourceAsStream("/" + icon);
        SVGLoader loader = new SVGLoader();

        if (resourceAsStream == null) {
            return Optional.empty();
        }

        SVGDocument svgDocument = loader.load(resourceAsStream);
        BufferedImage awtImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = awtImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        svgDocument.renderWithPlatform(new NullPlatformSupport(), g2d, new ViewBox(0, 0, size, size));
        g2d.dispose();


        return Optional.of(SwingFXUtils.toFXImage(awtImage, null));
    }

    public static Optional<ImageView> loadImageIcon(String icon, int size) {
        return loadImageIcon(icon, size, Colors.BLACKISH);
    }


    public static Optional<ImageView> loadImageIcon(String icon, int size, Color color) {
        return loadIcon(icon, size).map(i -> {
            ImageView imageView = new ImageView(i);

            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(1.0);  // Make black white

            Blend blend = new Blend(
                    BlendMode.SRC_ATOP,
                    colorAdjust,
                    new ColorInput(0, 0, i.getWidth(), i.getHeight(), color)
            );

            imageView.setEffect(blend);
            return imageView;
        });
    }

    public static Optional<SVGDocument> loadSvgResource(String icon) {
        if (StringUtils.isEmpty(icon)) {
            return Optional.empty();
        }

        InputStream resourceAsStream = ActionButton.class.getResourceAsStream("/" + icon);
        if (resourceAsStream == null) {
            return Optional.empty();
        }

        SVGLoader loader = new SVGLoader();
        return Optional.ofNullable(loader.load(resourceAsStream));
    }

    public static Optional<SVGPath> loadSvgPath(String icon) {
        if (StringUtils.isEmpty(icon)) {
            return Optional.empty();
        }

        try (InputStream resourceAsStream = ActionButton.class.getResourceAsStream("/" + icon)) {
            if (resourceAsStream == null) {
                return Optional.empty();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document document = factory.newDocumentBuilder().parse(resourceAsStream);
            NodeList pathNodes = document.getElementsByTagName("path");

            if (pathNodes.getLength() != 1) {
                return Optional.empty();
            }

            org.w3c.dom.Node pathNode = pathNodes.item(0);
            org.w3c.dom.Node dAttribute = pathNode.getAttributes().getNamedItem("d");
            if (dAttribute == null || StringUtils.isBlank(dAttribute.getNodeValue())) {
                return Optional.empty();
            }

            SVGPath svgPath = new SVGPath();
            svgPath.setContent(dAttribute.getNodeValue());
            return Optional.of(svgPath);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
