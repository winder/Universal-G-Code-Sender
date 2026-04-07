/*
   Copyright 2007 Simon Mieth

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
package org.kabeja.dxf.objects;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.helpers.Point;


public class DXFPlotSettings extends DXFObject {
    protected String name = "";
    protected String configName = "";
    protected String canonicalMediaName = "";
    protected String plotViewName = "";
    protected String currentStylesheet = "";

    /**
     * The margins stored [top,right,bottom,left]
     */
    protected double[] margin = new double[4];
    protected Point plotOrigin = new Point();
    protected double paperWidth;
    protected double paperHeight;
    protected int paperUnits = 0;
    protected int flags = 0;
    protected int plotType = 0;
    protected int plotRotation = 0;
    protected Bounds windowToPlot = new Bounds();
    protected double customScaleNumerator = 1.0;
    protected double customScaleDenominator = 1.0;

    public String getObjectType() {
        return DXFConstants.OBJECT_TYPE_PLOTSETTINGS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getCanonicalMediaName() {
        return canonicalMediaName;
    }

    public void setCanonicalMediaName(String canonicalMediaName) {
        this.canonicalMediaName = canonicalMediaName;
    }

    public String getPlotViewName() {
        return plotViewName;
    }

    public void setPlotViewName(String plotViewName) {
        this.plotViewName = plotViewName;
    }

    public String getCurrentStylesheet() {
        return currentStylesheet;
    }

    public void setCurrentStylesheet(String currentStylesheet) {
        this.currentStylesheet = currentStylesheet;
    }

    public double[] getMargin() {
        return margin;
    }

    public void setMargin(double[] margin) {
        this.margin = margin;
    }

    public Point getPlotOrigin() {
        return plotOrigin;
    }

    public void setPlotOrigin(Point plotOrigin) {
        this.plotOrigin = plotOrigin;
    }

    public double getPaperWidth() {
        return paperWidth;
    }

    public void setPaperWidth(double paperWidth) {
        this.paperWidth = paperWidth;
    }

    public double getPaperHeight() {
        return paperHeight;
    }

    public void setPaperHeight(double paperHeight) {
        this.paperHeight = paperHeight;
    }

    public int getPaperUnit() {
        return paperUnits;
    }

    public void setPaperUnit(int paperUnits) {
        this.paperUnits = paperUnits;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getPlotType() {
        return plotType;
    }

    public void setPlotType(int plotType) {
        this.plotType = plotType;
    }

    public int getPlotRotation() {
        return plotRotation;
    }

    public void setPlotRotation(int plotRotation) {
        this.plotRotation = plotRotation;
    }

    public Bounds getWindowToPlot() {
        return windowToPlot;
    }

    public void setWindowToPlot(Bounds windowToPlot) {
        this.windowToPlot = windowToPlot;
    }

    public double getCustomScaleNumerator() {
        return customScaleNumerator;
    }

    public void setCustomScaleNumerator(double customScaleNumerator) {
        this.customScaleNumerator = customScaleNumerator;
    }

    public double getCustomScaleDenominator() {
        return customScaleDenominator;
    }

    public void setCustomScaleDenominator(double customScaleDenominator) {
        this.customScaleDenominator = customScaleDenominator;
    }

    public double getCustomScale() {
        return this.customScaleNumerator / this.customScaleDenominator;
    }
}
