package com.willwinder.universalgcodesender.utils;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.awt.NullPlatformSupport;
import org.apache.commons.lang3.StringUtils;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.io.InputStream;
import java.util.Optional;

public class SvgIconLoader {

    public static final int SIZE_SMALL = 16;
    public static final int SIZE_MEDIUM = 24;

    private static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N
    }

    public static Optional<ImageIcon> loadIcon(String icon, int size) {
        if (StringUtils.isEmpty(icon)) {
            return Optional.empty();
        }

        Optional<ImageIcon> result = Optional.empty();
        if (isDarkLaF()) {
            result = loadIconImageIcon(icon.replace(".svg", "_dark.svg"), size);
        }

        if (result.isEmpty()) {
            result = loadIconImageIcon(icon, size);
        }

        return result;
    }

    private static Optional<ImageIcon> loadIconImageIcon(String icon, int size) {
        try (InputStream resourceAsStream =
                     SvgIconLoader.class.getResourceAsStream("/" + icon)) {

            if (resourceAsStream == null) {
                return Optional.empty();
            }

            SVGLoader loader = new SVGLoader();
            SVGDocument svgDocument = loader.load(resourceAsStream);

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            assert svgDocument != null;
            svgDocument.renderWithPlatform(
                    NullPlatformSupport.INSTANCE,
                    g2d,
                    new ViewBox(0, 0, size, size)
            );

            g2d.dispose();

            return Optional.of(new ImageIcon(image));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<ImageIcon> loadImageIcon(String icon, int size) {
        return loadIcon(icon, size).map(iconImage -> {
            Image img = iconImage.getImage();

            BufferedImage src = new BufferedImage(
                    img.getWidth(null),
                    img.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g = src.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return new ImageIcon(src);
        });
    }

    public static ImageIcon createDisabledIcon(BufferedImage source) {
        ImageProducer producer = new FilteredImageSource(
                source.getSource(),
                isDarkLaF() ? DisabledIconFilter.INSTANCE_DARK : DisabledIconFilter.INSTANCE_LIGHT
        );
        Image image = Toolkit.getDefaultToolkit().createImage(producer);
        return new ImageIcon(image);
    }

    public static BufferedImage toBufferedImage(ImageIcon icon) {
        Image img = icon.getImage();

        BufferedImage result = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        result.getGraphics().drawImage(img, 0, 0, null);
        result.getGraphics().dispose();

        return result;
    }

    /**
     * Replace non-transparent pixels with the target color
     * while preserving alpha (similar to JavaFX Blend SRC_ATOP behavior).
     */
    private static BufferedImage recolorImage(BufferedImage src, Color color) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int targetRGB = color.getRGB() & 0x00FFFFFF;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = src.getRGB(x, y);

                int alpha = (argb >> 24) & 0xFF;

                if (alpha == 0) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    int newPixel = (alpha << 24) | targetRGB;
                    result.setRGB(x, y, newPixel);
                }
            }
        }

        return result;
    }

    public static Optional<SVGDocument> loadSvgResource(String icon) {
        if (StringUtils.isEmpty(icon)) {
            return Optional.empty();
        }

        InputStream resourceAsStream = SvgIconLoader.class.getResourceAsStream("/" + icon);
        if (resourceAsStream == null) {
            return Optional.empty();
        }

        SVGLoader loader = new SVGLoader();
        return Optional.ofNullable(loader.load(resourceAsStream));
    }
}