/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.Utils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.text.ParseException;

/**
 * @author Joacim Breiler
 */
public class StockSettingsPanel extends JPanel {
    private final Controller controller;
    private JTextField stockThickness;


    public StockSettingsPanel(Controller controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[20%][80%]"));

        add(new JLabel("Thickness"));
        stockThickness = new TextFieldWithUnit(Unit.MM, 2, controller.getSettings().getStockThickness());
        add(stockThickness, "grow, wrap");
    }


    public double getStockThickness() {
        try {
            return Utils.formatter.parse(stockThickness.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getStockThickness();
        }
    }
}
