/*
    Copyright 2016 Will Winder

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
package com.willwinder.ugs.nbp.core.options;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        id = "Macros",
        location = "UGS",
        displayName = "#AdvancedOption_DisplayName_Macros",
        keywords = "#AdvancedOption_Keywords_Macros",
        keywordsCategory = MacrosOptionsPanelController.KEYWORDS_CATEGORY
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Macros=Macros", "AdvancedOption_Keywords_Macros=macros"})
public class MacrosOptionsPanelController extends AbstractOptionPanelController<MacrosPanel> {
    public static final String KEYWORDS_CATEGORY = "UGS/Macros";

    @Override
    public MacrosPanel getPanel() {
        if (panel == null) {
            panel = new MacrosPanel(this);
        }
        return panel;
    }
}