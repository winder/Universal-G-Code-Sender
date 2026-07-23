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
package com.willwinder.ugs.designer.io.ugsd.v1;

import com.google.gson.annotations.Expose;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Raster;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

public class EntityRasterV1 extends CuttableEntityV1 {
    @Expose
    private String image;

    @Expose
    private int levels;

    @Expose
    private boolean invert;

    @Expose
    private int[][] powerCurveControlPoints;

    @Expose
    private boolean roughing = true;

    @Expose
    private double stockToLeave;

    @Expose
    private boolean depthMapping;

    @Expose
    private double depthDetail = 0.6;

    @Expose
    private double depthSmoothing = 0.3;

    @Expose
    private double depthContrast = 0.1;

    @Expose
    private double depthEmphasis = 0.33;

    @Expose
    private String rawDepthData;

    public EntityRasterV1() {
        super(EntityTypeV1.RASTER);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public int[][] getPowerCurveControlPoints() {
        return powerCurveControlPoints;
    }

    public void setPowerCurveControlPoints(int[][] powerCurveControlPoints) {
        this.powerCurveControlPoints = powerCurveControlPoints;
    }

    public boolean isRoughing() {
        return roughing;
    }

    public void setRoughing(boolean roughing) {
        this.roughing = roughing;
    }

    public double getStockToLeave() {
        return stockToLeave;
    }

    public void setStockToLeave(double stockToLeave) {
        this.stockToLeave = stockToLeave;
    }

    public boolean isDepthMapping() {
        return depthMapping;
    }

    public void setDepthMapping(boolean depthMapping) {
        this.depthMapping = depthMapping;
    }

    public double getDepthDetail() {
        return depthDetail;
    }

    public void setDepthDetail(double depthDetail) {
        this.depthDetail = depthDetail;
    }

    public double getDepthSmoothing() {
        return depthSmoothing;
    }

    public void setDepthSmoothing(double depthSmoothing) {
        this.depthSmoothing = depthSmoothing;
    }

    public double getDepthContrast() {
        return depthContrast;
    }

    public void setDepthContrast(double depthContrast) {
        this.depthContrast = depthContrast;
    }

    public double getDepthEmphasis() {
        return depthEmphasis;
    }

    public void setDepthEmphasis(double depthEmphasis) {
        this.depthEmphasis = depthEmphasis;
    }

    public String getRawDepthData() {
        return rawDepthData;
    }

    public void setRawDepthData(String rawDepthData) {
        this.rawDepthData = rawDepthData;
    }

    @Override
    public Entity toInternal() {
        try {
            Raster raster = new Raster(ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(image))));
            applyCommonAttributes(raster);
            raster.setLevels(this.levels);
            raster.setInvert(this.invert);
            raster.setRoughing(this.roughing);
            raster.setStockToLeave(this.stockToLeave);

            // Restore the tuning and the cached raw depth before enabling depth mapping, so the first
            // generation reuses the cached estimate instead of re-running the model.
            raster.setDepthDetail(this.depthDetail);
            raster.setDepthSmoothing(this.depthSmoothing);
            raster.setDepthContrast(this.depthContrast);
            raster.setDepthEmphasis(this.depthEmphasis);
            raster.setRawDepthData(this.rawDepthData);
            raster.setDepthMapping(this.depthMapping);

            if (this.powerCurveControlPoints != null) {
                raster.setPowerCurveControlPoints(this.powerCurveControlPoints);
            }
            return raster;
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
