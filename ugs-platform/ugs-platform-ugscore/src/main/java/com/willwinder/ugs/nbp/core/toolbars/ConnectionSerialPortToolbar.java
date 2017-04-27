package com.willwinder.ugs.nbp.core.toolbars;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
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
        category = LocalizingService.ConnectionSerialPortToolbarCategory,
        id = LocalizingService.ConnectionSerialPortToolbarActionId
)
@ActionRegistration(
        iconBase = ConnectionSerialPortToolbar.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.ConnectionSerialPortToolbarTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Connection",
                position = 990)})
public class ConnectionSerialPortToolbar extends AbstractAction implements Presenter.Toolbar {
    public static final String ICON_BASE = "resources/icons/serialport.png";

    public ConnectionSerialPortToolbar() {
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, LocalizingService.ConnectionSerialPortToolbarTitle);
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
