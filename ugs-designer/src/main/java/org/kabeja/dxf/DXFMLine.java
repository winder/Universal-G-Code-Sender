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

import java.util.ArrayList;
import java.util.List;

import org.kabeja.dxf.helpers.DXFMLineSegment;
import org.kabeja.dxf.helpers.MLineConverter;
import org.kabeja.dxf.helpers.Point;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFMLine extends DXFEntity {
    public final static int JUSTIFICATION_TOP = 0;
    public final static int JUSTIFICATION_ZERO = 1;
    public final static int JUSTIFICATION_BOTTOM = 2;
    protected double scale = 1.0;
    protected Point startPoint = new Point();
    protected List mlineSegments = new ArrayList();
    protected int lineCount = 0;
    protected int justification = 0;
    protected String mLineStyleID = "";
    protected String mLineStyleName = "";

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.dxf.DXFEntity#getBounds()
     */
    public Bounds getBounds() {
        Bounds b = new Bounds();
        DXFPolyline[] pl = this.toDXFPolylines();

        for (int i = 0; i < pl.length; i++) {
            b.addToBounds(pl[i].getBounds());
        }

        // b.setValid(false);
        return b;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.dxf.DXFEntity#getType()
     */
    public String getType() {
        return DXFConstants.ENTITY_TYPE_MLINE;
    }

    public double getLength() {
        //TODO convert  mline -> polyline only  after changes
        DXFPolyline[] pl = toDXFPolylines();
        double l = 0;

        for (int i = 0; i < pl.length; i++) {
            l += pl[i].getLength();
        }

        return l;
    }

    public void addDXFMLineSegement(DXFMLineSegment seg) {
        this.mlineSegments.add(seg);
    }

    public int getDXFMLineSegmentCount() {
        return this.mlineSegments.size();
    }

    public DXFMLineSegment getDXFMLineSegment(int index) {
        return (DXFMLineSegment) this.mlineSegments.get(index);
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public String getMLineStyleID() {
        return mLineStyleID;
    }

    public void setMLineStyleID(String lineStyleID) {
        mLineStyleID = lineStyleID;
    }

    public int getJustification() {
        return justification;
    }

    public void setJustification(int justification) {
        this.justification = justification;
    }

    public String getMLineStyleName() {
        return mLineStyleName;
    }

    public void setMLineStyleName(String lineStyleName) {
        mLineStyleName = lineStyleName;
    }

    protected DXFPolyline[] toDXFPolylines() {
        return MLineConverter.toDXFPolyline(this);
    }

    public boolean isClosed() {
        return (this.flags & 2) == 2;
    }
}
