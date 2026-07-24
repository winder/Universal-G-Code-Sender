/*
    Copyright 2018-2023 Will Winder

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
package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.jog.actions.ShowABCStepSizeAction;
import com.willwinder.ugs.nbp.jog.actions.UseSeparateStepSizeAction;
import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.JPopupMenu;
import java.awt.BorderLayout;

/**
 * The jog control panel in NetBeans
 *
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "JogTopComponent"
)
@TopComponent.Registration(
        mode = Mode.LEFT_BOTTOM,
        openAtStartup = true,
        position = 5000)
@ActionID(
        category = JogTopComponent.CATEGORY,
        id = JogTopComponent.ACTION_ID)
@ActionReference(
        path = JogTopComponent.WINOW_PATH)
@TopComponent.OpenActionRegistration(
        displayName = "Jog Controller",
        preferredID = "JogTopComponent"
)
public final class JogTopComponent extends TopComponent implements UGSEventListener, JogPanelListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW_PLUGIN;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.jog.JogTopComponent";

    private final BackendAPI backend;
    private final JogPanel jogPanel;
    private final JogService jogService;
    private final JogButtonHandler jogButtonHandler;

    public JogTopComponent() {
        backend = LookupService.lookup(BackendAPI.class);
        jogService = LookupService.lookup(JogService.class);
        jogButtonHandler = new JogButtonHandler(backend, jogService);
        UseSeparateStepSizeAction separateStepSizeAction = Lookup.getDefault().lookup(UseSeparateStepSizeAction.class);
        ShowABCStepSizeAction showABCStepSizeAction = Lookup.getDefault().lookup(ShowABCStepSizeAction.class);

        jogPanel = new JogPanel();
        jogPanel.setEnabled(jogService.canJog());
        updateSettings();
        jogPanel.addListener(this);


        setLayout(new BorderLayout());
        add(jogPanel, BorderLayout.CENTER);

        // Right click options
        if (separateStepSizeAction != null || showABCStepSizeAction != null) {
            JPopupMenu popupMenu = new JPopupMenu();
            if (separateStepSizeAction != null) {
                popupMenu.add(separateStepSizeAction);
            }
            if (showABCStepSizeAction != null) {
                popupMenu.add(showABCStepSizeAction);
            }
            SwingHelpers.traverse(this, (comp) -> comp.setComponentPopupMenu(popupMenu));
        }
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(this);
        jogButtonHandler.destroy();
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);
        updateControls();
        updateSettings();
        backend.addUGSEventListener(this);
        jogButtonHandler.init();
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent || event instanceof SettingChangedEvent) {
            updateSettings();
            updateControls();
        }
    }

    private void updateSettings() {
        jogPanel.setFeedRate(Double.valueOf(jogService.getFeedRate()).intValue());
        jogPanel.setStepSizeXY(jogService.getStepSizeXY());
        jogPanel.setStepSizeZ(jogService.getStepSizeZ());
        jogPanel.setStepSizeABC(jogService.getStepSizeABC());
        jogPanel.setUnit(jogService.getUnits());
        jogPanel.enabledStepSizes(jogService.useStepSizeZ(), jogService.showABCStepSize());

        checkAxisEnabled(Axis.A);
        checkAxisEnabled(Axis.B);
        checkAxisEnabled(Axis.C);
    }

    private void checkAxisEnabled(Axis axis) {
        Settings settings = backend.getSettings();
        jogPanel.setButtonsVisible(axis, settings.isAxisEnabled(axis) && backend.getController() != null && backend.getController().getCapabilities().hasAxis(axis));
    }

    private void updateControls() {
        jogPanel.setEnabled(jogService.canJog());
    }

    @Override
    public void onJogButtonClicked(JogPanelButtonEnum button) {
        jogButtonHandler.onJogButtonClicked(button);
    }

    @Override
    public void onJogButtonLongPressed(JogPanelButtonEnum button) {
        jogButtonHandler.onJogButtonLongPressed(button);
    }

    @Override
    public void onJogButtonLongReleased(JogPanelButtonEnum button) {
        jogButtonHandler.onJogButtonLongReleased();
    }

    @Override
    public void onStepSizeZChanged(double value) {
        jogService.setStepSizeZ(value);
    }

    @Override
    public void onStepSizeXYChanged(double value) {
        jogService.setStepSizeXY(value);
    }

    @Override
    public void onStepSizeABCChanged(double value) {
        jogService.setStepSizeABC(value);
    }

    @Override
    public void onFeedRateChanged(int value) {
        jogService.setFeedRate(value);
    }

    @Override
    public void onToggleUnit() {
        if (jogService.getUnits() == UnitUtils.Units.MM) {
            jogService.setUnits(UnitUtils.Units.INCH);
        } else {
            jogService.setUnits(UnitUtils.Units.MM);
        }
    }

    @Override
    public void onIncreaseStepSize() {
        jogService.multiplyXYStepSize();
        jogService.multiplyZStepSize();
        jogService.multiplyABCStepSize();
    }

    @Override
    public void onDecreaseStepSize() {
        jogService.divideXYStepSize();
        jogService.divideZStepSize();
        jogService.divideABCStepSize();
    }
}
