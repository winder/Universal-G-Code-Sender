package com.willwinder.ugs.nbp.designer.io;


import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.Rectangle;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DrawIO {

    public void export(File f, Controller c) {
        try {
            c.getSelectionManager().removeAll();
            BufferedImage bi = c.getDrawing().getImage(); // retrieve image
            ImageIO.write(bi, "png", f);
        } catch (IOException e) {
        }
    }

    public Point getPoint(String str) {
        String[] p = str.split(",");

        return new Point(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1]
                .trim()));

    }

    public void open(File f, Controller c) {
        int lineNumber = 1;
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String str;


            c.newDrawing();

            while ((str = in.readLine()) != null) {
                try {
                    lineNumber++;
                    if (str.length() == 0) {
                        continue;
                    }

                    String[] parts = str.split(";");

                    Point p1 = getPoint(parts[1]);
                    Point p2 = getPoint(parts[2]);
                    AbstractEntity sh = null;
                    parts[0] = parts[0].trim();

                    if (parts[0].equals("rect")) {
                        boolean fill = Integer.parseInt(parts[4].trim()) != 0;
                        sh = new Rectangle(p1.x, p1.y);
                    } else if (parts[0].equals("circ")) {
                        boolean fill = Integer.parseInt(parts[4].trim()) != 0;
                        sh = new Ellipse(p1.x, p1.y);
                    } else {
                        throw new ArrayIndexOutOfBoundsException();
                    }

                    if (sh != null) {
                        sh.setSize(new Dimension(Double.valueOf(p2.getX()).intValue(), Double.valueOf(p2.getY()).intValue()));
                        c.getDrawing().insertEntity(sh);
                    }
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("Could not read line " + lineNumber
                            + " in file \"" + f + "\"");
                }

            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void save(File f, Controller c) {
        Drawing d = c.getDrawing();

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(f));

            out.write(d.getPreferredSize().width + ","
                    + d.getPreferredSize().height + "\n");

            for (Entity s : c.getDrawing().getShapes()) {
                out.write(s.toString() + "\n");
            }
            out.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not save the drawing.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
}
