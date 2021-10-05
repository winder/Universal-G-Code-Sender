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
import org.kabeja.dxf.helpers.Vector;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFView {
    private Point centerPoint = new Point();
    private double height = 0.0;
    private double width = 0.0;
    private String name = "";
    private Vector viewDirection = new Vector();
    private Point target = new Point();
    private double lensLength = 0.0;
    private double frontClipping = 0.0;
    private double backClipping = 0.0;
    private double twistAngle = 0.0;
    private int renderMode = 0;
    private Vector ucsOrigin = new Vector();
    private Vector ucsXAxis = new Vector();
    private Vector ucsYAxis = new Vector();
    private int ucsType = 0;
    private double ucsElevation = 0.0;
    private boolean useUCS = false;

    /**
     * @return Returns the backClipping.
     */
    public double getBackClipping() {
        return backClipping;
    }

    /**
     * @param backClipping
     *            The backClipping to set.
     */
    public void setBackClipping(double backClipping) {
        this.backClipping = backClipping;
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
     * @return Returns the frontClipping.
     */
    public double getFrontClipping() {
        return frontClipping;
    }

    /**
     * @param frontClipping
     *            The frontClipping to set.
     */
    public void setFrontClipping(double frontClipping) {
        this.frontClipping = frontClipping;
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
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the renderMode.
     */
    public int getRenderMode() {
        return renderMode;
    }

    /**
     * @param renderMode
     *            The renderMode to set.
     */
    public void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

    /**
     * @return Returns the target.
     */
    public Point getTarget() {
        return target;
    }

    /**
     * @param target
     *            The target to set.
     */
    public void setTarget(Point target) {
        this.target = target;
    }

    /**
     * @return Returns the twistAngle.
     */
    public double getTwistAngle() {
        return twistAngle;
    }

    /**
     * @param twistAngle
     *            The twistAngle to set.
     */
    public void setTwistAngle(double twistAngle) {
        this.twistAngle = twistAngle;
    }

    /**
     * @return Returns the ucsElevation.
     */
    public double getUcsElevation() {
        return ucsElevation;
    }

    /**
     * @param ucsElevation
     *            The ucsElevation to set.
     */
    public void setUcsElevation(double ucsElevation) {
        this.ucsElevation = ucsElevation;
    }

    /**
     * @return Returns the ucsOrigin.
     */
    public Vector getUcsOrigin() {
        return ucsOrigin;
    }

    /**
     * @param ucsOrigin
     *            The ucsOrigin to set.
     */
    public void setUcsOrigin(Vector ucsOrigin) {
        this.ucsOrigin = ucsOrigin;
    }

    /**
     * @return Returns the ucsType.
     */
    public int getUcsType() {
        return ucsType;
    }

    /**
     * @param ucsType
     *            The ucsType to set.
     */
    public void setUcsType(int ucsType) {
        this.ucsType = ucsType;
    }

    /**
     * @return Returns the ucsXAxis.
     */
    public Vector getUcsXAxis() {
        return ucsXAxis;
    }

    /**
     * @param ucsXAxis
     *            The ucsXAxis to set.
     */
    public void setUcsXAxis(Vector ucsXAxis) {
        this.ucsXAxis = ucsXAxis;
    }

    /**
     * @return Returns the ucsYAxis.
     */
    public Vector getUcsYAxis() {
        return ucsYAxis;
    }

    /**
     * @param ucsYAxis
     *            The ucsYAxis to set.
     */
    public void setUcsYAxis(Vector ucsYAxis) {
        this.ucsYAxis = ucsYAxis;
    }

    /**
     * @return Returns the viewDirection.
     */
    public Vector getViewDirection() {
        return viewDirection;
    }

    /**
     * @param viewDirection
     *            The viewDirection to set.
     */
    public void setViewDirection(Vector viewDirection) {
        this.viewDirection = viewDirection;
    }

    /**
     * @return Returns the width.
     */
    public double getWidth() {
        return width;
    }

    /**
     * @param width
     *            The width to set.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * @return Returns the useUCS.
     */
    public boolean isUseUCS() {
        return useUCS;
    }

    /**
     * @param useUCS The useUCS to set.
     */
    public void setUseUCS(boolean useUCS) {
        this.useUCS = useUCS;
    }
}
