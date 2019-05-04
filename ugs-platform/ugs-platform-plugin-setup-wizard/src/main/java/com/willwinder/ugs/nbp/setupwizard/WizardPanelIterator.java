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

import org.openide.WizardDescriptor;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The wizard panel iterator will handle the active step, the step names and keep track if they
 * should be enabled.
 *
 * @author Joacim Breiler
 */
public class WizardPanelIterator implements WizardDescriptor.Iterator<WizardDescriptor>, ChangeListener {
    private final Logger logger = Logger.getLogger(WizardPanelIterator.class.getSimpleName());
    private final List<AbstractWizardPanel> panelList;
    private final Set<ChangeListener> listeners = new HashSet<>();
    private AbstractWizardPanel currentPanel;
    private WizardDescriptor wizardDescriptor;

    public WizardPanelIterator(List<AbstractWizardPanel> panelList) {
        this.panelList = panelList;
        loadPanel(0);
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return currentPanel;
    }

    @Override
    public String name() {
        return currentPanel.getName();
    }

    private List<AbstractWizardPanel> getEnabledSteps() {
        return panelList.stream()
                .filter(AbstractWizardPanel::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasNext() {
        return getEnabledSteps().indexOf(currentPanel) < (getEnabledSteps().size() - 1);
    }

    @Override
    public boolean hasPrevious() {
        return getEnabledSteps().indexOf(currentPanel) != 0;
    }

    @Override
    public void nextPanel() {
        int nextIndex = getEnabledSteps().indexOf(currentPanel) + 1;
        loadPanel(nextIndex);
    }

    @Override
    public void previousPanel() {
        int previousIndex = getEnabledSteps().indexOf(currentPanel) - 1;
        loadPanel(previousIndex);
    }

    private void loadPanel(int index) {
        if (currentPanel != null) {
            logger.info("Destroying step: " + currentPanel.getClass().getSimpleName());
            currentPanel.removeChangeListener(this);
            currentPanel.destroy();
        }

        currentPanel = getEnabledSteps().get(index);
        currentPanel.addChangeListener(this);
        logger.info("Initializing step: " + currentPanel.getClass().getSimpleName());

        // Is this the last step?
        if (index == getEnabledSteps().size() - 1) {
            currentPanel.setFinishPanel(true);
        }

        currentPanel.initialize();
        ((JComponent) currentPanel.getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);

        // Get the step names and set their indexes
        ((JComponent) currentPanel.getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getSteps());
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private String[] getSteps() {
        String[] steps = new String[getEnabledSteps().size()];
        for (int i = 0; i < getEnabledSteps().size(); i++) {
            steps[i] = getEnabledSteps().get(i).getName();
        }
        return steps;
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    /**
     * Receive events from the step panels and update the wizard descriptor.
     *
     * @param e the fired event
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (wizardDescriptor != null) {
            // When a wizard step has changed, reload all steps in case new steps should be enabled.
            wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, getSteps());
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, currentPanel.getErrorMessage());
        } else {
            logger.warning("No reference to the wizard descriptor set, we will not know if the steps have changed!");
        }
    }
}
