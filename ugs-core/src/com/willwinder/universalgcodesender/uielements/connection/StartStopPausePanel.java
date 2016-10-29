/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.connection;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.actions.Pause;
import com.willwinder.universalgcodesender.uielements.actions.Start;
import com.willwinder.universalgcodesender.uielements.actions.Stop;
import java.awt.Color;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class StartStopPausePanel extends JPanel {
    final BackendAPI backend;

    JButton start = new JButton();
    JButton stop = new JButton();
    JButton pause = new JButton();

    // Icons: http://www.customicondesign.com/free-icons/flatastic-icon-set/
    String SMALL = "16x16";
    String LARGE = "24x24";
    String START_RESOURCE = "/resources/icons/%s-start.png";
    String STOP_RESOURCE = "/resources/icons/%s-stop.png";
    String PAUSE_RESOURCE = "/resources/icons/%s-pause.png";

    private final JPopupMenu menuPopup = new JPopupMenu();
    private final JRadioButtonMenuItem small = new JRadioButtonMenuItem(
            Localization.getString("toolbar.icon.small"));
    private final JRadioButtonMenuItem large = new JRadioButtonMenuItem(
            Localization.getString("toolbar.icon.large"));


    public StartStopPausePanel(BackendAPI backend) {
        this.backend = backend;

        // popup menu
        ButtonGroup group = new ButtonGroup();

        boolean isLarge = backend.getSettings().getToolbarIconSize() == 1;

        small.setSelected(!isLarge);
        group.add(small);
        small.addActionListener(a -> setIcons());
        menuPopup.add(small);

        large.setSelected(isLarge);
        group.add(large);
        large.addActionListener(a -> setIcons());
        menuPopup.add(large);

        this.setComponentPopupMenu(menuPopup);

        setActions();
        setIcons();
        initComponents();
    }

    private final void setActions() {
        start.setAction(new Start(backend));
        stop.setAction(new Stop(backend));
        pause.setAction(new Pause(backend));
    }

    private final void setIcons() {
        start.setBorderPainted(false);
        stop.setBorderPainted(false);
        pause.setBorderPainted(false);

        String size = small.isSelected() ? this.SMALL : this.LARGE;
        backend.getSettings().setToolbarIconSize(small.isSelected() ? 0 : 1);

        start.setIcon(new ImageIcon(getClass().getResource(
                String.format(START_RESOURCE, size))));
        stop.setIcon(new ImageIcon(getClass().getResource(
                String.format(STOP_RESOURCE, size))));
        pause.setIcon(new ImageIcon(getClass().getResource(
                String.format(PAUSE_RESOURCE, size))));

        this.revalidate();
    }

    private final void initComponents() {
        setLayout(new MigLayout("wrap 3, inset 0"));

        add(start);
        add(stop);
        add(pause);
    }
}
