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

import com.willwinder.universalgcodesender.model.BackendAPI;
import net.miginfocom.swing.MigLayout;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract base wizard step panel that will handle events for showing and hiding the step, handle finished event,
 * wrap with scrollbars and registering its name to the wizard.
 * <p>
 * Extend this class and add your components using {@link #getPanel()}.
 *
 * @author Joacim Breiler
 */
public abstract class AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private final BackendAPI backend;

    /**
     * The name of the step to be displayed
     */
    private final String name;

    /**
     * The panel with the actual panel content
     */
    private final JPanel panel;

    /**
     * The root panel to display
     */
    private final Component rootComponent;

    /**
     * A set of listeners
     */
    private final Set<ChangeListener> listeners = new HashSet<>();

    /**
     * If the panel form is valid and should be able to go to the next step
     */
    private boolean isValid;

    /**
     * If this panel is finishable
     */
    private boolean isFinishPanel;

    private String errorMessage;

    /**
     * Constructs a wizard panel which can be used to display a step in a wizard
     *
     * @param backend the backend api
     * @param name    the name of the step
     */
    public AbstractWizardPanel(BackendAPI backend, String name) {
        this(backend, name, false);
    }

    public AbstractWizardPanel(BackendAPI backend, String name, boolean debug) {
        String layoutConstraints = "hidemode 3, fill, w 300:560, h 200:320";
        if (debug) {
            layoutConstraints += ", debug";
        }

        this.backend = backend;
        this.name = name;
        this.panel = new JPanel(new MigLayout(layoutConstraints));

        JScrollPane scrollPane = new JScrollPane(this.panel);
        scrollPane.setBorder(null);
        scrollPane.setName(name);
        this.rootComponent = scrollPane;
    }

    /**
     * The panel that can be used to add new content to.
     *
     * @return a panel that can be used to add new content to
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Returns the root component. This is not intended to be used
     * when constructing a wizard panel layout. Instead use {@link #getPanel()}
     *
     * @return the root component
     */
    @Override
    public Component getComponent() {
        return rootComponent;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        // Not used in default implementation
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // Not used in default implementation
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        if (isValid != this.isValid) {
            this.isValid = isValid;
            fireChange();
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public BackendAPI getBackend() {
        return backend;
    }

    public String getName() {
        return name;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        fireChange();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isFinishPanel() {
        return isFinishPanel;
    }

    public void setFinishPanel(boolean finishable) {
        if (isFinishPanel != finishable) {
            isFinishPanel = finishable;
            fireChange();
        }
    }

    private void fireChange() {
        listeners.forEach(l -> l.stateChanged(new ChangeEvent(this)));
    }

    /**
     * When the panel step is loaded and is about to be shown this method is called.
     */
    public abstract void initialize();

    /**
     * When the panel step has finished and before next step is loaded
     */
    public abstract void destroy();

    /**
     * Should the panel be visible in the list of setup steps.
     *
     * @return true if the panel should be visible among the steps.
     */
    public abstract boolean isEnabled();
}
