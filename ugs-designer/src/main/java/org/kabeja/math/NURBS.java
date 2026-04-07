/*
   Copyright 2008 Simon Mieth

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
package org.kabeja.math;

import org.kabeja.dxf.helpers.Point;


public class NURBS {
    protected Point[] controlPoints;
    protected double[] knots;
    protected double[] weights;
    protected int degree;
    protected boolean closed = false;

    public NURBS(Point[] controlPoints, double[] knots, double[] weights,
        int degree) {
        this.controlPoints = controlPoints;

        this.knots = knots;
        this.weights = weights;
        this.degree = degree;

        //some init stuff
        if (this.weights.length == 0) {
            this.weights = new double[this.controlPoints.length];
        }

        for (int i = 0; i < weights.length; i++) {
            if (weights[i] == 0.0) {
                weights[i] = 1.0;
            }
        }
    }

    public double[] getBasicFunctions(int i, double u) {
        double[] n = new double[degree + 1];
        n[0] = 1.0;

        double[] left = new double[degree + 1];
        double[] right = new double[degree + 1];

        for (int j = 1; j <= degree; j++) {
            left[j] = u - this.knots[(i + 1) - j];
            right[j] = this.knots[i + j] - u;

            double saved = 0.0;

            for (int r = 0; r < j; r++) {
                double t = n[r] / (right[r + 1] + left[j - r]);
                n[r] = saved + (right[r + 1] * t);
                saved = left[j - r] * t;
            }

            n[j] = saved;
        }

        return n;
    }

    public Point getPointAt(int i, double u) {
        Point p = new Point();
        double[] n = this.getBasicFunctions(i, u);

        double t = 0.0;

        for (int j = 0; j <= this.degree; j++) {
            int d = i - this.degree + j;
            double w = this.weights[d];

            p.setX(p.getX() + (n[j] * this.controlPoints[d].getX() * w));
            p.setY(p.getY() + (n[j] * this.controlPoints[d].getY() * w));
            p.setZ(p.getZ() + (n[j] * this.controlPoints[d].getZ() * w));
            t += (n[j] * w);
        }

        p.setX((p.getX() / t));
        p.setY(p.getY() / t);
        p.setZ(p.getZ() / t);

        return p;
    }

    public Point getPointAt(double u) {
        int interval = this.findSpawnIndex(u);

        return this.getPointAt(interval, u);
    }

    public int findSpawnIndex(double u) {
        if (u == this.knots[this.controlPoints.length + 1]) {
            return this.controlPoints.length;
        }

        int low = this.degree;
        int high = this.controlPoints.length + 1;
        int mid = (low + high) / 2;

        while ((u < this.knots[mid]) || (u >= this.knots[mid + 1])) {
            if (u < this.knots[mid]) {
                high = mid;
            } else {
                low = mid;
            }

            mid = (low + high) / 2;
        }

        return mid;
    }

    public Point[] getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(Point[] controlPoints) {
        this.controlPoints = controlPoints;
    }

    public double[] getKnots() {
        return knots;
    }

    public void setKnots(double[] knots) {
        this.knots = knots;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
