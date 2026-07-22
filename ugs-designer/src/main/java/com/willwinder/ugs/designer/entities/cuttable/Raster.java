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
package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityEvent;
import com.willwinder.ugs.designer.entities.EntitySetting;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.utils.DepthMapGenerator;
import com.willwinder.ugs.designer.utils.DepthMapParameters;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Raster extends AbstractCuttable {

    private static final Logger LOGGER = Logger.getLogger(Raster.class.getSimpleName());

    private static final ExecutorService DEPTH_MAP_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "depth-map-generator");
        thread.setDaemon(true);
        return thread;
    });

    private BufferedImage image;
    private Rectangle2D.Double relativeShape;

    // The number of intensities to use
    private int levels = 255;

    private boolean invert = false;

    private boolean roughing = true;
    private double stockToLeave = 0.2;

    private boolean depthMapping = false;
    private DepthMapParameters depthMapParameters = new DepthMapParameters();
    private transient volatile float[][] rawDepth;
    private transient volatile BufferedImage depthMap;
    private transient volatile boolean generatingDepthMap;

    // Power curve control points: array of [x, y] pairs mapping brightness→power
    private int[][] powerCurveControlPoints = MonotoneCubicSpline.defaultControlPoints();
    private transient int[] powerCurveLut; // cached LUT from control points

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
            // With depth mapping on we preview the actual grayscale height field, otherwise the laser ink mask
            BufferedImage preview = depthMapping ? getOrCreateProcessedGray() : getOrCreateProcessedInkMask();

            double targetW = Math.max(0.0001, relativeShape.getWidth());
            double targetH = Math.max(0.0001, relativeShape.getHeight());

            double sx = targetW / (double) preview.getWidth();
            double sy = targetH / (double) preview.getHeight();

            // Drawing world is Y-up; images are Y-down => flip Y to show right-side-up
            AffineTransform imgLocal = new AffineTransform();
            imgLocal.scale(sx, -sy);
            imgLocal.translate(0, -preview.getHeight());

            AffineTransform tx = new AffineTransform(getTransform());
            tx.concatenate(imgLocal);

            graphics.drawImage(preview, tx, null);
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

        copy.invert = this.invert;
        copy.levels = this.levels;
        copy.roughing = this.roughing;
        copy.stockToLeave = this.stockToLeave;
        copy.depthMapping = this.depthMapping;
        copy.depthMapParameters = this.depthMapParameters.copy();
        copy.rawDepth = this.rawDepth;
        copy.depthMap = this.depthMap;
        copy.powerCurveControlPoints = MonotoneCubicSpline.deepClone(this.powerCurveControlPoints);
        copy.invalidateProcessedCache();

        return copy;
    }

    @Override
    public List<EntitySetting> getSettings() {
        List<EntitySetting> settings = super.getSettings();
        settings = new java.util.ArrayList<>(settings);
        settings.addAll(Arrays.asList(
                EntitySetting.RASTER_POWER_CURVE,
                EntitySetting.RASTER_LEVELS,
                EntitySetting.RASTER_INVERT,
                EntitySetting.RASTER_DEPTH_MAPPING,
                EntitySetting.RASTER_DEPTH_DETAIL,
                EntitySetting.RASTER_DEPTH_SMOOTHING,
                EntitySetting.RASTER_DEPTH_CONTRAST,
                EntitySetting.RASTER_DEPTH_EMPHASIS
        ));
        return settings;
    }

    @Override
    public Optional<Object> getEntitySetting(EntitySetting entitySetting) {
        return switch (entitySetting) {
            case RASTER_INVERT -> Optional.of(invert);
            case RASTER_LEVELS -> Optional.of(levels);
            case RASTER_POWER_CURVE -> Optional.of(MonotoneCubicSpline.deepClone(powerCurveControlPoints));
            case ROUGHING -> Optional.of(roughing);
            case STOCK_TO_LEAVE -> Optional.of(stockToLeave);
            case RASTER_DEPTH_MAPPING -> Optional.of(depthMapping);
            case RASTER_DEPTH_DETAIL -> Optional.of(depthMapParameters.getDetail());
            case RASTER_DEPTH_SMOOTHING -> Optional.of(depthMapParameters.getSmoothing());
            case RASTER_DEPTH_CONTRAST -> Optional.of(depthMapParameters.getContrast());
            case RASTER_DEPTH_EMPHASIS -> Optional.of(depthMapParameters.getEmphasis());
            default -> super.getEntitySetting(entitySetting);
        };
    }

    @Override
    public void setEntitySetting(EntitySetting entitySetting, Object value) {
        switch (entitySetting) {
            case RASTER_INVERT -> setInvert(asBoolean(value));
            case RASTER_LEVELS -> setLevels(asInteger(value));
            case RASTER_POWER_CURVE -> setPowerCurveControlPoints(asIntArray2D(value));
            case ROUGHING -> setRoughing(asBoolean(value));
            case STOCK_TO_LEAVE -> setStockToLeave(asDouble(value));
            case RASTER_DEPTH_MAPPING -> setDepthMapping(asBoolean(value));
            case RASTER_DEPTH_DETAIL -> setDepthDetail(asDouble(value));
            case RASTER_DEPTH_SMOOTHING -> setDepthSmoothing(asDouble(value));
            case RASTER_DEPTH_CONTRAST -> setDepthContrast(asDouble(value));
            case RASTER_DEPTH_EMPHASIS -> setDepthEmphasis(asDouble(value));
            default -> super.setEntitySetting(entitySetting, value);
        }
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

    public boolean isRoughing() {
        return roughing;
    }

    public void setRoughing(boolean roughing) {
        this.roughing = roughing;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    public double getStockToLeave() {
        return stockToLeave;
    }

    public void setStockToLeave(double stockToLeave) {
        this.stockToLeave = Math.max(0, stockToLeave);
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    public boolean isDepthMapping() {
        return depthMapping;
    }

    public void setDepthMapping(boolean depthMapping) {
        this.depthMapping = depthMapping;
        invalidateProcessedCache();
        if (depthMapping) {
            requestDepthMapAsync();
        }
    }

    public double getDepthDetail() {
        return depthMapParameters.getDetail();
    }

    public void setDepthDetail(double detail) {
        depthMapParameters.setDetail(detail);
        onDepthMapParameterChanged();
    }

    public double getDepthSmoothing() {
        return depthMapParameters.getSmoothing();
    }

    public void setDepthSmoothing(double smoothing) {
        depthMapParameters.setSmoothing(smoothing);
        onDepthMapParameterChanged();
    }

    public double getDepthContrast() {
        return depthMapParameters.getContrast();
    }

    public void setDepthContrast(double contrast) {
        depthMapParameters.setContrast(contrast);
        onDepthMapParameterChanged();
    }

    public double getDepthEmphasis() {
        return depthMapParameters.getEmphasis();
    }

    public void setDepthEmphasis(double emphasis) {
        depthMapParameters.setEmphasis(emphasis);
        onDepthMapParameterChanged();
    }

    public int[][] getPowerCurveControlPoints() {
        return MonotoneCubicSpline.deepClone(powerCurveControlPoints);
    }

    public void setPowerCurveControlPoints(int[][] controlPoints) {
        if (controlPoints == null || controlPoints.length < 2) {
            this.powerCurveControlPoints = MonotoneCubicSpline.defaultControlPoints();
        } else {
            this.powerCurveControlPoints = MonotoneCubicSpline.deepClone(controlPoints);
        }
        this.powerCurveLut = null;
        invalidateProcessedCache();
    }

    private int[] getOrCreatePowerCurveLut() {
        if (powerCurveLut == null) {
            powerCurveLut = MonotoneCubicSpline.buildLut(powerCurveControlPoints);
        }
        return powerCurveLut;
    }

    private void invalidateProcessedCache() {
        processedGray = null;
        processedInkMask = null;
        powerCurveLut = null;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    private BufferedImage getSourceImage() {
        if (depthMapping && image != null) {
            BufferedImage generated = depthMap;
            if (generated != null) {
                return generated;
            }
            // Not ready yet: kick off background generation and show the original image meanwhile
            requestDepthMapAsync();
        }
        return image;
    }

    private void requestDepthMapAsync() {
        if (image == null || depthMap != null || generatingDepthMap) {
            return;
        }
        generatingDepthMap = true;
        BufferedImage sourceSnapshot = image;
        float[][] rawSnapshot = rawDepth;
        DepthMapParameters parametersSnapshot = depthMapParameters.copy();

        DEPTH_MAP_EXECUTOR.submit(() -> {
            float[][] computedRaw = rawSnapshot;
            BufferedImage result = null;
            try {
                DepthMapGenerator generator = new DepthMapGenerator();
                // The model is only needed to compute the raw estimate; a cached raw estimate can be
                // post-processed without it, so a reopened design never re-runs the model.
                if (computedRaw == null) {
                    if (generator.isModelAvailable()) {
                        computedRaw = generator.estimateDepth(sourceSnapshot);
                    } else {
                        LOGGER.warning("Depth model not available at " + generator.getModelPath()
                                + "; falling back to the original image");
                    }
                }
                if (computedRaw != null) {
                    result = generator.toDepthMap(computedRaw, sourceSnapshot, parametersSnapshot);
                }
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Failed to generate depth map, falling back to the original image", e);
            }
            depthMapGenerated(sourceSnapshot, parametersSnapshot, computedRaw, result);
        });
    }

    private void depthMapGenerated(BufferedImage source, DepthMapParameters parameters, float[][] computedRaw, BufferedImage result) {
        Runnable finish = () -> {
            generatingDepthMap = false;

            // Discard results computed from an image that has since changed
            if (source != image) {
                if (depthMapping) {
                    requestDepthMapAsync();
                }
                return;
            }

            if (computedRaw != null) {
                rawDepth = computedRaw;
            }

            // Parameters changed while generating: keep the raw estimate and re-run the cheap pass
            if (!parameters.equals(depthMapParameters)) {
                if (depthMapping) {
                    requestDepthMapAsync();
                }
                return;
            }

            if (result != null) {
                depthMap = result;
                invalidateProcessedCache();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            finish.run();
        } else {
            SwingUtilities.invokeLater(finish);
        }
    }

    /**
     * Ensures the depth map is generated synchronously so gcode generation samples the height field
     * instead of the original image. Blocks the calling thread while the model runs; a no-op unless
     * depth mapping is enabled.
     */
    public void awaitDepthMap() {
        if (!depthMapping || image == null || depthMap != null) {
            return;
        }

        try {
            DepthMapGenerator generator = new DepthMapGenerator();

            float[][] raw = rawDepth;
            if (raw == null) {
                if (!generator.isModelAvailable()) {
                    LOGGER.warning("Depth model not available at " + generator.getModelPath()
                            + "; falling back to the original image");
                    return;
                }
                raw = generator.estimateDepth(image);
                rawDepth = raw;
            }
            depthMap = generator.toDepthMap(raw, image, depthMapParameters.copy());
            processedGray = null;
            processedInkMask = null;
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to generate depth map, falling back to the original image", e);
        }
    }

    private void onDepthMapParameterChanged() {
        depthMap = null; // keep the cached raw estimate; only the cheap post-processing changed
        if (depthMapping) {
            requestDepthMapAsync();
        }
        invalidateProcessedCache();
    }

    /**
     * Returns the cached raw depth estimate as a Base64-encoded 16-bit grayscale PNG, or {@code null}
     * if it has not been generated yet. Persisting this lets a reopened design skip the expensive model
     * inference. The pipeline is scale-invariant, so the values are min-max normalized for storage.
     *
     * @return the cached raw depth as a base64 PNG, or {@code null}
     */
    public String getRawDepthData() {
        float[][] raw = rawDepth;
        if (raw == null) {
            return null;
        }

        int h = raw.length;
        int w = raw[0].length;
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (float[] row : raw) {
            for (float v : row) {
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }
        float range = max - min;
        if (range <= 0) {
            range = 1f;
        }

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
        var wr = img.getRaster();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int value = Math.round((raw[y][x] - min) / range * 65535f);
                wr.setSample(x, y, 0, Math.max(0, Math.min(65535, value)));
            }
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "png", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Restores a previously cached raw depth estimate (see {@link #getRawDepthData()}) so the model does
     * not have to run again. Ignored if the cache does not match the current image dimensions.
     *
     * @param data a base64 16-bit grayscale PNG, or {@code null}
     */
    public void setRawDepthData(String data) {
        if (data == null || data.isBlank() || image == null) {
            return;
        }

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
            if (img == null || img.getWidth() != image.getWidth() || img.getHeight() != image.getHeight()) {
                return; // stale cache for a different image
            }

            int w = img.getWidth();
            int h = img.getHeight();
            var rd = img.getRaster();
            float[][] raw = new float[h][w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    raw[y][x] = rd.getSample(x, y, 0) / 65535f;
                }
            }
            this.rawDepth = raw;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read cached depth data", e);
        }
    }

    private BufferedImage getOrCreateProcessedInkMask() {
        if (processedInkMask != null) {
            return processedInkMask;
        }

        BufferedImage gray = getOrCreateProcessedGray();
        int w = gray.getWidth();
        int h = gray.getHeight();
        byte[] grayData = ((java.awt.image.DataBufferByte) gray.getRaster().getDataBuffer()).getData();

        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] maskData = ((java.awt.image.DataBufferInt) mask.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < grayData.length; i++) {
            int v = grayData[i] & 0xFF;
            maskData[i] = (255 - v) << 24; // white → transparent, black → opaque
        }

        processedInkMask = mask;
        return processedInkMask;
    }


    private BufferedImage getOrCreateProcessedGray() {
        if (processedGray != null) {
            return processedGray;
        }

        BufferedImage src = getSourceImage();
        int w = src.getWidth();
        int h = src.getHeight();

        // Pre-compute a master LUT: luminance (0–255) → final gray value.
        // The entire pipeline (invert, levels, power curve) depends only on the
        // scalar luminance, so it collapses into a single table.
        int[] masterLut = buildMasterLut();

        int[] srcPixels = src.getRGB(0, 0, w, h, null, 0, w);
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] dstData = ((java.awt.image.DataBufferByte) dst.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < srcPixels.length; i++) {
            int argb = srcPixels[i];
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            int lum = (r * 54 + g * 183 + b * 19) >> 8;
            dstData[i] = (byte) masterLut[lum];
        }

        processedGray = dst;
        return processedGray;
    }

    private int[] buildMasterLut() {
        final int[] curveLut = getOrCreatePowerCurveLut();
        int[] masterLut = new int[256];

        for (int i = 0; i < 256; i++) {
            double l = i / 255.0;

            if (invert) {
                l = 1.0 - l;
            }

            int curveInput = (int) Math.round(clamp(l, 0.0, 1.0) * 255.0);
            double ql = curveLut[Math.max(0, Math.min(255, curveInput))] / 255.0;

            if (levels < 255) {
                double steps = levels - 1;
                ql = Math.round(ql * steps) / steps;
            }

            masterLut[i] = (int) Math.round(ql * 255.0);
        }

        return masterLut;
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

    private static int[][] asIntArray2D(Object value) {
        if (value instanceof int[][] arr) return arr;
        return MonotoneCubicSpline.defaultControlPoints();
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
        return List.of(CutType.NONE, CutType.LASER_RASTER, CutType.HEIGHT_MAP);
    }
}
