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

import org.kabeja.dxf.helpers.Point;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFVPort {
    private String name = "";
    private Point lowerLeftCorner = new Point();
    private Point upperRightCorner = new Point();
    private Point centerPoint = new Point();
    private Point snapBasePoint = new Point();
    private Point gridSpacingPoint = new Point();
    private Point viewDirectionPoint = new Point();
    private Point viewTargetPoint = new Point();
    private double height;
    private double width;
    private double ratio;
    private double lensLength;
    private double frontClippingPlane;
    private double backClippingPlane;
    private double rotationAngle;
    private double viewTwistAngle;
    private double circleZoom;
    private double fastZoom;
    private boolean snap;
    private boolean grid;
    private boolean active = false;

    /**
     * @return Returns the backClippingPlane.
     */
    public double getBackClippingPlane() {
        return backClippingPlane;
    }

    /**
     * @param backClippingPlane The backClippingPlane to set.
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
     * @param centerPoint The centerPoint to set.
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
     * @param circleZoom The circleZoom to set.
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
     * @param fastZoom The fastZoom to set.
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
     * @param frontClippingPlane The frontClippingPlane to set.
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
     * @param grid The grid to set.
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
     * @param gridSpacingPoint The gridSpacingPoint to set.
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
     * @param height The height to set.
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
     * @param lensLength The lensLength to set.
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
     * @param lowerLeftCorner The lowerLeftCorner to set.
     */
    public void setLowerLeftCorner(Point lowerLeftCorner) {
        this.lowerLeftCorner = lowerLeftCorner;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the ratio.
     */
    public double getAspectRatio() {
        return ratio;
    }

    /**
     * @param ratio The ratio to set.
     */
    public void setAspectRatio(double ratio) {
        this.ratio = ratio;
    }

    /**
     * @return Returns the rotationAngle.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * @param rotationAngle The rotationAngle to set.
     */
    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    /**
     * @return Returns the snap.
     */
    public boolean isSnap() {
        return snap;
    }

    /**
     * @param snap The snap to set.
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
     * @param snapBasePoint The snapBasePoint to set.
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
     * @param upperRightCorner The upperRightCorner to set.
     */
    public void setUpperRightCorner(Point upperRightCorner) {
        this.upperRightCorner = upperRightCorner;
    }

    /**
     * @return Returns the viewDirectionPoint.
     */
    public Point getViewDirectionPoint() {
        return viewDirectionPoint;
    }

    /**
     * @param viewDirectionPoint The viewDirectionPoint to set.
     */
    public void setViewDirectionPoint(Point viewDirectionPoint) {
        this.viewDirectionPoint = viewDirectionPoint;
    }

    /**
     * @return Returns the viewTargetPoint.
     */
    public Point getViewTargetPoint() {
        return viewTargetPoint;
    }

    /**
     * @param viewTargetPoint The viewTargetPoint to set.
     */
    public void setViewTargetPoint(Point viewTargetPoint) {
        this.viewTargetPoint = viewTargetPoint;
    }

    /**
     * @return Returns the viewTwistAngle.
     */
    public double getViewTwistAngle() {
        return viewTwistAngle;
    }

    /**
     * @param viewTwistAngle The viewTwistAngle to set.
     */
    public void setViewTwistAngle(double viewTwistAngle) {
        this.viewTwistAngle = viewTwistAngle;
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
