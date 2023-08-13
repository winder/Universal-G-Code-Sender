/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.GUIBackend;

import javax.swing.*;
import java.awt.*;

/**
 * A small program for testing the UI
 */
public class AutoLevelerTestMain extends JFrame {

    public static void main(String[] args) throws Exception {
        CentralLookup.getDefault().add(new GUIBackend());

        AutoLevelerTestMain autoLevelerTestMain = new AutoLevelerTestMain();
        autoLevelerTestMain.start();

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Visualizer test");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private void start() throws Exception {
        setPreferredSize(new Dimension(1024, 768));

        AutoLevelerTopComponent topComponent = new AutoLevelerTopComponent();
        add(topComponent);
        topComponent.componentOpened();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setVisible(true);
    }
}
