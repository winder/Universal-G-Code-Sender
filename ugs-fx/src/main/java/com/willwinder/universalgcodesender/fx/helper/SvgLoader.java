package com.willwinder.universalgcodesender.fx.helper;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.awt.NullPlatformSupport;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

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
}
