/*
 * Created on Jan 4, 2005
 *
 *
 */
package org.kabeja.dxf;

import org.kabeja.dxf.helpers.Point;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth </a>
 *
 *
 *
 */
public class DXFDimension extends DXFEntity {
    protected final int TYPE_LINEAR = 0;
    protected final int TYPE_ALIGNMENT = 1;
    protected final int TYPE_4POINT = 2;
    protected final int TYPE_DIAMETER = 3;
    protected final int TYPE_RADIAL = 4;
    protected final int TYPE_3POINT_ANGLE = 5;
    protected final int TYPE_COORDINATES = 6;
    protected int dimType;
    protected Point referencePoint = new Point();
    protected Point textPoint = new Point();
    protected Point insertPoint = new Point();
    protected Point referencePoint3 = new Point();
    protected Point referencePoint4 = new Point();
    protected Point referencePoint5 = new Point();
    protected Point referencePoint6 = new Point();
    protected int attechmentLocation;
    protected boolean exactTextLineSpacing = false;
    protected double rotate = 0;
    protected double horizontalDirection = 0;
    protected String dimensionStyle = "";
    protected String dimensionText = "";
    protected String dimensionBlock = "";
    protected int dimensionArea = 0;
    protected double textRotation = 0.0;
    protected double dimensionRotation = 0.0;
    protected double inclinationHelpLine = 0.0;
    protected double leadingLineLength = 0.0;
    protected double horizontalAlign = 0.0;

    public DXFDimension() {
    }

    /**
     * @return Returns the attechmentLocation.
     */
    public int getAttechmentLocation() {
        return attechmentLocation;
    }

    /**
     * @param attechmentLocation
     *            The attechmentLocation to set.
     */
    public void setAttechmentLocation(int attechmentLocation) {
        this.attechmentLocation = attechmentLocation;
    }

    /**
     * @return Returns the dimensionStyle.
     */
    public String getDimensionStyleID() {
        return dimensionStyle;
    }

    /**
     * @param dimensionStyle
     *            The dimensionStyle to set.
     */
    public void setDimensionStyleID(String dimensionStyle) {
        this.dimensionStyle = dimensionStyle;
    }

    /**
     * @return Returns the exactTextLineSpacing.
     */
    public boolean isExactTextLineSpacing() {
        return exactTextLineSpacing;
    }

    /**
     * @param exactTextLineSpacing
     *            The exactTextLineSpacing to set.
     */
    public void setExactTextLineSpacing(boolean exactTextLineSpacing) {
        this.exactTextLineSpacing = exactTextLineSpacing;
    }

    /**
     * @return Returns the horizontalDirection.
     */
    public double getHorizontalDirection() {
        return horizontalDirection;
    }

    /**
     * @param horizontalDirection
     *            The horizontalDirection to set.
     */
    public void setHorizontalDirection(double horizontalDirection) {
        this.horizontalDirection = horizontalDirection;
    }

    /**
     * @return Returns the insertPoint.
     */
    public Point getInsertPoint() {
        return insertPoint;
    }

    /**
     * @param insertPoint
     *            The insertPoint to set.
     */
    public void setInsertPoint(Point insertPoint) {
        this.insertPoint = insertPoint;
    }

    /**
     * @return Returns the referencePoint.
     */
    public Point getReferencePoint() {
        return referencePoint;
    }

    /**
     * @param referencePoint
     *            The referencePoint to set.
     */
    public void setReferencePoint(Point referencePoint) {
        this.referencePoint = referencePoint;
    }

    /**
     * @return Returns the referencePoint3.
     */
    public Point getReferencePoint3() {
        return referencePoint3;
    }

    /**
     * @param referencePoint3
     *            The referencePoint3 to set.
     */
    public void setReferencePoint3(Point referencePoint3) {
        this.referencePoint3 = referencePoint3;
    }

    /**
     * @return Returns the referencePoint4.
     */
    public Point getReferencePoint4() {
        return referencePoint4;
    }

    /**
     * @param referencePoint4
     *            The referencePoint4 to set.
     */
    public void setReferencePoint4(Point referencePoint4) {
        this.referencePoint4 = referencePoint4;
    }

    /**
     * @return Returns the referencePoint5.
     */
    public Point getReferencePoint5() {
        return referencePoint5;
    }

    /**
     * @param referencePoint5
     *            The referencePoint5 to set.
     */
    public void setReferencePoint5(Point referencePoint5) {
        this.referencePoint5 = referencePoint5;
    }

    /**
     * @return Returns the referencePoint6.
     */
    public Point getReferencePoint6() {
        return referencePoint6;
    }

    /**
     * @param referencePoint6
     *            The referencePoint6 to set.
     */
    public void setReferencePoint6(Point referencePoint6) {
        this.referencePoint6 = referencePoint6;
    }

    /**
     * @return Returns the rotate.
     */
    public double getRotate() {
        return rotate;
    }

    /**
     * @param rotate
     *            The rotate to set.
     */
    public void setRotate(double rotate) {
        this.rotate = rotate;
    }

    /**
     * @return Returns the textPoint.
     */
    public Point getTextPoint() {
        return textPoint;
    }

    /**
     * @param textPoint
     *            The textPoint to set.
     */
    public void setTextPoint(Point textPoint) {
        this.textPoint = textPoint;
    }

    /**
     * @return Returns the type.
     */
    public int getDimensionType() {
        return dimType;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setDimensionType(int type) {
        this.dimType = type;
    }

    public double getDimensionRotation() {
        return dimensionRotation;
    }

    public void setDimensionRotation(double dimensionRotation) {
        this.dimensionRotation = dimensionRotation;
    }

    public String getDimensionText() {
        return dimensionText;
    }

    public void setDimensionText(String dimensionText) {
        this.dimensionText = dimensionText;
    }

    public double getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(double horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    public double getInclinationHelpLine() {
        return inclinationHelpLine;
    }

    public void setInclinationHelpLine(double inclinationHelpLine) {
        this.inclinationHelpLine = inclinationHelpLine;
    }

    public double getLeadingLineLength() {
        return leadingLineLength;
    }

    public void setLeadingLineLength(double leadingLineLength) {
        this.leadingLineLength = leadingLineLength;
    }

    public double getTextRotation() {
        return textRotation;
    }

    public void setTextRotation(double textRotation) {
        this.textRotation = textRotation;
    }

    public String getDimensionBlock() {
        return dimensionBlock;
    }

    public void setDimensionBlock(String dimensionBlock) {
        this.dimensionBlock = dimensionBlock;
    }

    public int getDimensionArea() {
        return dimensionArea;
    }

    public void setDimensionArea(int dimensionArea) {
        this.dimensionArea = dimensionArea;
    }

    public Bounds getBounds() {
        // TODO add real bounds
        Bounds bounds = new Bounds();

        if (this.doc.getDXFBlock(this.dimensionBlock) != null) {
            DXFBlock block = doc.getDXFBlock(this.getDimensionBlock());
            Bounds b = block.getBounds();
            Point refPoint = block.getReferencePoint();

            if (b.isValid()) {
                // Translate to origin
                bounds.setMaximumX((b.getMaximumX() - refPoint.getX()));
                bounds.setMinimumX((b.getMinimumX() - refPoint.getX()));
                bounds.setMaximumY((b.getMaximumY() - refPoint.getY()));
                bounds.setMinimumY((b.getMinimumY() - refPoint.getY()));

                // translate to the InsertPoint
                bounds.setMaximumX(bounds.getMaximumX() +
                    this.insertPoint.getX());
                bounds.setMinimumX(bounds.getMinimumX() +
                    this.insertPoint.getX());
                bounds.setMaximumY(bounds.getMaximumY() +
                    this.insertPoint.getY());
                bounds.setMinimumY(bounds.getMinimumY() +
                    this.insertPoint.getY());
                ;
            }
        } else {
            bounds.setValid(false);
        }

        return bounds;
    }

    public DXFDimensionStyle getDXFDimensionStyle() {
        return doc.getDXFDimensionStyle(getDimensionStyleID());
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_DIMENSION;
    }

    public double getLength() {
        return 0;
    }
}
