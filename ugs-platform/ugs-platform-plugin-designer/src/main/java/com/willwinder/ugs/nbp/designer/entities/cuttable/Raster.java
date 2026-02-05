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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.universalgcodesender.utils.MathUtils;
import static com.willwinder.universalgcodesender.utils.MathUtils.clamp;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class Raster extends AbstractCuttable {

    private BufferedImage image;
    private Rectangle2D.Double relativeShape;

    // brightness: -1..+1 (adds to normalized luminance)
    private double brightness = 0.0;
    // contrast: 0..3 (multiplies around midpoint 0.5)
    private double contrast = 1.0;
    // gamma: 0.1..10 (applied as pow(l, 1/gamma))
    private double gamma = 1.0;
    // The number of intensities to use
    private int levels = 255;

    private boolean invert = false;
    private transient BufferedImage processedGray; // cached grayscale image
    private transient BufferedImage processedInkMask;  // cached ARGB: black color with alpha derived from gray

    public Raster(BufferedImage image) {
        super(0, 0);
        setImage(image);
    }

    public Raster(File imageFile) {
        super(0, 0);

        try {
            setImage(ImageIO.read(imageFile));
        } catch (IOException ignored) {
            // If load fails, we still create an entity with a tiny placeholder shape.
        }

        setName(imageFile.getName());
    }

    private void setImage(BufferedImage image) {
        this.image = image;

        double w = (image != null) ? image.getWidth() : 1.0;
        double h = (image != null) ? image.getHeight() : 1.0;
        this.relativeShape = new Rectangle2D.Double(0, 0, w, h);
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (isHidden()) {
            return;
        }

        if (image != null) {
            BufferedImage inkMask = getOrCreateProcessedInkMask();

            double targetW = Math.max(0.0001, relativeShape.getWidth());
            double targetH = Math.max(0.0001, relativeShape.getHeight());

            double sx = targetW / (double) inkMask.getWidth();
            double sy = targetH / (double) inkMask.getHeight();

            // Drawing world is Y-up; images are Y-down => flip Y to show right-side-up
            AffineTransform imgLocal = new AffineTransform();
            imgLocal.scale(sx, -sy);
            imgLocal.translate(0, -inkMask.getHeight());

            AffineTransform tx = new AffineTransform(getTransform());
            tx.concatenate(imgLocal);

            graphics.drawImage(inkMask, tx, null);
        }

        super.render(graphics, drawing);
    }

    @Override
    public Shape getRelativeShape() {
        return relativeShape;
    }

    @Override
    public Entity copy() {
        Raster copy = new Raster(this.image);
        copyPropertiesTo(copy);

        copy.brightness = this.brightness;
        copy.contrast = this.contrast;
        copy.gamma = this.gamma;
        copy.invert = this.invert;
        copy.invalidateProcessedCache();

        return copy;
    }

    @Override
    public List<EntitySetting> getSettings() {
        List<EntitySetting> settings = super.getSettings();
        settings = new java.util.ArrayList<>(settings);
        settings.addAll(Arrays.asList(
                EntitySetting.RASTER_BRIGHTNESS,
                EntitySetting.RASTER_CONTRAST,
                EntitySetting.RASTER_GAMMA,
                EntitySetting.RASTER_INVERT,
                EntitySetting.RASTER_LEVELS
        ));
        return settings;
    }

    @Override
    public Optional<Object> getEntitySetting(EntitySetting entitySetting) {
        return switch (entitySetting) {
            case RASTER_BRIGHTNESS -> Optional.of(brightness);
            case RASTER_CONTRAST -> Optional.of(contrast);
            case RASTER_GAMMA -> Optional.of(gamma);
            case RASTER_INVERT -> Optional.of(invert);
            case RASTER_LEVELS -> Optional.of(levels);
            default -> super.getEntitySetting(entitySetting);
        };
    }

    @Override
    public void setEntitySetting(EntitySetting entitySetting, Object value) {
        switch (entitySetting) {
            case RASTER_BRIGHTNESS -> setBrightness(asDouble(value));
            case RASTER_CONTRAST -> setContrast(asDouble(value));
            case RASTER_GAMMA -> setGamma(asDouble(value));
            case RASTER_INVERT -> setInvert(asBoolean(value));
            case RASTER_LEVELS -> setLevels(asInteger(value));
            default -> super.setEntitySetting(entitySetting, value);
        }
    }

    public double getBrightness() {
        return brightness;
    }

    public void setBrightness(double rasterBrightness) {
        this.brightness = clamp(rasterBrightness, -1.0, 1.0);
        invalidateProcessedCache();
    }

    public double getContrast() {
        return contrast;
    }

    public void setContrast(double rasterContrast) {
        this.contrast = clamp(rasterContrast, 0.0, 3.0);
        invalidateProcessedCache();
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double rasterGamma) {
        this.gamma = clamp(rasterGamma, 0.1, 10.0);
        invalidateProcessedCache();
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = MathUtils.clamp(levels, 2, 255);
        invalidateProcessedCache();
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
        invalidateProcessedCache();
    }

    private void invalidateProcessedCache() {
        processedGray = null;
        processedInkMask = null;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    private BufferedImage getOrCreateProcessedInkMask() {
        if (processedInkMask != null) {
            return processedInkMask;
        }

        BufferedImage gray = getOrCreateProcessedGray();
        BufferedImage mask = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {
                int argb = gray.getRGB(x, y);
                int v = argb & 0xFF; // gray pixel intensity 0..255

                int a = 255 - v; // white -> transparent, black -> opaque
                int out = (a << 24); // black RGB = 0, alpha = a
                mask.setRGB(x, y, out);
            }
        }

        processedInkMask = mask;
        return processedInkMask;
    }


    private BufferedImage getOrCreateProcessedGray() {
        if (processedGray != null) {
            return processedGray;
        }

        BufferedImage src = image;
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        final double contrast = this.contrast;
        final double brightness = this.brightness;
        final double invGamma = 1.0 / Math.max(0.0001, gamma);
        final boolean invert = this.invert;

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = (argb) & 0xFF;

                double l = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;

                l = l + brightness;
                l = (l - 0.5) * contrast + 0.5;
                l = clamp(l, 0.0, 1.0);

                l = Math.pow(l, invGamma);

                if (invert) {
                    l = 1.0 - l;
                }

                double ql = clamp(l, 0.0, 1.0);
                if (levels < 255) {
                    double steps = levels - 1;
                    ql = Math.round(ql * steps) / steps;
                }

                int lv = (int) Math.round(ql * 255.0);

                int grayArgb = (0xFF << 24) | (lv << 16) | (lv << 8) | lv;
                dst.setRGB(x, y, grayArgb);
            }
        }

        processedGray = dst;
        return processedGray;
    }


    private static double asDouble(Object value) {
        if (value instanceof Double d) return d;
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Float f) return f.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }

    private static int asInteger(Object value) {
        if (value instanceof Double d) return d.intValue();
        if (value instanceof Integer i) return i;
        if (value instanceof Float f) return f.intValue();
        return Integer.parseInt(String.valueOf(value));
    }


    private static boolean asBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    /**
     * Returns the image data as a Base64 encoded PNG
     *
     * @return the image as base64 encoded string
     */
    public String getImageData() {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public double getIntensityAt(Point2D worldPoint) {
        if (image == null) {
            return 1;
        }

        // Invert entity transform: world → local
        AffineTransform inv;
        try {
            inv = getTransform().createInverse();
        } catch (NoninvertibleTransformException e) {
            return 1;
        }

        Point2D.Double local = new Point2D.Double();
        inv.transform(worldPoint, local);

        // Check bounds in local raster space
        if (!relativeShape.contains(local)) {
            return 1;
        }

        BufferedImage gray = getOrCreateProcessedGray();

        // Map local coordinates → image pixel coordinates
        double nx = (local.x - relativeShape.x) / relativeShape.width;
        double ny = (local.y - relativeShape.y) / relativeShape.height;

        int px = (int) Math.floor(nx * gray.getWidth());
        int py = (int) Math.floor((1.0 - ny) * gray.getHeight()); // Y-up → Y-down

        if (px < 0 || px >= gray.getWidth() || py < 0 || py >= gray.getHeight()) {
            return 1;
        }

        int argb = gray.getRGB(px, py);
        int v = argb & 0xFF; // gray value

        return v / 255.0;
    }

    @Override
    public List<CutType> getAvailableCutTypes() {
        return List.of(CutType.LASER_RASTER, CutType.LASER_ON_PATH, CutType.ON_PATH, CutType.INSIDE_PATH, CutType.OUTSIDE_PATH, CutType.SURFACE);
    }
}
