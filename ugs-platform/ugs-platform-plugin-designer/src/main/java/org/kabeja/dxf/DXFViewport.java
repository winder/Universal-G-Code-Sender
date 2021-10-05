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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;
import org.kabeja.math.MathUtils;
import org.kabeja.math.ParametricPlane;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFViewport extends DXFEntity {
    private String viewportID = "";
    private String plotStyleName = "";
    private Point lowerLeftCorner = new Point();
    private Point upperRightCorner = new Point();
    private Point centerPoint = new Point();
    private Point snapBasePoint = new Point();
    private Point snapSpacingPoint = new Point();
    private Point gridSpacingPoint = new Point();
    private Vector viewDirectionVector = new Vector();
    private Point viewCenterPoint = new Point();
    private Point viewTargetPoint = new Point();
    private Vector ucsOrigin = new Vector();
    private Vector ucsXAxis = new Vector();
    private Vector ucsYAxis = new Vector();
    private int ucsType = 0;
    private int viewportStatus = 0;
    private double ucsElevation = 0.0;
    private boolean useUCS = false;
    private double height;
    private double width;
    private double ratio;
    private double lensLength;
    private double viewHeight;
    private double frontClippingPlane;
    private double backClippingPlane;
    private double twistAngle;
    private double snapAngle;
    private double circleZoom;
    private double fastZoom;
    private boolean snap;
    private boolean grid;
    private boolean active = false;
    private int renderMode;
    private Set frozenLayerSet = new HashSet();

    /**
     * @return Returns the backClippingPlane.
     */
    public double getBackClippingPlane() {
        return backClippingPlane;
    }

    /**
     * @param backClippingPlane
     *            The backClippingPlane to set.
     */
    public void setBackClippingPlane(double backClippingPlane) {
        this.backClippingPlane = backClippingPlane;
    }

    /**
     * @return Returns the centerPoint.
     */
    public Point getCenterPoint() {
        return centerPoint;
    }

    /**
     * @param centerPoint
     *            The centerPoint to set.
     */
    public void setCenterPoint(Point centerPoint) {
        this.centerPoint = centerPoint;
    }

    /**
     * @return Returns the circleZoom.
     */
    public double getCircleZoom() {
        return circleZoom;
    }

    /**
     * @param circleZoom
     *            The circleZoom to set.
     */
    public void setCircleZoom(double circleZoom) {
        this.circleZoom = circleZoom;
    }

    /**
     * @return Returns the fastZoom.
     */
    public double getFastZoom() {
        return fastZoom;
    }

    /**
     * @param fastZoom
     *            The fastZoom to set.
     */
    public void setFastZoom(double fastZoom) {
        this.fastZoom = fastZoom;
    }

    /**
     * @return Returns the frontClippingPlane.
     */
    public double getFrontClippingPlane() {
        return frontClippingPlane;
    }

    /**
     * @param frontClippingPlane
     *            The frontClippingPlane to set.
     */
    public void setFrontClippingPlane(double frontClippingPlane) {
        this.frontClippingPlane = frontClippingPlane;
    }

    /**
     * @return Returns the grid.
     */
    public boolean isGrid() {
        return grid;
    }

    /**
     * @param grid
     *            The grid to set.
     */
    public void setGrid(boolean grid) {
        this.grid = grid;
    }

    /**
     * @return Returns the gridSpacingPoint.
     */
    public Point getGridSpacingPoint() {
        return gridSpacingPoint;
    }

    /**
     * @param gridSpacingPoint
     *            The gridSpacingPoint to set.
     */
    public void setGridSpacingPoint(Point gridSpacingPoint) {
        this.gridSpacingPoint = gridSpacingPoint;
    }

    /**
     * @return Returns the height.
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param height
     *            The height to set.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @return Returns the lensLength.
     */
    public double getLensLength() {
        return lensLength;
    }

    /**
     * @param lensLength
     *            The lensLength to set.
     */
    public void setLensLength(double lensLength) {
        this.lensLength = lensLength;
    }

    /**
     * @return Returns the lowerLeftCorner.
     */
    public Point getLowerLeftCorner() {
        return lowerLeftCorner;
    }

    /**
     * @param lowerLeftCorner
     *            The lowerLeftCorner to set.
     */
    public void setLowerLeftCorner(Point lowerLeftCorner) {
        this.lowerLeftCorner = lowerLeftCorner;
    }

    /**
     * @return Returns the name.
     */
    public String getViewportID() {
        return viewportID;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setViewportID(String name) {
        this.viewportID = name;
    }

    /**
     * @return Returns the ratio.
     */
    public double getAspectRatio() {
        return ratio;
    }

    /**
     * @param ratio
     *            The ratio to set.
     */
    public void setAspectRatio(double ratio) {
        this.ratio = ratio;
    }

    /**
     * @param rotationAngle
     *            The rotationAngle to set.
     */
    public void setTwistAngle(double rotationAngle) {
        this.twistAngle = rotationAngle;
    }

    /**
     * @return Returns the snap.
     */
    public boolean isSnap() {
        return snap;
    }

    /**
     * @param snap
     *            The snap to set.
     */
    public void setSnap(boolean snap) {
        this.snap = snap;
    }

    /**
     * @return Returns the snapBasePoint.
     */
    public Point getSnapBasePoint() {
        return snapBasePoint;
    }

    /**
     * @param snapBasePoint
     *            The snapBasePoint to set.
     */
    public void setSnapBasePoint(Point snapBasePoint) {
        this.snapBasePoint = snapBasePoint;
    }

    /**
     * @return Returns the upperRightCorner.
     */
    public Point getUpperRightCorner() {
        return upperRightCorner;
    }

    /**
     * @param upperRightCorner
     *            The upperRightCorner to set.
     */
    public void setUpperRightCorner(Point upperRightCorner) {
        this.upperRightCorner = upperRightCorner;
    }

    /**
     * @return Returns the viewDirectionPoint.
     */
    public Vector getViewDirectionVector() {
        return viewDirectionVector;
    }

    /**
     * @param viewDirectionPoint
     *            The viewDirectionPoint to set.
     */
    public void setViewDirectionVector(Vector viewDirectionPoint) {
        this.viewDirectionVector = viewDirectionPoint;
    }

    /**
     * @return Returns the viewTargetPoint.
     */
    public Point getViewCenterPoint() {
        return viewCenterPoint;
    }

    /**
     * @param viewTargetPoint
     *            The viewTargetPoint to set.
     */
    public void setViewCenterPoint(Point viewTargetPoint) {
        this.viewCenterPoint = viewTargetPoint;
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     *            The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public Bounds getBounds() {
        Bounds bounds = new Bounds();

        if (this.viewportStatus > 0) {
            bounds.addToBounds(this.centerPoint.getX() - (this.width / 2),
                this.centerPoint.getY() - (this.height / 2), 0.0);
            bounds.addToBounds(this.centerPoint.getX() + (this.width / 2),
                this.centerPoint.getY() + (this.height / 2), 0.0);
        }

        return bounds;
    }

    public double getLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_VIEWPORT;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

    public double getViewHeight() {
        return viewHeight;
    }

    public void setViewHeight(double viewHeight) {
        this.viewHeight = viewHeight;
    }

    public Vector getUcsOrigin() {
        return ucsOrigin;
    }

    public void setUcsOrigin(Vector ucsOrigin) {
        this.ucsOrigin = ucsOrigin;
    }

    public Vector getUcsXAxis() {
        return ucsXAxis;
    }

    public void setUcsXAxis(Vector ucsXAxis) {
        this.ucsXAxis = ucsXAxis;
    }

    public Vector getUcsYAxis() {
        return ucsYAxis;
    }

    public void setUcsYAxis(Vector ucsYAxis) {
        this.ucsYAxis = ucsYAxis;
    }

    public int getUcsType() {
        return ucsType;
    }

    public void setUcsType(int ucsType) {
        this.ucsType = ucsType;
    }

    public double getUcsElevation() {
        return ucsElevation;
    }

    public void setUcsElevation(double ucsElevation) {
        this.ucsElevation = ucsElevation;
    }

    public boolean isUseUCS() {
        return useUCS;
    }

    public void setUseUCS(boolean useUCS) {
        this.useUCS = useUCS;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public double getSnapAngle() {
        return snapAngle;
    }

    public void setSnapAngle(double snapAngle) {
        this.snapAngle = snapAngle;
    }

    public Point getViewTargetPoint() {
        return viewTargetPoint;
    }

    public void setViewTargetPoint(Point viewTargetPoint) {
        this.viewTargetPoint = viewTargetPoint;
    }

    public Point getSnapSpacingPoint() {
        return snapSpacingPoint;
    }

    public void setSnapSpacingPoint(Point snapSpacingPoint) {
        this.snapSpacingPoint = snapSpacingPoint;
    }

    public String getPlotStyleName() {
        return plotStyleName;
    }

    public void setPlotStyleName(String plotStyleName) {
        this.plotStyleName = plotStyleName;
    }

    public int getViewportStatus() {
        return viewportStatus;
    }

    public void setViewportStatus(int viewportStatus) {
        this.viewportStatus = viewportStatus;
    }

    public double getTwistAngle() {
        return twistAngle;
    }

    public void addFrozenLayer(String layerName) {
        this.frozenLayerSet.add(layerName);
    }

    public boolean isFrozenLayer(String layerName) {
        return this.frozenLayerSet.contains(layerName);
    }

    public Iterator getFrozenLayerIterator() {
        return this.frozenLayerSet.iterator();
    }

    public double getZoomXPFactor() {
        if (this.viewHeight != 0.0) {
            return this.height / this.viewHeight;
        } else {
            return this.calculateZoomXPFactor();
        }
    }

    public double calculateZoomXPFactor() {
        double c = (this.getViewDirectionVector().getLength() * 42) / this.lensLength;
        double f = this.width / this.height;
        double b = Math.sqrt((Math.pow(c, 2) / (Math.pow(f, 2) + 1)));

        return this.height / b;
    }

    public Bounds getModelspaceViewBounds() {
        double f = this.width / this.height;

        // the half of width and height
        double modelH = (this.height / this.getZoomXPFactor()) / 2;
        double modelW = (f * modelH);
        double wf = modelW / modelH;
        Vector directionX = null;

        if ((this.viewDirectionVector.getX() == 0.0) &&
                (this.viewDirectionVector.getY() == 0.0) &&
                (this.viewDirectionVector.getZ() == 1.0)) {
            directionX = new Vector(1, 0, 0);
        } else {
            directionX = MathUtils.crossProduct(DXFConstants.DEFAULT_Z_AXIS_VECTOR,
                    this.viewDirectionVector);
        }

        ParametricPlane plane = new ParametricPlane(this.viewTargetPoint,
                directionX,
                MathUtils.crossProduct(this.viewDirectionVector, directionX),
                this.viewDirectionVector);
        Bounds bounds = new Bounds();
        Point p = plane.getPoint(this.viewCenterPoint.getX() - modelW,
                this.viewCenterPoint.getY() - modelH);
        bounds.addToBounds(p);
        p = plane.getPoint(this.viewCenterPoint.getX() + modelW,
                this.viewCenterPoint.getY() + modelH);
        bounds.addToBounds(p);

        return bounds;
    }
}
