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
import javax.swing.event.ChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class WizardPanelIterator implements WizardDescriptor.Iterator<WizardDescriptor> {
    private final Logger logger = Logger.getLogger(WizardPanelIterator.class.getSimpleName());
    private final List<AbstractWizardPanel> panelList;
    private final Set<ChangeListener> listeners = new HashSet<>();
    private AbstractWizardPanel currentPanel;

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

    @Override
    public boolean hasNext() {
        return panelList.indexOf(currentPanel) < (panelList.size() - 1);
    }

    @Override
    public boolean hasPrevious() {
        return panelList.indexOf(currentPanel) != 0;
    }

    @Override
    public void nextPanel() {
        int nextIndex = panelList.indexOf(currentPanel) + 1;
        loadPanel(nextIndex);
    }

    @Override
    public void previousPanel() {
        int previousIndex = panelList.indexOf(currentPanel) - 1;
        loadPanel(previousIndex);
    }

    private void loadPanel(int index) {
        if (currentPanel != null) {
            logger.info("Destroying step: " + currentPanel.getClass().getSimpleName());
            currentPanel.destroy();
        }

        currentPanel = panelList.get(index);
        logger.info("Initializing step: " + currentPanel.getClass().getSimpleName());

        // Is this the last step?
        if( index == panelList.size() - 1) {
            currentPanel.setFinishPanel(true);
        }

        currentPanel.initialize();
        ((JComponent) currentPanel.getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
}
