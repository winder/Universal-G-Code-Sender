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
package com.willwinder.ugs.nbm.visualizer.actions;

import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanelController;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.CATEGORY_VISUALIZER,
        id = EditVisualizerOptionsAction.ID)
@ActionRegistration(
        displayName = "Edit visualizer options...",
        lazy = false)
@ActionReferences({
        @ActionReference(
                separatorBefore = 2049,
                path = LocalizingService.MENU_VISUALIZER,
                position = 2050)
})
public class EditVisualizerOptionsAction extends AbstractAction {
    public static final String ID = "com.willwinder.ugs.nbm.visualizer.actions.EditVisualizerOptionsAction";

    public EditVisualizerOptionsAction() {
        putValue("menuText", LocalizingService.EditVisualizerOptionsTitle);
        putValue(NAME, LocalizingService.EditVisualizerOptionsTitle);
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open(VisualizerOptionsPanelController.KEYWORDS_CATEGORY);
    }
}
