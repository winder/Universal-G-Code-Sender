/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "UGS",
        displayName = "#AdvancedOption_DisplayName_AutoLeveler",
        keywords = "#AdvancedOption_Keywords_AutoLeveler",
        keywordsCategory = "UGS/AutoLeveler"
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_AutoLeveler=Auto Leveler", "AdvancedOption_Keywords_AutoLeveler=autoleveler, surface scanner"})
public class AutoLevelerOptionsPanelController extends AbstractOptionsPanelController {
    @Override
    public AbstractUGSSettings initPanel() {
        return new AutoLevelerSettingsPanel(settings, this);
    }
}