package com.willwinder.ugs.nbp.core.toolbars;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.toolbars.ConnectionSerialPortPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@ActionID(
        category = "Machine",
        id = "com.willwinder.ugs.nbp.core.toolbars.ConnectionSerialPortToolbar"
)
@ActionRegistration(
        iconBase = ConnectionSerialPortToolbar.ICON_BASE,
        displayName = "#" + ConnectionSerialPortToolbar.TITLE_LOCALIZATION_KEY,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Connection",
                position = 990)})
public class ConnectionSerialPortToolbar extends AbstractAction implements Presenter.Toolbar {
    public static final String ICON_BASE = "resources/icons/serialport.png";
    public static final String TITLE_LOCALIZATION_KEY = "mainWindow.swing.serialport.toolbarTitle";

    public ConnectionSerialPortToolbar() {
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, Localization.getString(TITLE_LOCALIZATION_KEY));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Component getToolbarPresenter() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        return new ConnectionSerialPortPanel(backend);
    }
}
