/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.platform;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openide.cookies.SaveCookie;
import org.openide.util.ImageUtilities;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;

/**
 * @author Joacim Breiler
 */
public class UgsSaveCookie implements Icon, SaveCookie {
    private final UgsDataObject dataObject;
    private final Icon icon = ImageUtilities.loadImageIcon("img/new.svg", false);

    public UgsSaveCookie(UgsDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public void save() {
        saveDesign();
        PlatformUtils.exportAndLoadGcode(dataObject.getName());
    }

    private void saveDesign() {
        UgsDesignWriter writer = new UgsDesignWriter();
        writer.write(new File(dataObject.getPrimaryFile().getPath()), ControllerFactory.getController());
        dataObject.setModified(false);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
