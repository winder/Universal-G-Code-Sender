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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;

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

    public static final String ICON_BASE = "resources/icons/pendant.svg";

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

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fill, inset 0"));

        Optional<PendantURLBean> first = results.stream().findFirst();
        if (first.isPresent()) {
            panel.add(new QRPanel(first.get()), "grow, al center, wrap");
            JLabel link = createLinkLabel(first.get());
            panel.add(link, "al center, gaptop 10");
        } else {
            panel.add(new JLabel("No network interface detected"), "al center, gap 10");
        }

        Window parent = SwingUtilities.getWindowAncestor((Component) e.getSource());
        JOptionPane.showMessageDialog(parent, panel, "Web pendant", JOptionPane.PLAIN_MESSAGE);
    }

    private static JLabel createLinkLabel(PendantURLBean pendantURLBean) {
        String urlPattern = "<html><a href=\"%s\">%s</a></html>";
        JLabel link = new JLabel(String.format(urlPattern, pendantURLBean.getUrlString(), pendantURLBean.getUrlString()));
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(pendantURLBean.getUrlString()));
                } catch (Exception ex) {
                    // Never mind
                }
            }
        });
        return link;
    }
}
