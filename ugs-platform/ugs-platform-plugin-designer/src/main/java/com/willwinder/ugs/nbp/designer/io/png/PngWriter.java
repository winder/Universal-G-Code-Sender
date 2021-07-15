/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.png;

import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Joacim Breiler
 */
public class PngWriter implements DesignWriter {
    @Override
    public void write(File file, Controller controller) {
        try {
            write(new FileOutputStream(file), controller);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't write to file", e);
        }
    }

    @Override
    public void write(OutputStream outputStream, Controller controller) {
        try {
            controller.getSelectionManager().clearSelection();
            BufferedImage bi = controller.getDrawing().getImage();
            ImageIO.write(bi, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write to file", e);
        }
    }
}
