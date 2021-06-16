package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Size;
import com.willwinder.universalgcodesender.Utils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.text.ParseException;

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
