/*
    Copyright 2021 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;

/**
 * @author Joacim Breiler
 */
public class EntityTextV1 extends CuttableEntityV1 {
    private String text;
    private String fontName;

    public EntityTextV1() {
        super(EntityTypeV1.TEXT);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    @Override
    public Entity toInternal() {
        Text text = new Text();
        text.setFontFamily(fontName);
        text.setText(this.text);
        applyCommonAttributes(text);
        return text;
    }
}
