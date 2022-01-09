package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import org.apache.commons.lang3.StringUtils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Text extends AbstractCuttable {
    private String text;
    private String fontFamily;
    private Shape shape;

    private AffineTransform transform = AffineTransform.getScaleInstance(1, -1);

    public Text(double x, double y) {
        super(x, y);
        setName("Text");
        text = "";
        fontFamily = Font.SANS_SERIF;
        regenerateShape();
    }

    public Text() {
        this(0, 0);
    }

    private void regenerateShape() {
        if (StringUtils.isNotEmpty(text)) {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            Font font = new Font(this.fontFamily, Font.PLAIN, 18);
            GlyphVector glyphVector = font.createGlyphVector(g2.getFontRenderContext(), text);
            shape = transform.createTransformedShape(glyphVector.getOutline(0, 0));
        } else {
            // Create a temporary shape
            shape = transform.createTransformedShape(new Rectangle2D.Double(0, 0, 2, 12));
        }
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        regenerateShape();
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        regenerateShape();
    }

    @Override
    public Entity copy() {
        Text copy = new Text();
        copy.setText(getText());
        copy.setFontFamily(getFontFamily());
        copy.setStartDepth(getStartDepth());
        copy.setTargetDepth(getTargetDepth());
        copy.setCutType(getCutType());
        copy.setTransform(new AffineTransform(getTransform()));
        return copy;
    }
}
