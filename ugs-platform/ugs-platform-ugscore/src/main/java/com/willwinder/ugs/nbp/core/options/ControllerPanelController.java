/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.core.options;

import org.netbeans.spi.options.OptionsPanelController;

/**
 *
 * @author wwinder
 */
@OptionsPanelController.SubRegistration(
        location = "UGS",
        displayName = "#AdvancedOption_DisplayName_ControllerOptions",
        keywords = "#AdvancedOption_Keywords_ControllerOptions",
        keywordsCategory = "UGS/ControllerOptions"
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_ControllerOptions=Controller Options", "AdvancedOption_Keywords_ControllerOptions=UGS"})

public class ControllerPanelController extends AbstractOptionsPanelController {
    @Override
    UGSOptionsPanel initPanel() {
        return new UGSControllerOptionPanel(this);
    }
    
}
