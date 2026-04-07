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
public class StyledTextParagraph {
    public static final int VERTICAL_ALIGNMENT_TOP = 2;
    public static final int VERTICAL_ALIGNMENT_BASELINE = 1;
    public static final int VERTICAL_ALIGNMENT_BOTTOM = 0;
    public static final int VERTICAL_ALIGNMENT_CENTER = 4;
    private boolean italic = false;
    private boolean bold = false;
    private boolean underline = false;
    private boolean overline = false;
    private double fontHeight = 0.0;
    private String font = "";
    private StringBuffer text = new StringBuffer();
    private double width = 0.0;
    private double obliquiAngle = 0.0;
    private double characterspace = 0.0;
    private int lineIndex = 0;
    private boolean newline = false;
    private String fontFile = "";
    private int alignment = 1;
    private Point insertPoint = new Point();

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
     * @return Returns the characterspace.
     */
    public double getCharacterspace() {
        return characterspace;
    }

    /**
     * @param characterspace
     *            The characterspace to set.
     */
    public void setCharacterspace(double characterspace) {
        this.characterspace = characterspace;
    }

    /**
     * @return Returns the alignment.
     */
    public int getValign() {
        return alignment;
    }

    /**
     * @param alignment
     *            The alignment to set.
     */
    public void setValign(int alignment) {
        this.alignment = alignment;
    }

    /**
     * @return Returns the bold.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * @param bold
     *            The bold to set.
     */
    public void setBold(boolean bold) {
        this.bold = bold;
    }

    /**
     * @return Returns the font.
     */
    public String getFont() {
        return font;
    }

    /**
     * @param font
     *            The font to set.
     */
    public void setFont(String font) {
        this.font = font;
    }

    /**
     * @return Returns the height.
     */
    public double getFontHeight() {
        return fontHeight;
    }

    /**
     * @param height
     *            The height to set.
     */
    public void setFontHeight(double height) {
        this.fontHeight = height;
    }

    /**
     * @return Returns the italic.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * @param italic
     *            The italic to set.
     */
    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    /**
     * @return Returns the obliquiAngle.
     */
    public double getObliquiAngle() {
        return obliquiAngle;
    }

    /**
     * @param obliquiAngle
     *            The obliquiAngle to set.
     */
    public void setObliquiAngle(double obliquiAngle) {
        this.obliquiAngle = obliquiAngle;
    }

    /**
     * @return Returns the overline.
     */
    public boolean isOverline() {
        return overline;
    }

    /**
     * @param overline
     *            The overline to set.
     */
    public void setOverline(boolean overline) {
        this.overline = overline;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text.toString();
    }

    /**
     * @param text
     *            The text to set.
     */
    public void setText(String text) {
        this.text.delete(0, text.length());
        this.text.append(text);
    }

    /**
     * @return Returns the underline.
     */
    public boolean isUnderline() {
        return underline;
    }

    /**
     * @param underline
     *            The underline to set.
     */
    public void setUnderline(boolean underline) {
        this.underline = underline;
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
     * @return Returns the linecount.
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * @param linecount
     *            The linecount to set.
     */
    public void setLineIndex(int linecount) {
        this.lineIndex = linecount;
    }

    /**
     * @return Returns the newline.
     */
    public boolean isNewline() {
        return newline;
    }

    /**
     * @param newline
     *            The newline to set.
     */
    public void setNewline(boolean newline) {
        this.newline = newline;
    }

    public void append(String text) {
        this.text.append(text);
    }

    public void append(char c) {
        this.text.append(c);
    }

    public int getLength() {
        return text.length();
    }
}
