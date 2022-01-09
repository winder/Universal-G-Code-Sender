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
package com.willwinder.ugs.nbp.core.ui;

import org.openide.awt.ToolbarWithOverflow;

import javax.swing.*;
import javax.swing.plaf.ToolBarUI;
import java.awt.*;

/**
 * A generic toolbar with overflow and the netbeans LaF.
 *
 * @author Joacim Breiler
 */
public class ToolBar extends ToolbarWithOverflow {

    public static final String TOOLBAR_UI = "Nb.Toolbar.ui";

    public ToolBar() {
        super();
        add(Box.createRigidArea(new Dimension(1, 32)));
    }

    @Override
    public String getUIClassID() {
        //For GTK and Aqua look and feels, NetBeans provide a custom toolbar UI -
        //but we cannot override this globally or it will cause problems for
        //the form editor & other things
        if (UIManager.get(TOOLBAR_UI) != null) {
            return TOOLBAR_UI;
        } else {
            return super.getUIClassID();
        }
    }

    @Override
    public String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorToolbar";
    }

    @Override
    public void setUI(ToolBarUI ui) {
        super.setUI(ui);
    }
}
