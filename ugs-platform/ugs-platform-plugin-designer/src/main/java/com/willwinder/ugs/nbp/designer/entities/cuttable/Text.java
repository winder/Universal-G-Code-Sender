package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Text extends AbstractCuttable {
    private String text;
    private String fontFamily;
    private Shape shape;

    private AffineTransform transform = AffineTransform.getScaleInstance(1, -1);

    public Text(double x, double y) {
        text = "BlÄpp";
        fontFamily = Font.SANS_SERIF;
        regenerateShape();
        setPosition(new Point2D.Double(x, y));
    }

    private void regenerateShape() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Font font = new Font(fontFamily, Font.PLAIN, 18);
        GlyphVector vect = font.createGlyphVector(g2.getFontRenderContext(), text);
        this.shape = transform.createTransformedShape(vect.getOutline(0f, (float) -(vect.getVisualBounds().getY())));
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public void setSize(Size size) {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        regenerateShape();
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        regenerateShape();
    }

    public String getFontFamily() {
        return fontFamily;
    }
}
