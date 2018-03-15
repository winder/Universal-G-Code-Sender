package com.willwinder.ugs.nbp.jog.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import org.openide.util.lookup.ServiceProvider;

@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.nbp.jog.actions.UseSeparateStepSizeAction")
@ActionRegistration(
        displayName = "resources.MessagesBundle#platform.plugin.jog.useSeparateStepSize",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE_JOG_STEP_SIZE,
                position = 10000,
                separatorAfter = 10001)
})
@ServiceProvider(service = UseSeparateStepSizeAction.class)
public class UseSeparateStepSizeAction extends AbstractAction implements Presenter.Menu {

    private final BackendAPI backend;
    private final JCheckBoxMenuItem menuItem;

    public UseSeparateStepSizeAction() {
        String title = Localization.getString("platform.plugin.jog.useSeparateStepSize");
        putValue(NAME, title);

        menuItem = new JCheckBoxMenuItem(title);
        menuItem.setAction(this);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onBackendEvent);
    }

    private void onBackendEvent(UGSEvent event) {
        if (event.isSettingChangeEvent()) {
            menuItem.setSelected(backend.getSettings().useZStepSize());
        }

        EventQueue.invokeLater(() -> setEnabled(isEnabled()));
    }

    @Override
    public boolean isEnabled() {
        return backend != null && backend.isConnected();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return menuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Toggle the usage separate Z step size
        backend.getSettings().setUseZStepSize(!backend.getSettings().useZStepSize());
    }
}
