/*
    Copyright 2017 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.core.panels.QRPanel;
import com.willwinder.ugs.nbp.core.services.PendantService;
import com.willwinder.ugs.nbp.core.ui.PendantUrlRenderer;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import net.miginfocom.swing.MigLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.net.URI;
import java.util.Collection;

/**
 * @author wwinder
 */
@ActionID(
        category = LocalizingService.PendantCategory,
        id = LocalizingService.PendantActionId)
@ActionRegistration(
        displayName = "resources/MessagesBundle#" + LocalizingService.PendantTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Pendant",
                position = 980),
        @ActionReference(
                path = LocalizingService.PendantWindowPath,
                position = 1025)})
public class PendantAction extends AbstractAction {

    private static final String ICON_BASE = "resources/icons/pendant.svg";

    public PendantAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.PendantTitle);
        putValue(NAME, LocalizingService.PendantTitle);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PendantService pendantService = Lookup.getDefault().lookup(PendantService.class);
        Collection<PendantURLBean> results = pendantService.startPendant();
        PendantURLBean firstUrl = results.stream().findFirst().orElse(null);

        JPanel contentPanel = new JPanel(new MigLayout("fill, inset 0"));
        JPanel mainPanel = new JPanel(new MigLayout("fill, inset 0"));

        JComboBox<PendantURLBean> comboBox = new JComboBox<>(results.toArray(PendantURLBean[]::new));
        comboBox.setRenderer(new PendantUrlRenderer());


        comboBox.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                setSelectedUrl(contentPanel, (PendantURLBean) ev.getItem());
            }
        });

        comboBox.setSelectedItem(firstUrl);
        setSelectedUrl(contentPanel, firstUrl);

        mainPanel.add(contentPanel, "grow, wrap");
        mainPanel.add(comboBox, "growx, wrap");

        JButton openBrowser = new JButton("Open browser");
        mainPanel.add(openBrowser, "growx");

        Window parent = SwingUtilities.getWindowAncestor((Component) e.getSource());
        JDialog dialog = createDialog(parent, mainPanel, "Web pendant");

        openBrowser.addActionListener(ev -> openBrowserAndClose(dialog, (PendantURLBean) comboBox.getSelectedItem()));

        if (results.isEmpty()) {
            comboBox.setVisible(false);
            openBrowser.setVisible(false);
        }

        dialog.setVisible(true);
    }

    private static JDialog createDialog(Window parent, JPanel panel, String title) {
        JDialog dialog = new JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new MigLayout("fill, inset 10"));
        dialog.add(panel, "grow");
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        return dialog;
    }

    private static void openBrowserAndClose(JDialog dialog, PendantURLBean urlBean) {
        if (urlBean != null) {
            try {
                Desktop.getDesktop().browse(new URI(urlBean.getUrlString()));
            } catch (Exception ignore) {
                // Never mind
            }
        }
        dialog.dispose();
    }

    private static void setSelectedUrl(JPanel content, PendantURLBean url) {
        content.removeAll();
        if (url != null) {
            content.add(new QRPanel(url), "grow, wrap");
        } else {
            content.add(new JLabel("No network interface detected"), "al center");
        }
        content.revalidate();
        content.repaint();
    }
}
