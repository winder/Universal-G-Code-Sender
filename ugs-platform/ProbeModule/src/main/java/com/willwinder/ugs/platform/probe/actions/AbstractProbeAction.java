/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.probe.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.platform.probe.ProbeTopComponent;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractProbeAction extends AbstractAction implements UGSEventListener {

    private static final AtomicBoolean acceptedSettings = new AtomicBoolean(false);
    private final BackendAPI backend;

    public AbstractProbeAction() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        setEnabled(isEnabled());
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        // Ask the user if the current probe settings are ok
        if (!isParentTopComponent(e.getSource()) && !isOkToProbe()) {
            return;
        }

        performProbeAction();
    }

    /**
     * A method for executing the actual probe routine
     */
    public abstract void performProbeAction();

    /**
     * A text to use in popup for asking for confirmation on running the probe routine
     *
     * @return a text to display in popup
     */
    public abstract String getProbeConfirmationText();

    private boolean isOkToProbe() {
        if (acceptedSettings.get()) {
            return true;
        }

        ImageIcon largeIcon = (ImageIcon) getValue(LARGE_ICON_KEY);
        int result = JOptionPane.showConfirmDialog(null, new JLabel(getProbeConfirmationText(), largeIcon, JLabel.LEFT), getValue(NAME).toString(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        acceptedSettings.set(result == JOptionPane.OK_OPTION);
        return acceptedSettings.get();
    }

    @Override
    public boolean isEnabled() {
        return backend.isConnected() && backend.isIdle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            putValue(Action.SHORT_DESCRIPTION, Localization.getString("probe.action.tooltip.enabled"));
        } else {
            putValue(Action.SHORT_DESCRIPTION, Localization.getString("probe.action.tooltip.disabled"));
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            SwingUtilities.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    /**
     * Returns true if the passed source object is an ancestor of the ProbeTopComponent window.
     *
     * @param source a source object of any type
     * @return true if the source object is related to the ProbeTopComponent
     */
    private boolean isParentTopComponent(Object source) {
        if (source instanceof ProbeTopComponent) {
            return true;
        } else if (source instanceof Component) {
            return isParentTopComponent(((Component) source).getParent());
        }

        return false;
    }

    protected BackendAPI getBackend() {
        return backend;
    }
}
