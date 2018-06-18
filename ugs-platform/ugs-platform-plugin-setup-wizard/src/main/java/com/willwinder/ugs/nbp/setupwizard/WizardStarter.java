/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.setupwizard;

import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelConnection;
import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelImportSettings;
import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelHardLimits;
import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelHoming;
import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelMotorWiring;
import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelSoftLimits;
import com.willwinder.ugs.nbp.setupwizard.panels.WizardPanelStepCalibration;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts the wizard
 *
 * @author Joacim Breiler
 */
public class WizardStarter {
    public static void openWizard(BackendAPI backend) {
        List<AbstractWizardPanel> panels = createWizardStepPanels(backend);

        WizardPanelIterator panelIterator = new WizardPanelIterator(panels);
        WizardDescriptor wizardDescriptor = createWizardDescriptor(panelIterator);
        panelIterator.initialize(wizardDescriptor);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();

        // Make sure all panels are destroyed after we finished the wizard
        panels.forEach(AbstractWizardPanel::destroy);
    }

    private static WizardDescriptor createWizardDescriptor(WizardPanelIterator panelIterator) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(panelIterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("<html><body><h1>{0}</h1></body></html>"));
        wizardDescriptor.setTitle("Setup wizard");
        wizardDescriptor.setModal(true);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        return wizardDescriptor;
    }

    /**
     * Creates all wizard step panels. New steps can be added, make sure to implement {@link AbstractWizardPanel#isEnabled()}
     * to make it visible in the wizard.
     *
     * @param backend the backend api
     * @return a list of wizard step panels
     */
    private static List<AbstractWizardPanel> createWizardStepPanels(BackendAPI backend) {
        List<AbstractWizardPanel> panels = new ArrayList<>();
        panels.add(new WizardPanelConnection(backend));
        panels.add(new WizardPanelImportSettings(backend));
        panels.add(new WizardPanelMotorWiring(backend));
        panels.add(new WizardPanelStepCalibration(backend));
        panels.add(new WizardPanelHardLimits(backend));
        panels.add(new WizardPanelHoming(backend));
        panels.add(new WizardPanelSoftLimits(backend));
        return panels;
    }
}
