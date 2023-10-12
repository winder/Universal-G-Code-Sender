/*
    Copyright 2018-2020 Will Winder

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
package com.willwinder.ugs.nbp.setupwizard.panels;

import com.willwinder.ugs.nbp.setupwizard.AbstractWizardPanel;
import com.willwinder.ugs.nbp.setupwizard.components.ConnectionPanel;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A wizard step panel for connecting to a controller.
 *
 * @author Joacim Breiler
 */
public class WizardPanelConnection extends AbstractWizardPanel implements UGSEventListener {
    private boolean isErrorConnecting = false;
    private ConnectionPanel connectionPanel;
    private JLabel labelTitle;
    private JLabel labelNotSupported;
    private JLabel labelNotRecognized;
    private JButton resetConnectionButton;

    public WizardPanelConnection(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.connection.title"), false);

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));
        panel.add(connectionPanel, "growx, wrap");
        panel.add(labelTitle, "grow, aligny top, wrap");
        panel.add(labelNotSupported, "grow, aligny top, wrap");
        panel.add(labelNotRecognized, "grow, aligny top, wrap");
        panel.add(resetConnectionButton, "wmin 250, hmin 36, wrap, gaptop 10");
        getPanel().add(panel, "aligny top, growx");
    }

    private void initComponents() {
        connectionPanel = new ConnectionPanel(getBackend(), this::onConnect);
        connectionPanel.setVisible(true);

        labelNotSupported = new JLabel("<html><body><p>" + Localization.getString("platform.plugin.setupwizard.connection.not-supported") + "</p></body></html>");
        labelNotSupported.setIcon(ImageUtilities.loadImageIcon("icons/information24.png", false));
        labelNotSupported.setVisible(false);

        labelNotRecognized = new JLabel("<html><body><p>" + Localization.getString("platform.plugin.setupwizard.connection.error-connecting") + "</p></body></html>");
        labelNotSupported.setIcon(ImageUtilities.loadImageIcon("icons/information24.png", false));
        labelNotSupported.setVisible(false);

        labelTitle = new JLabel(Localization.getString("platform.plugin.setupwizard.unknown-version"));
        labelTitle.setVisible(false);

        resetConnectionButton = new JButton(Localization.getString("platform.plugin.setupwizard.connection.retry"));
        resetConnectionButton.setVisible(false);
        resetConnectionButton.addActionListener(e -> {
            isErrorConnecting = false;
            resetConnectionButton.setVisible(false);
            refreshComponents();
        });
    }

    private void onConnect() {
        try {
            isErrorConnecting = false;
            connectionPanel.setVisible(false);
            Settings settings = getBackend().getSettings();
            getBackend().connect(settings.getFirmwareVersion(), settings.getPort(), Integer.parseInt(settings.getPortRate()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refreshComponents();
    }

    private void refreshComponents() {
        ControllerState controllerState = getBackend().getControllerState();
        boolean isConnected = getBackend().isConnected() && (controllerState == ControllerState.IDLE || controllerState == ControllerState.ALARM || controllerState == ControllerState.HOLD);
        setValid(isConnected && getBackend().getController().getCapabilities().hasSetupWizardSupport());

        // Reset panels
        connectionPanel.setVisible(false);
        labelNotSupported.setVisible(false);
        labelNotRecognized.setVisible(false);
        labelTitle.setVisible(false);
        labelTitle.setIcon(null);
        resetConnectionButton.setVisible(false);
        setFinishPanel(false);

        if (isConnected && getBackend().getController().getCapabilities().hasSetupWizardSupport()) {
            labelTitle.setVisible(true);
            labelTitle.setText("<html><body><h2> " + Localization.getString("platform.plugin.setupwizard.connection.connected-to") + " " + getBackend().getController().getFirmwareVersion() + "</h2></body></html>");
            labelTitle.setIcon(ImageUtilities.loadImageIcon("icons/checked24.png", false));
        } else if (isConnected && !getBackend().getController().getCapabilities().hasSetupWizardSupport()) {
            labelTitle.setVisible(true);
            labelTitle.setText("<html><body><h2>" + Localization.getString("platform.plugin.setupwizard.connection.connected-to") + " " + getBackend().getController().getFirmwareVersion() + "</h2></body></html>");
            labelNotSupported.setVisible(true);
            setFinishPanel(true);
        } else if (controllerState == ControllerState.CONNECTING) {
            labelTitle.setVisible(true);
            labelTitle.setText("<html><body><h2>" + Localization.getString("platform.plugin.setupwizard.connection.connecting") + "</h2></body></html>");
        } else if (isErrorConnecting) {
            labelTitle.setVisible(true);
            labelTitle.setText("<html><body><h2>" + Localization.getString("platform.plugin.setupwizard.connection.error-title") + "</h2></body></html>");
            labelNotRecognized.setVisible(true);
            resetConnectionButton.setVisible(true);
        } else if (controllerState == ControllerState.DISCONNECTED) {
            connectionPanel.setVisible(true);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent controllerStateEvent) {
            ControllerState state = controllerStateEvent.getState();
            if (state == ControllerState.DISCONNECTED) {
                isErrorConnecting = true;
            }

            refreshComponents();
        }
    }
}
