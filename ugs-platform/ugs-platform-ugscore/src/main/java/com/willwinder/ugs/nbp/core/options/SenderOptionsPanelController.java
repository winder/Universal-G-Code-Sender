/*
    Copywrite 2016 Will Winder

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
        location = "UGS",
        displayName = "#AdvancedOption_DisplayName_Sender",
        keywords = "#AdvancedOption_Keywords_Sender",
        keywordsCategory = "UGS/Sender"
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_Sender=Sender",
    "AdvancedOption_Keywords_Sender=Sender"})
public class SenderOptionsPanelController extends AbstractOptionPanelController<SenderOptionsPanel> {
    @Override
    public SenderOptionsPanel getPanel() {
        if (panel == null) {
            panel = new SenderOptionsPanel(this);
        }
        return panel;
    }
}
