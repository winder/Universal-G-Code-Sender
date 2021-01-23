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

import com.willwinder.ugs.nbp.core.services.PendantService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author wwinder
 */
@ActionID(
        category = LocalizingService.PendantCategory,
        id = LocalizingService.PendantActionId)
@ActionRegistration(
        //iconBase = PauseAction.ICON_BASE,
        displayName = "Pendant", //resources.MessagesBundle#" + LocalizingService.PendantTitleKey,
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

    private final BackendAPI backend;

    public PendantAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

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
        panel.setLayout(new MigLayout("fill, wrap 1"));
        String urlPattern = "<HTML>URL: <a href=\"%s\">%s</a></html>";
        for (PendantURLBean result : results) {
            panel.add(new JLabel(String.format(urlPattern, result.getUrlString(), result.getUrlString())),
                    "al center");
            panel.add(new JLabel(
                    "",
                    new ImageIcon(result.getQrCodeJpg(), "QR Code"),
                    JLabel.CENTER),
                    "al center");

            JOptionPane.showMessageDialog(null,panel,"Pendant Address",JOptionPane.PLAIN_MESSAGE);

            return;
        }
    }
}
