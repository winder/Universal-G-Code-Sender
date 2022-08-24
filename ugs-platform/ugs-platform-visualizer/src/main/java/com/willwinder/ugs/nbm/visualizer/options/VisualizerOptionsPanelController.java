/*
    Copyright 2016-2022 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.options;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        id = "Visualizer",
        location = "UGS",
        displayName = "#AdvancedOption_DisplayName_Visualizer",
        keywords = "#AdvancedOption_Keywords_Visualizer",
        keywordsCategory = VisualizerOptionsPanelController.KEYWORDS_CATEGORY

)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Visualizer=Visualizer", "AdvancedOption_Keywords_Visualizer=Visualizer"})
public class VisualizerOptionsPanelController extends AbstractOptionPanelController<VisualizerOptionsPanel> {

    public static final String KEYWORDS_CATEGORY = "UGS/Visualizer";

    @Override
    public VisualizerOptionsPanel getPanel() {
        if (panel == null) {
            panel = new VisualizerOptionsPanel(this);
        }
        return panel;
    }
}

