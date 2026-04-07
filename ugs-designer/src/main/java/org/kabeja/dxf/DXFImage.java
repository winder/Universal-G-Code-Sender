/*
 * Created on 28.06.2005
 *
 */
package org.kabeja.dxf;

import java.util.ArrayList;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.objects.DXFImageDefObject;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFImage extends DXFEntity {
    protected Point insertPoint = new Point();
    protected Point vectorV = new Point();
    protected Point vectorU = new Point();
    protected double imageSizeAlongU;
    protected double imageSizeAlongV;
    protected String imageDefID = "";
    protected double brightness;
    protected double contrast;
    protected double fade;
    protected ArrayList clipBoundary = new ArrayList();
    protected boolean clipping = false;
    protected boolean rectangularClipping = false;
    protected boolean polygonalClipping = false;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.dxf.DXFEntity#getBounds()
     */
    public Bounds getBounds() {
        Bounds b = new Bounds();
        DXFImageDefObject imageDef = (DXFImageDefObject) this.doc.getDXFObjectByID(this.getImageDefObjectID());

        if (imageDef != null) {
            b.addToBounds(this.insertPoint);
            b.addToBounds(insertPoint.getX() + imageSizeAlongU,
                insertPoint.getY() + imageSizeAlongV, this.insertPoint.getZ());
        } else {
            b.setValid(false);
        }

        return b;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.dxf.DXFEntity#getType()
     */
    public String getType() {
        return DXFConstants.ENTITY_TYPE_IMAGE;
    }

    public Point getInsertPoint() {
        return insertPoint;
    }

    public void setInsertPoint(Point p) {
        this.insertPoint = p;
    }

    public void setImageDefObjectID(String id) {
        this.imageDefID = id;
    }

    public String getImageDefObjectID() {
        return this.imageDefID;
    }

    /**
     * @return Returns the imageSizeAlongU.
     */
    public double getImageSizeAlongU() {
        return imageSizeAlongU;
    }

    /**
     * @param imageSizeAlongU
     *            The imageSizeAlongU to set.
     */
    public void setImageSizeAlongU(double imageSizeAlongU) {
        this.imageSizeAlongU = imageSizeAlongU;
    }

    /**
     * @return Returns the imageSizeAlongV.
     */
    public double getImageSizeAlongV() {
        return imageSizeAlongV;
    }

    /**
     * @param imageSizeAlongV
     *            The imageSizeAlongV to set.
     */
    public void setImageSizeAlongV(double imageSizeAlongV) {
        this.imageSizeAlongV = imageSizeAlongV;
    }

    /**
     * @return Returns the vectorU.
     */
    public Point getVectorU() {
        return vectorU;
    }

    /**
     * @param vectorU
     *            The vectorU to set.
     */
    public void setVectorU(Point vectorU) {
        this.vectorU = vectorU;
    }

    /**
     * @return Returns the vectorV.
     */
    public Point getVectorV() {
        return vectorV;
    }

    /**
     * @param vectorV
     *            The vectorV to set.
     */
    public void setVectorV(Point vectorV) {
        this.vectorV = vectorV;
    }

    /**
     * @return Returns the brightness.
     */
    public double getBrightness() {
        return brightness;
    }

    /**
     * @param brightness
     *            The brightness to set.
     */
    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    /**
     * @return Returns the clipping.
     */
    public boolean isClipping() {
        return clipping;
    }

    /**
     * @param clipping
     *            The clipping to set.
     */
    public void setClipping(boolean clipping) {
        this.clipping = clipping;
    }

    /**
     * @return Returns the contrast.
     */
    public double getContrast() {
        return contrast;
    }

    /**
     * @param contrast
     *            The contrast to set.
     */
    public void setContrast(double contrast) {
        this.contrast = contrast;
    }

    /**
     * @return Returns the fade.
     */
    public double getFade() {
        return fade;
    }

    /**
     * @param fade
     *            The fade to set.
     */
    public void setFade(double fade) {
        this.fade = fade;
    }

    /**
     * @return Returns the clipBoundary.
     */
    public ArrayList getClipBoundary() {
        return clipBoundary;
    }

    public void addClippingPoint(Point p) {
        clipBoundary.add(p);
    }

    /**
     * @return Returns the polygonalClipping.
     */
    public boolean isPolygonalClipping() {
        return polygonalClipping;
    }

    /**
     * @param polygonalClipping
     *            The polygonalClipping to set.
     */
    public void setPolygonalClipping(boolean polygonalClipping) {
        this.polygonalClipping = polygonalClipping;
        this.rectangularClipping = !polygonalClipping;
    }

    /**
     * @return Returns the rectangularClipping.
     */
    public boolean isRectangularClipping() {
        return rectangularClipping;
    }

    /**
     * @param rectangularClipping
     *            The rectangularClipping to set.
     */
    public void setRectangularClipping(boolean rectangularClipping) {
        this.rectangularClipping = rectangularClipping;
        this.polygonalClipping = !rectangularClipping;
    }

    public double getLength() {
        return 0;
    }
}
