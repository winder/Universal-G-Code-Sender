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
}
