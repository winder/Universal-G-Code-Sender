package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.apache.commons.lang3.StringUtils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Text extends AbstractCuttable {
    private String text;
    private String fontFamily;
    private Shape shape;

    private AffineTransform transform = AffineTransform.getScaleInstance(1, -1);

    public Text(double x, double y) {
        text = "";
        fontFamily = Font.SANS_SERIF;
        regenerateShape();
        setPosition(new Point2D.Double(x, y));
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

    @Override
    public void setSize(Size size) {
        if (size.getWidth() < 2) {
            size = new Size(2, size.getHeight());
        }

        if (size.getHeight() < 2) {
            size = new Size(size.getWidth(), 2);
        }
        shape. //setFrame(0, 0, size.getWidth(), size.getHeight());
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
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
}
