package com.willwinder.ugs.nbp.core.toolbars;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.toolbars.ConnectionBaudRatePanel;
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
        category = LocalizingService.ConnectionBaudRateToolbarCategory,
        id = LocalizingService.ConnectionBaudRateToolbarActionId
)
@ActionRegistration(
        iconBase = ConnectionBaudRateToolbar.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.ConnectionBaudRateToolbarTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Connection",
                position = 995)})
public class ConnectionBaudRateToolbar extends AbstractAction implements Presenter.Toolbar {
    public static final String ICON_BASE = "resources/icons/baudrate.png";

    public ConnectionBaudRateToolbar() {
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, LocalizingService.ConnectionBaudRateToolbarTitle);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Component getToolbarPresenter() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        return new ConnectionBaudRatePanel(backend);
    }
}
