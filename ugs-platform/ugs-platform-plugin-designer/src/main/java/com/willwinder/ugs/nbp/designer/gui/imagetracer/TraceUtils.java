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

import jankovicsandras.imagetracer.ImageTracer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

/**
 * @author Joacim Breiler
 */
public class TraceUtils {

    public static String traceImage(File selectedFile, TraceSettings settingsPanel) {
        // Options
        HashMap<String, Float> options = new HashMap<>();

        // Tracing
        options.put("ltres", settingsPanel.getLineThreshold());
        options.put("qtres", settingsPanel.getQuadThreshold());
        options.put("pathomit", Integer.valueOf(settingsPanel.getPathOmit()).floatValue());

        // Color quantization
        options.put("colorsampling", 0f); // 1f means true ; 0f means false: starting with generated palette
        int numberOfColors = settingsPanel.getNumberOfColors();
        options.put("numberofcolors", Integer.valueOf(numberOfColors).floatValue());
        options.put("mincolorratio", 0f);
        options.put("colorquantcycles", Integer.valueOf(settingsPanel.getColorQuantize()).floatValue());

        // SVG rendering
        options.put("scale", 1f);
        options.put("roundcoords", 0f); // 1f means rounded to 1 decimal places, like 7.3 ; 3f means rounded to 3 places, like 7.356 ; etc.
        options.put("lcpr", 0f);
        options.put("qcpr", 0f);
        options.put("desc", 1f); // 1f means true ; 0f means false: SVG descriptions deactivated
        options.put("viewbox", 0f); // 1f means true ; 0f means false: fixed width and height

        // Selective Gauss Blur
        options.put("blurradius", Integer.valueOf(settingsPanel.getBlurRadius()).floatValue()); // 0f means deactivated; 1f .. 5f : blur with this radius
        options.put("blurdelta", Integer.valueOf(settingsPanel.getBlurDelta()).floatValue()); // smaller than this RGB difference will be blurred


        try {
            BufferedImage img = ImageIO.read(selectedFile);
            BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = gray.createGraphics();
            g.drawImage(img, 0, 0, null);

            byte[][] palette = generatePalette(numberOfColors, settingsPanel.getStartColor(), settingsPanel.getEndColor());
            return ImageTracer.imageToSVG(gray, options, palette);
        } catch (Exception e) {
            throw new RuntimeException("Could not trace image", e);
        }
    }

    private static byte[][] generatePalette(int numberOfColors, int startColorValue, int endColorValue) {
        int step = (startColorValue - endColorValue) / numberOfColors;
        byte[][] palette = new byte[numberOfColors][4];
        for (int colorcnt = 0; colorcnt < numberOfColors; colorcnt++) {
            int value = endColorValue + (colorcnt * step);
            palette[colorcnt][0] = (byte) (-128 + value); // R
            palette[colorcnt][1] = (byte) (-128 + value); // G
            palette[colorcnt][2] = (byte) (-128 + value); // B
            palette[colorcnt][3] = (byte) 127;              // A
        }
        return palette;
    }
}
