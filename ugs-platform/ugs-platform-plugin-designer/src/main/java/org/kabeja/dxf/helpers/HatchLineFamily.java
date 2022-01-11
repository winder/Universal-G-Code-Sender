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
package org.kabeja.dxf.helpers;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class HatchLineFamily {
    private double rotationAngle;
    private double baseX;
    private double baseY;
    private double offsetX;
    private double offsetY;
    private double length = 0.0;
    private double[] pattern = new double[0];

    /**
     * @return Returns the baseX.
     */
    public double getBaseX() {
        return baseX;
    }

    /**
     * @param baseX
     *            The baseX to set.
     */
    public void setBaseX(double baseX) {
        this.baseX = baseX;
    }

    /**
     * @return Returns the baseY.
     */
    public double getBaseY() {
        return baseY;
    }

    /**
     * @param baseY
     *            The baseY to set.
     */
    public void setBaseY(double baseY) {
        this.baseY = baseY;
    }

    /**
     * @return Returns the offsetX.
     */
    public double getOffsetX() {
        return offsetX;
    }

    /**
     * @param offsetX
     *            The offsetX to set.
     */
    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * @return Returns the offsetY.
     */
    public double getOffsetY() {
        return offsetY;
    }

    /**
     * @param offsetY
     *            The offsetY to set.
     */
    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * @return Returns the pattern.
     */
    public double[] getPattern() {
        return pattern;
    }

    /**
     * @param pattern
     *            The pattern to set.
     */
    public void setPattern(double[] pattern) {
        if (pattern != null) {
            this.pattern = pattern;
        }
    }

    /**
     * @return Returns the rotationAngle.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * @param rotationAngle
     *            The rotationAngle to set.
     */
    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public double getLength() {
        if (length <= 0.0) {
            for (int i = 0; i < pattern.length; i++) {
                this.length += Math.abs(pattern[i]);
            }
        }

        return this.length;
    }

    public double getPatternWidth() {
        return (getLength() * Math.cos(this.rotationAngle));
    }

    public double getPatternHeight() {
        return (getLength() * Math.sin(this.rotationAngle));
    }

    public Point getBasePoint() {
        return transform(this.baseX, this.baseY);
    }

    public Point getOffsetPoint() {
        return transform(this.offsetX, this.offsetY);
    }

    protected Point transform(double x, double y) {
        Point p = new Point();
        p.setX((Math.cos(this.rotationAngle) * x) +
            (Math.sin(this.rotationAngle) * y));
        p.setY((Math.cos(this.rotationAngle) * y) -
            (Math.sin(this.rotationAngle) * x));

        return p;
    }

    public Point getMinimalBasePoint() {
        Point p = new Point();
        Point b = getBasePoint();
        Point o = getOffsetPoint();
        p.setX(b.getX() % o.getX());
        p.setY(b.getY() % o.getY());

        return p;
    }
}
