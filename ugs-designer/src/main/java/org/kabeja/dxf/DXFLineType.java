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


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 *
 */
public class DXFLineType {
    private String name = "";
    private String descritpion = "";
    private double totalPatternLength = 0.0;
    private double[] pattern;
    private int elementCount = 0;
    private int[] offsetX;
    private int[] offsetY;
    private int alignment;
    protected double scale = 1.0;

    public String getDescritpion() {
        return descritpion;
    }

    public void setDescritpion(String descritpion) {
        this.descritpion = descritpion;
    }

    public int getSegmentCount() {
        return elementCount;
    }

    public void setSegmentCount(int elementCount) {
        this.elementCount = elementCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[] getPattern() {
        return pattern;
    }

    public void setPattern(double[] pattern) {
        this.pattern = pattern;
    }

    public double getPatternLength() {
        return totalPatternLength;
    }

    public void setPatternLength(double patternLength) {
        this.totalPatternLength = patternLength;
    }

    /**
     * @return Returns the alignment.
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * @param alignment
     *            The alignment to set.
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * @return Returns the scale.
     */
    public double getScale() {
        return scale;
    }

    /**
     * @param scale The scale to set.
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean isScaleToFit() {
        if (alignment == 83) {
            return true;
        }

        return false;
    }
}
