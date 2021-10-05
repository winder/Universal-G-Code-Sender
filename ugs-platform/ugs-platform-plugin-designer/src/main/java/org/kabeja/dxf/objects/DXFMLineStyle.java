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
package org.kabeja.dxf.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kabeja.dxf.DXFConstants;


public class DXFMLineStyle extends DXFObject {
    protected List lines = new ArrayList();
    protected String name = "";
    protected String descrition = "";
    protected int fillColor = 256;
    protected int flags = 0;
    protected double startAngle = 0;
    protected double endAngle = 0;

    public String getObjectType() {
        return DXFConstants.OBJECT_TYPE_MLINESTYLE;
    }

    public void addDXFMLineStyleElement(DXFMLineStyleElement e) {
        this.lines.add(e);
    }

    public DXFMLineStyleElement getDXFMLineStyleLElement(int index) {
        return (DXFMLineStyleElement) this.lines.get(index);
    }

    public DXFMLineStyleElement removeDXFMLineStyleLElement(int index) {
        return (DXFMLineStyleElement) this.lines.remove(index);
    }

    public int getDXFMLineStyleLElementCount() {
        return this.lines.size();
    }

    public void sortDXFMLineStyleElements(Comparator comp) {
        Collections.sort(this.lines, comp);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescrition() {
        return descrition;
    }

    public void setDescrition(String descrition) {
        this.descrition = descrition;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    public boolean isFilled() {
        return (this.flags & 1) == 1;
    }

    public boolean hasStartSquareCaps() {
        return (this.flags & 16) == 16;
    }

    public boolean hasStartRoundCaps() {
        return (this.flags & 64) == 64;
    }

    public boolean hasStartInnerArcs() {
        return (this.flags & 32) == 32;
    }

    public boolean hasEndSquareCaps() {
        return (this.flags & 256) == 256;
    }

    public boolean hasEndRoundCaps() {
        return (this.flags & 1024) == 1024;
    }

    public boolean hasEndInnderArcs() {
        return (this.flags & 512) == 512;
    }

    public boolean showMiters() {
        return (this.flags & 2) == 2;
    }
}
