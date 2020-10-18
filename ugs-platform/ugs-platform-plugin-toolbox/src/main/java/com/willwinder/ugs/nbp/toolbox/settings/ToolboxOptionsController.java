/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.toolbox.settings;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionPanelController;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
        id = "Toolbox",
        location = "UGS",
        displayName = "#AdvancedOption_DisplayName_Toolbox",
        keywords = "#AdvancedOption_Keywords_Toolbox",
        keywordsCategory = ToolboxOptionsController.KEYWORDS_CATEGORY
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Toolbox=Toolbox", "AdvancedOption_Keywords_Toolbox=toolbox, actions"})
public class ToolboxOptionsController extends AbstractOptionPanelController<ToolboxOptionsPanel> {
    public static final String KEYWORDS_CATEGORY = "UGS/Toolbox";

    private final ActionRegistrationService actionRegistrationService;
    private ToolboxOptionsPanel panel;

    public ToolboxOptionsController() {
        actionRegistrationService = Lookup.getDefault().lookup(ActionRegistrationService.class);
    }

    @Override
    public ToolboxOptionsPanel getPanel() {
        if (panel == null) {
            panel = new ToolboxOptionsPanel(this);
        }
        return panel;
    }

    public ActionRegistrationService getActionRegistrationService() {
        return actionRegistrationService;
    }
}

