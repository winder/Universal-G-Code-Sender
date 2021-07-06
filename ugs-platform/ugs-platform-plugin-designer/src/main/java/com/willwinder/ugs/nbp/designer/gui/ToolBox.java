package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.Size;
import com.willwinder.universalgcodesender.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.*;

import static org.openide.NotifyDescriptor.OK_OPTION;

public class ToolBox extends JToolBar {

    public ToolBox(Controller controller) {
        super("Tools");
        setFloatable(false);

        JToggleButton select = new JToggleButton(new ToolSelectAction(controller));
        select.setText("");
        select.setToolTipText("Select and move shapes");
        add(select);

        JToggleButton rectangle = new JToggleButton(new ToolDrawRectangleAction(controller));
        rectangle.setText("");
        rectangle.setToolTipText("Draw squares and rectangles");
        add(rectangle);

        JToggleButton circle = new JToggleButton(new ToolDrawCircleAction(controller));
        circle.setText("");
        circle.setToolTipText("Draw circles and ellipses");
        add(circle);


        JToggleButton insert = new JToggleButton(new ToolInsertAction(controller));
        insert.setText("");
        insert.setToolTipText("Inserts a drawing");
        insert.setContentAreaFilled(false);
        add(insert);


        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem menuItemCreateSpringProject = new JMenuItem("Spring Project");
        popupMenu.add(menuItemCreateSpringProject);

        JMenuItem menuItemCreateHibernateProject = new JMenuItem("Hibernate Project");
        popupMenu.add(menuItemCreateHibernateProject);

        JMenuItem menuItemCreateStrutsProject = new JMenuItem("Struts Project");
        popupMenu.add(menuItemCreateStrutsProject);


        add(Box.createRigidArea(new Dimension(10, 10)));
        add(Box.createHorizontalGlue());

        JSlider zoomSlider = new JSlider(1, 1000, 100);
        zoomSlider.addChangeListener(event -> {
            double scale = ((double) zoomSlider.getValue()) / 100d;
            controller.getDrawing().setScale(scale);
        });
        zoomSlider.setValue((int) (controller.getDrawing().getScale() * 100));

        zoomSlider.setMaximumSize(new Dimension(100, 32));
        add(zoomSlider);

        add(Box.createHorizontalStrut(6));
        PanelButton toolButton = new PanelButton("Tool", controller.getSettings().getToolDescription());
        toolButton.addActionListener(e -> {
            ToolSettingsPanel toolSettingsPanel = new ToolSettingsPanel(controller);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(toolSettingsPanel, "Tool setings", true, null);
            if (DialogDisplayer.getDefault().notify(dialogDescriptor) == OK_OPTION) {
                ChangeToolSettingsAction changeStockSettings = new ChangeToolSettingsAction(controller, toolSettingsPanel.getToolDiameter(), toolSettingsPanel.getFeedSpeed(), toolSettingsPanel.getPlungeSpeed(), toolSettingsPanel.getDepthPerPass(), toolSettingsPanel.getStepOver());
                changeStockSettings.actionPerformed(null);
                controller.getUndoManager().addAction(changeStockSettings);
                toolButton.setText(controller.getSettings().getToolDescription());
            }
        });
        add(toolButton);

        add(Box.createHorizontalStrut(6));
        PanelButton stockButton = new PanelButton("Stock size", controller.getSettings().getStockSizeDescription());
        stockButton.addActionListener(e -> {
            StockSettingsPanel stockSettingsPanel = new StockSettingsPanel(controller);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(stockSettingsPanel, "Stock size", true, null);
            if (DialogDisplayer.getDefault().notify(dialogDescriptor) == OK_OPTION) {
                Size stockSize = stockSettingsPanel.getStockSize();
                double stockThickness = stockSettingsPanel.getStockThickness();
                ChangeStockSettingsAction changeStockSettingsAction = new ChangeStockSettingsAction(controller, stockSize.getWidth(), stockSize.getHeight(), stockThickness);
                changeStockSettingsAction.actionPerformed(null);
                controller.getUndoManager().addAction(changeStockSettingsAction);
                stockButton.setText(controller.getSettings().getStockSizeDescription());
            }
        });
        add(stockButton);
        controller.getSettings().addListener(() -> {
            Size stockSize = controller.getSettings().getStockSize();
            double thickness = controller.getSettings().getStockThickness();
            stockButton.setText(Utils.formatter.format(stockSize.getWidth()) + " x " + Utils.formatter.format(stockSize.getHeight()) + " x " + Utils.formatter.format(thickness));
        });


        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(circle);
        buttons.add(rectangle);
        buttons.add(insert);

        controller.addListener(event -> {
            if (event == ControllerEventType.TOOL_SELECTED) {
                buttons.clearSelection();
                controller.getSelectionManager().clearSelection();
                switch (controller.getTool()) {
                    case SELECT:
                        select.setSelected(true);
                        break;
                    case RECTANGLE:
                        rectangle.setSelected(true);
                        break;
                    case CIRCLE:
                        circle.setSelected(true);
                        break;
                    case INSERT:
                        insert.setSelected(true);
                        break;
                }
                repaint();
            }
        });
    }
}