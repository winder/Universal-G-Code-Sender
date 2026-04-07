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
 */
public class DXFStyle {
    private String name = "";
    private String fontFile = "";
    private String bigFontFile = "";
    private double textHeight = 0.0;
    private double widthFactor = 1.0;
    private double obliqueAngle = 0.0;
    private int textGenerationFlag = 0;
    private int flags = 0;
    private double lastHeight = 0.0;

    /**
     * @return Returns the bigFontFile.
     */
    public String getBigFontFile() {
        return bigFontFile;
    }

    /**
     * @param bigFontFile
     *            The bigFontFile to set.
     */
    public void setBigFontFile(String bigFontFile) {
        this.bigFontFile = bigFontFile;
    }

    /**
     * @return Returns the fontFile.
     */
    public String getFontFile() {
        return fontFile;
    }

    /**
     * @param fontFile
     *            The fontFile to set.
     */
    public void setFontFile(String fontFile) {
        this.fontFile = fontFile;
    }

    /**
     * @return Returns the lastHeight.
     */
    public double getLastHeight() {
        return lastHeight;
    }

    /**
     * @param lastHeight
     *            The lastHeight to set.
     */
    public void setLastHeight(double lastHeight) {
        this.lastHeight = lastHeight;
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
     * @return Returns the obliqueAngle.
     */
    public double getObliqueAngle() {
        return obliqueAngle;
    }

    /**
     * @param obliqueAngle
     *            The obliqueAngle to set.
     */
    public void setObliqueAngle(double obliqueAngle) {
        this.obliqueAngle = obliqueAngle;
    }

    /**
     * @return Returns the textGenerationFlag.
     */
    public int getTextGenerationFlag() {
        return textGenerationFlag;
    }

    /**
     * @param textGenerationFlag
     *            The textGenerationFlag to set.
     */
    public void setTextGenerationFlag(int textGenerationFlag) {
        this.textGenerationFlag = textGenerationFlag;
    }

    /**
     * @return Returns the textHeight.
     */
    public double getTextHeight() {
        return textHeight;
    }

    /**
     * @param textHeight
     *            The textHeight to set.
     */
    public void setTextHeight(double textHeight) {
        this.textHeight = textHeight;
    }

    /**
     * @return Returns the widthFactor.
     */
    public double getWidthFactor() {
        return widthFactor;
    }

    /**
     * @param widthFactor
     *            The widthFactor to set.
     */
    public void setWidthFactor(double widthFactor) {
        this.widthFactor = widthFactor;
    }

    /**
     * @return Returns the flags.
     */
    public int getFlags() {
        return flags;
    }

    /**
     * @param flags
     *            The flags to set.
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isBackward() {
        return this.textGenerationFlag == 2;
    }

    public void setBackward(boolean b) {
        if (b) {
            this.textGenerationFlag = 2;
        } else {
            this.textGenerationFlag = 0;
        }
    }

    public boolean isUpsideDown() {
        return this.textGenerationFlag == 4;
    }

    public void setUpsideDown(boolean b) {
        if (b) {
            this.textGenerationFlag = 4;
        } else {
            this.textGenerationFlag = 0;
        }
    }
}
