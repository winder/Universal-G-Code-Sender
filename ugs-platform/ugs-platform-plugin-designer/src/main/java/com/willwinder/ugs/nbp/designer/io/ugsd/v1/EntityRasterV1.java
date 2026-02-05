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
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.google.gson.annotations.Expose;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Raster;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

public class EntityRasterV1 extends CuttableEntityV1 {
    @Expose
    private String image;

    @Expose
    private double brightness;

    @Expose
    private double contrast;

    @Expose
    private double gamma;

    @Expose
    private int levels;

    public EntityRasterV1() {
        super(EntityTypeV1.RASTER);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    public double getBrightness() {
        return brightness;
    }

    public void setContrast(double contrast) {
        this.contrast = contrast;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getGamma() {
        return gamma;
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    @Override
    public Entity toInternal() {
        try {
            Raster raster = new Raster(ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(image))));
            applyCommonAttributes(raster);
            raster.setContrast(this.contrast);
            raster.setBrightness(this.brightness);
            raster.setGamma(this.gamma);
            raster.setLevels(this.levels);
            return raster;
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
