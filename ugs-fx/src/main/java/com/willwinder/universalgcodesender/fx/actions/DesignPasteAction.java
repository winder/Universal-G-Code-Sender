/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import com.google.gson.JsonSyntaxException;
import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.universalgcodesender.i18n.Localization;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DesignPasteAction extends AbstractDesignEditAction {
    public static final String ICON_BASE = "icons/paste.svg";

    private static final Logger LOGGER = Logger.getLogger(DesignPasteAction.class.getSimpleName());

    public DesignPasteAction() {
        super(Localization.getString("platform.designer.paste"), ICON_BASE);
    }

    @Override
    protected void performAction() {
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
            List<Entity> entities = new UgsDesignReader().deserialize(data);
            controller.getDrawing().insertEntities(entities);
            controller.getSelectionManager().clearSelection();
            controller.getSelectionManager().setSelection(entities);
        } catch (UnsupportedFlavorException | JsonSyntaxException | IOException ex) {
            LOGGER.log(Level.INFO, "Unknown paste buffer data format, ignoring");
        }
    }
}
