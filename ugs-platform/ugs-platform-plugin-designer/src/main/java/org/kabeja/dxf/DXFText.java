/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.kabeja.dxf;

import org.kabeja.dxf.helpers.DXFTextParser;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.TextDocument;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFText extends DXFEntity {
    public static final double DEFAULT_FONT_SIZE = 8;
    public static final int VALIGN_BASELINE = 0;
    public static final int VALIGN_BOTTOM = 1;
    public static final int VALIGN_CENTER = 2;
    public static final int VALIGN_TOP = 3;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_ALIGNED = 3;
    public static final int ALIGN_MIDDLE = 4;
    public static final int ALIGN_FIT = 5;
    protected double rotation = 0.0;
    protected double height = 0.0;
    protected double scale_x = 1.0;
    protected double oblique_angle = 0.0;
    protected double align_x = 0.0;
    protected double align_y = 0.0;
    protected double align_z = 0.0;

    // the horizontal align
    protected int align = 0;

    // the vertical align
    protected int valign = 0;
    protected String text = "";
    protected String textStyle = "";
    protected Point p;
    protected Point align_p1;
    protected Point align_p2;
    protected boolean upsideDown = false;
    protected boolean backward = false;
    protected boolean alignmentPointSet = false;
    protected boolean top = false;
    protected boolean bottom = false;
    protected boolean vertical_center = false;
    protected TextDocument textDoc = new TextDocument();

    /**
     *
     */
    public DXFText() {
        this.p = new Point();
        this.align_p1 = new Point();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.dxf.DXFEntity#setDXFDocument(org.dxf2svg.dxf.DXFDocument)
     */
    public void setDXFDocument(DXFDocument doc) {
        super.setDXFDocument(doc);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.dxf.DXFEntity#updateViewPort()
     */
    public Bounds getBounds() {
        Bounds bounds = new Bounds();

        double tl = getTextDocument().getText().length();

        if (tl > 0) {
            // TODO calculate the real values
            Point p = calculateAlignmentPoint();
            bounds.addToBounds(p);

            double h = getHeight();

            double w = tl * h * 0.6;

            if (this.isBackward()) {
                w = -1 * w;
            }

            // we set the horizontal width
            switch (this.align) {
            case 0:
                bounds.addToBounds(p.getX() + w, p.getY(), p.getZ());

                break;

            case 1:
                bounds.addToBounds(p.getX() + (w / 2), p.getY(), p.getZ());
                bounds.addToBounds(p.getX() - (w / 2), p.getY(), p.getZ());

                break;

            case 2:
                bounds.addToBounds(p.getX() - w, p.getY(), p.getZ());

                break;

            case 3:
                bounds.addToBounds(p.getX() - w, p.getY(), p.getZ());

                break;

            case 4:
                bounds.addToBounds(p.getX() + (w / 2), p.getY(), p.getZ());
                bounds.addToBounds(p.getX() - (w / 2), p.getY(), p.getZ());

                break;

            case 5:
                bounds.addToBounds(p.getX() + (w / 2), p.getY(), p.getZ());
                bounds.addToBounds(p.getX() - (w / 2), p.getY(), p.getZ());

                break;
            }

            // set the vertical height
            switch (this.valign) {
            case VALIGN_BASELINE:
                bounds.addToBounds(p.getX(), p.getY() + (h * 0.75), p.getZ());

                break;

            case VALIGN_BOTTOM:
                bounds.addToBounds(p.getX(), p.getY() + h, p.getZ());

                break;

            case VALIGN_CENTER:
                bounds.addToBounds(p.getX(), p.getY() + (h * 0.5), p.getZ());
                bounds.addToBounds(p.getX(), p.getY() - (h * 0.5), p.getZ());

                break;

            case VALIGN_TOP:
                bounds.addToBounds(p.getX(), p.getY() - h, p.getZ());

                break;
            }
        } else {
            bounds.setValid(false);
        }

        return bounds;
    }

    /**
     * @return Returns the align.
     */
    public int getAlign() {
        return align;
    }

    /**
     * @param align
     *            The align to set.
     */
    public void setAlign(int align) {
        this.align = align;
    }

    /**
     * @return Returns the align_x.
     */
    public double getAlignX() {
        return align_p1.getX();
    }

    /**
     * @param align_x
     *            The align_x to set.
     */
    public void setAlignX(double align_x) {
        align_p1.setX(align_x);
    }

    /**
     * @return Returns the align_y.
     */
    public double getAlignY() {
        return align_p1.getY();
    }

    /**
     * @param align_y
     *            The align_y to set.
     */
    public void setAlignY(double align_y) {
        align_p1.setY(align_y);
    }

    /**
     * @return Returns the align_z.
     */
    public double getAlignZ() {
        return align_p1.getZ();
    }

    /**
     * @param align_z
     *            The align_z to set.
     */
    public void setAlignZ(double align_z) {
        align_p1.setZ(align_z);
    }

    /**
     * @return Returns the height.
     */
    public double getHeight() {
        if (height != 0.0) {
            return height;
        } else if (doc.getDXFStyle(this.textStyle) != null) {
            return doc.getDXFStyle(this.textStyle).getTextHeight();
        } else {
            return 0.0;
        }
    }

    /**
     * @param height
     *            The height to set.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @return Returns the oblique_angle.
     */
    public double getObliqueAngle() {
        return oblique_angle;
    }

    /**
     * @param oblique_angle
     *            The oblique_angle to set.
     */
    public void setObliqueAngle(double oblique_angle) {
        this.oblique_angle = oblique_angle;
    }

    /**
     * @return Returns the rotation.
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * @param rotation
     *            The rotation to set.
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     * @return Returns the scale_x.
     */
    public double getScaleX() {
        return scale_x;
    }

    /**
     * @param scale_x
     *            The scale_x to set.
     */
    public void setScaleX(double scale_x) {
        this.scale_x = scale_x;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            The text to set.
     */
    public void setText(String text) {
        this.text = text;
        this.textDoc = DXFTextParser.parseDXFText(this);
    }

    /**
     * @return Returns the textStyle.
     */
    public String getTextStyle() {
        return textStyle;
    }

    /**
     * @param textStyle
     *            The textStyle to set.
     */
    public void setTextStyle(String textStyle) {
        this.textStyle = textStyle;
    }

    /**
     * @return Returns the valign.
     */
    public int getValign() {
        return valign;
    }

    /**
     * @param valign
     *            The valign to set.
     */
    public void setValign(int valign) {
        this.valign = valign;
    }

    public void setX(double x) {
        p.setX(x);
    }

    public void setY(double y) {
        p.setY(y);
    }

    public void setZ(double z) {
        p.setZ(z);
    }

    public boolean isBackward() {
        return backward;
    }

    public void setBackward(boolean backward) {
        this.backward = backward;
    }

    public boolean isUpsideDown() {
        return upsideDown;
    }

    public void setUpsideDown(boolean upsideDown) {
        this.upsideDown = upsideDown;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_TEXT;
    }

    /**
     * @return Returns the alignmentPointSet.
     */
    public boolean isAlignmentPointSet() {
        return alignmentPointSet;
    }

    /**
     * @param alignmentPointSet
     *            The alignmentPointSet to set.
     */
    public void setAlignmentPoint(boolean alignmentPoint) {
        this.alignmentPointSet = alignmentPoint;
    }

    public TextDocument getTextDocument() {
        return this.textDoc;
    }

    public Point getInsertPoint() {
        return p;
    }

    public Point getAlignmentPoint() {
        return align_p1;
    }

    public Point calculateAlignmentPoint() {
        Point alignmentPoint = new Point(p.getX(), p.getY(), p.getZ());

        if (!isUpsideDown()) {
            switch (align) {
            case 1:

                if (alignmentPointSet) {
                    alignmentPoint.setX(align_p1.getX());
                }

                break;

            case 2:

                if (alignmentPointSet) {
                    alignmentPoint.setX(align_p1.getX());
                }

                break;

            case 3:

                if (alignmentPointSet) {
                    alignmentPoint.setX(align_p1.getX());
                }

                break;

            case 4:

                if (alignmentPointSet) {
                    alignmentPoint.setX(align_p1.getX());
                }

                break;

            case 5:

                if (alignmentPointSet) {
                    alignmentPoint.setX(align_p1.getX());
                }

                break;
            }

            if (alignmentPointSet) {
                alignmentPoint.setY(align_p1.getY());
            }
        }

        return alignmentPoint;
    }

    public boolean isOmitLineType() {
        return true;
    }

    public double getLength() {
        return 0;
    }
}
