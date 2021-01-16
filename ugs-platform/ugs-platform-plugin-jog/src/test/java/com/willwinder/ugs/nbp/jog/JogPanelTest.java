/*
    Copyright 2018-2021 Will Winder

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

import javax.swing.*;

public class JogPanelTest extends JFrame {
    private JogPanel jogPanel;

    public static void main(String[] args) throws Exception {
        JogPanelTest jogPanelTest = new JogPanelTest();
        jogPanelTest.start();
    }

    private void start() throws Exception {
        jogPanel = new JogPanel();
        getContentPane().add(jogPanel);

        createMenuBar();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);

        jogPanel.setFeedRate(1000);
        jogPanel.setStepSizeXY(100);
        jogPanel.setStepSizeZ(0.01);

        setMinimumSize(jogPanel.getMinimumSize());
    }

    private void createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem("Enabled");
        menuItem.addActionListener(e -> jogPanel.setEnabled(true));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Disabled");
        menuItem.addActionListener(e -> jogPanel.setEnabled(false));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Use Z step size");
        menuItem.addActionListener(e -> jogPanel.enabledStepSizes(true, false));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Use ABC step size");
        menuItem.addActionListener(e -> jogPanel.enabledStepSizes(false, true));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Use Z and ABC step size");
        menuItem.addActionListener(e -> jogPanel.enabledStepSizes(true, true));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Don't use Z or ABC step size");
        menuItem.addActionListener(e -> jogPanel.enabledStepSizes(false, false));
        fileMenu.add(menuItem);

        setJMenuBar(menuBar);
    }
}
