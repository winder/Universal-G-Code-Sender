package com.willwinder.ugs.nbp.designer.io;

import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PngWriter implements Writer {
    @Override
    public void write(File file, Controller controller) {
        try {
            controller.getSelectionManager().removeAll();
            BufferedImage bi = controller.getDrawing().getImage();
            ImageIO.write(bi, "png", file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write to file", e);
        }
    }
}
