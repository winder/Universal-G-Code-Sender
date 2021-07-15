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
import com.willwinder.ugs.nbp.designer.model.Size;
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
    private JTextField stockHeight;
    private JTextField stockWidth;

    public StockSettingsPanel(Controller controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[20%][80%]"));

        add(new JLabel("Stock size"), "span, wrap");
        Size stockSize = controller.getSettings().getStockSize();
        add(new JLabel("Width"));
        stockWidth = new JTextField(Utils.formatter.format(stockSize.getWidth()));
        add(stockWidth, "grow, wrap");

        add(new JLabel("Height"));
        stockHeight = new JTextField(Utils.formatter.format(stockSize.getHeight()));
        add(stockHeight, "grow, wrap");

        add(new JLabel("Thickness"));
        stockThickness = new JTextField(Utils.formatter.format(controller.getSettings().getStockThickness()));
        add(stockThickness, "grow, wrap");
    }

    public Size getStockSize() {
        try {
            return new Size(Utils.formatter.parse(stockWidth.getText()).doubleValue(), Utils.formatter.parse(stockHeight.getText()).doubleValue());
        } catch (ParseException e) {
            return controller.getSettings().getStockSize();
        }
    }

    public double getStockThickness() {
        try {
            return Utils.formatter.parse(stockThickness.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getStockThickness();
        }
    }
}
