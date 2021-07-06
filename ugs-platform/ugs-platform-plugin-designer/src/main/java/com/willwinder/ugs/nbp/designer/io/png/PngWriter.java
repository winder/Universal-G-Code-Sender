package com.willwinder.ugs.nbp.designer.io.png;

import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

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
