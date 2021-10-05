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

import org.kabeja.dxf.helpers.DXFTextParser;
import org.kabeja.dxf.helpers.DXFUtils;
import org.kabeja.dxf.helpers.TextDocument;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFMText extends DXFText {
    public static final int ATTACHMENT_TOP_LEFT = 1;
    public static final int ATTACHMENT_TOP_CENTER = 2;
    public static final int ATTACHMENT_TOP_RIGHT = 3;
    public static final int ATTACHMENT_MIDDLE_LEFT = 4;
    public static final int ATTACHMENT_MIDDLE_CENTER = 5;
    public static final int ATTACHMENT_MIDDLE_RIGHT = 6;
    public static final int ATTACHMENT_BOTTOM_LEFT = 7;
    public static final int ATTACHMENT_BOTTOM_CENTER = 8;
    public static final int ATTACHMENT_BOTTOM_RIGHT = 9;
    private int attachmentpointLocation = 1;
    private double refwidth = 0.0;
    private double refheight = 0.0;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.dxf.DXFText#processText(java.lang.String,
     *      org.xml.sax.ContentHandler)
     */
    public void setAttachmentPoint(int value) {
        this.attachmentpointLocation = value;
    }

    public void setReferenceWidth(double width) {
        this.refwidth = width;
    }

    public double getReferenceWidth() {
        return this.refwidth;
    }

    public void setReferenceHeight(double height) {
        this.refheight = height;
    }

    public double getReferenceHeight() {
        return this.refheight;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_MTEXT;
    }

    public double getRotation() {
        if (rotation != 0.0) {
            return rotation;
        } else if ((align_p1.getX() != 0.0) || (align_p1.getY() != 0.0) ||
                (align_p1.getZ() != 0.0)) {
            // the align point as direction vector here
            // calculate the angle between the x-axis and the direction-vector
            double[] x = { align_p1.getX(), align_p1.getY(), align_p1.getZ() };
            double v = align_p1.getX() / DXFUtils.vectorValue(x);
            v = Math.toDegrees(Math.acos(v));

            return v;
        }

        // same as 0.0
        return rotation;
    }

    public TextDocument getTextDocument() {
        return this.textDoc;
    }

    public void setText(String text) {
        this.text = text;

        this.textDoc = DXFTextParser.parseDXFMText(this);
    }

    public int getAlignment() {
        return attachmentpointLocation;
    }

    public boolean isOmitLineType() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.kabeja.dxf.DXFEntity#getBounds()
     */
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        int l = this.textDoc.getMaximumLineLength();

        if (l > 0) {
            double h = getHeight();

            if (h == 0.0) {
                h = getReferenceHeight();
            }

            double w = l * 0.7 * h;
            h *= this.textDoc.getLineCount();

            switch (this.attachmentpointLocation) {
            case ATTACHMENT_BOTTOM_CENTER:
                bounds.addToBounds(this.p.getX() + (w / 2), this.p.getY() + h,
                    p.getZ());
                bounds.addToBounds(this.p.getX() - (w / 2), this.p.getY(),
                    p.getZ());

                break;

            case ATTACHMENT_BOTTOM_LEFT:
                bounds.addToBounds(this.p.getX() + w, this.p.getY() + h,
                    p.getZ());
                bounds.addToBounds(this.p.getX(), this.p.getY(), p.getZ());

                break;

            case ATTACHMENT_BOTTOM_RIGHT:
                bounds.addToBounds(this.p.getX() - w, this.p.getY() + h,
                    p.getZ());
                bounds.addToBounds(this.p.getX(), this.p.getY(), p.getZ());

                break;

            case ATTACHMENT_MIDDLE_CENTER:
                bounds.addToBounds(this.p.getX() + (w / 2),
                    this.p.getY() + (h / 2), p.getZ());
                bounds.addToBounds(this.p.getX() - (w / 2),
                    this.p.getY() - (h / 2), p.getZ());

                break;

            case ATTACHMENT_MIDDLE_LEFT:
                bounds.addToBounds(this.p.getX(), this.p.getY() + (h / 2),
                    p.getZ());
                bounds.addToBounds(this.p.getX() + w, this.p.getY() - (h / 2),
                    p.getZ());

                break;

            case ATTACHMENT_MIDDLE_RIGHT:
                bounds.addToBounds(this.p.getX(), this.p.getY() + (h / 2),
                    p.getZ());
                bounds.addToBounds(this.p.getX() - w, this.p.getY() - (h / 2),
                    p.getZ());

                break;

            case ATTACHMENT_TOP_LEFT:
                bounds.addToBounds(this.p.getX(), this.p.getY(), p.getZ());
                bounds.addToBounds(this.p.getX() + w, this.p.getY() - h,
                    p.getZ());

                break;

            case ATTACHMENT_TOP_CENTER:
                bounds.addToBounds(this.p.getX() + (w / 2), this.p.getY(),
                    p.getZ());
                bounds.addToBounds(this.p.getX() - (w / 2), this.p.getY() - h,
                    p.getZ());

                break;

            case ATTACHMENT_TOP_RIGHT:
                bounds.addToBounds(this.p.getX(), this.p.getY(), p.getZ());
                bounds.addToBounds(this.p.getX() - w, this.p.getY() - h,
                    p.getZ());

                break;
            }
        } else {
            bounds.setValid(false);
        }

        return bounds;
    }
}
