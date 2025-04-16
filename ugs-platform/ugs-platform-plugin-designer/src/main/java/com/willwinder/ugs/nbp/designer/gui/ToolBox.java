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

import com.willwinder.ugs.nbp.core.ui.ToolBar;
import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.ImageUtilities;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;

/**
 * @author Joacim Breiler
 */
public class ToolBox extends ToolBar {

    private JToggleButton toolDropDownButton = null;
    private JToggleButton snapDropDownButton = null;
    private JToggleButton jogDropDownButton = null;
    private JToggleButton alignDropDownButton = null;

    public ToolBox(Controller controller) {
        setFloatable(false);

        JToggleButton select = new JToggleButton(new ToolSelectAction());
        select.setSelected(true);
        select.setText("");
        select.setToolTipText("Select and move shapes");
        add(select);

        add(createToolDropDownButton());

        addSeparator();

        JButton flipHorizontal = new JButton(new FlipHorizontallyAction());
        flipHorizontal.setText("");
        flipHorizontal.setToolTipText("Flips horizontally");
        flipHorizontal.setBorderPainted(false);
        add(flipHorizontal);

        JButton flipVertical = new JButton(new FlipVerticallyAction());
        flipVertical.setText("");
        flipVertical.setToolTipText("Flips vertically");
        flipVertical.setBorderPainted(false);
        add(flipVertical);

        addSeparator();

        add(createAlignDropDownButton());

        addSeparator();

        JButton union = new JButton(new UnionAction());
        union.setText("");
        union.setBorderPainted(false);
        add(union);

        JButton subtract = new JButton(new SubtractAction());
        subtract.setText("");
        subtract.setBorderPainted(false);
        add(subtract);

        JButton intersection = new JButton(new IntersectionAction());
        intersection.setText("");
        intersection.setBorderPainted(false);
        add(intersection);

        JButton breakApart = new JButton(new BreakApartAction());
        breakApart.setText("");
        breakApart.setBorderPainted(false);
        add(breakApart);

        addSeparator();

        JButton visible = new JButton(new ToggleHidden());
        visible.setToolTipText("Toggles if the object should be hidden");
        visible.setBorderPainted(false);
        visible.setHideActionText(true);
        add(visible);

        JButton multiply = new JButton(new MultiplyAction());
        multiply.setText("");
        multiply.setToolTipText("Multiplies the selection");
        multiply.setBorderPainted(false);
        add(multiply);

        add(createJogDropDownButton());
        addSeparator();

        JToggleButton zoom = new JToggleButton(new ToolZoomAction());
        zoom.setText("");
        zoom.setToolTipText("Controls zoom");
        zoom.setBorderPainted(false);
        add(zoom);

        ButtonGroup buttons = new ButtonGroup();
        buttons.add(select);
        buttons.add(toolDropDownButton);
        buttons.add(zoom);
        
        add(createSnapDropdownButton());
        
        controller.addListener(event -> {
            if (event == ControllerEventType.TOOL_SELECTED) {
                buttons.clearSelection();
                controller.getSelectionManager().clearSelection();
                switch (controller.getTool()) {
                    case SELECT:
                        select.setSelected(true);
                        break;
                    case POINT:
                    case RECTANGLE:
                    case CIRCLE:
                    case TEXT:
                        toolDropDownButton.setSelected(true);
                        break;
                    case ZOOM:
                        zoom.setSelected(true);
                        break;
                    default:
                        toolDropDownButton.setSelected(false);
                }
                repaint();
            }
        });
    }

    private JToggleButton createAlignDropDownButton() {
        ActionListener toolMenuListener = e -> {
            if (alignDropDownButton == null) {
                return;
            }

            JMenuItem source = (JMenuItem) e.getSource();
            alignDropDownButton.setIcon((Icon) source.getAction().getValue(Action.LARGE_ICON_KEY));
            alignDropDownButton.setAction(source.getAction());
        };

        AlignCenterAction alignCenterAction = new AlignCenterAction();
        JPopupMenu popupMenu = new JPopupMenu();
        addDropDownAction(popupMenu, new AlignLeftAction(), toolMenuListener);
        addDropDownAction(popupMenu, alignCenterAction, toolMenuListener);
        addDropDownAction(popupMenu, new AlignRightAction(), toolMenuListener);
        addDropDownAction(popupMenu, new AlignTopAction(), toolMenuListener);
        addDropDownAction(popupMenu, new AlignMiddleAction(), toolMenuListener);
        addDropDownAction(popupMenu, new AlignBottomAction(), toolMenuListener);
        alignDropDownButton = DropDownButtonFactory.createDropDownToggleButton(ImageUtilities.loadImageIcon(AlignCenterAction.LARGE_ICON_PATH, false), popupMenu);
        alignDropDownButton.setAction(alignCenterAction);
        alignDropDownButton.setSelected(false);
        return alignDropDownButton;
    }

    private JToggleButton createJogDropDownButton() {
        ActionListener toolMenuListener = e -> {
            if (jogDropDownButton == null) {
                return;
            }

            JMenuItem source = (JMenuItem) e.getSource();
            jogDropDownButton.setIcon((Icon) source.getAction().getValue(Action.LARGE_ICON_KEY));
            jogDropDownButton.setSelected(true);
            jogDropDownButton.setAction(source.getAction());
        };

        JogMachineToCenterAction toolDrawRectangleAction = new JogMachineToCenterAction();
        JPopupMenu popupMenu = new JPopupMenu();
        addDropDownAction(popupMenu, toolDrawRectangleAction, toolMenuListener);
        addDropDownAction(popupMenu, new JogMachineToTopLeftCornerAction(), toolMenuListener);
        addDropDownAction(popupMenu, new JogMachineToTopRightCornerAction(), toolMenuListener);
        addDropDownAction(popupMenu, new JogMachineToLowerLeftCornerAction(), toolMenuListener);
        addDropDownAction(popupMenu, new JogMachineToLowerRightCornerAction(), toolMenuListener);
        addDropDownAction(popupMenu, new JogMachineToNextAction(), toolMenuListener);
        addDropDownAction(popupMenu, new JogMachineToPreviousAction(), toolMenuListener);
        jogDropDownButton = DropDownButtonFactory.createDropDownToggleButton(ImageUtilities.loadImageIcon(JogMachineToCenterAction.LARGE_ICON_PATH, false), popupMenu);
        jogDropDownButton.setAction(toolDrawRectangleAction);
        return jogDropDownButton;
    }

    private JToggleButton createToolDropDownButton() {
        // An action listener that listens to the popup menu items and changes the current action
        ActionListener toolMenuListener = e -> {
            if (toolDropDownButton == null) {
                return;
            }

            JMenuItem source = (JMenuItem) e.getSource();
            toolDropDownButton.setIcon((Icon) source.getAction().getValue(Action.LARGE_ICON_KEY));
            toolDropDownButton.setSelected(true);
            toolDropDownButton.setAction(source.getAction());
        };

        ToolDrawRectangleAction toolDrawRectangleAction = new ToolDrawRectangleAction();
        JPopupMenu popupMenu = new JPopupMenu();
        addDropDownAction(popupMenu, toolDrawRectangleAction, toolMenuListener);
        addDropDownAction(popupMenu, new ToolDrawCircleAction(), toolMenuListener);
        addDropDownAction(popupMenu, new ToolDrawPointAction(), toolMenuListener);
        addDropDownAction(popupMenu, new ToolDrawTextAction(), toolMenuListener);
        popupMenu.addSeparator();
        addDropDownAction(popupMenu, new ToolImportAction(), null);
        addDropDownAction(popupMenu, new ToolClipartAction(), null);
        addDropDownAction(popupMenu, new TraceImageAction(), null);
        toolDropDownButton = DropDownButtonFactory.createDropDownToggleButton(ImageUtilities.loadImageIcon(ToolDrawRectangleAction.LARGE_ICON_PATH, false), popupMenu);
        toolDropDownButton.setAction(toolDrawRectangleAction);
        return toolDropDownButton;
    }
    
    private JToggleButton createSnapDropdownButton() {
        // An action listener that listens to the popup menu items and changes the current action
        ActionListener toolMenuListener = e -> {
            if (snapDropDownButton == null) {
                return;
            }

            JMenuItem source = (JMenuItem) e.getSource();
            snapDropDownButton.setIcon((Icon) source.getAction().getValue(Action.LARGE_ICON_KEY));
            snapDropDownButton.setSelected(true);
            snapDropDownButton.setAction(source.getAction());
        };

        SnapToGridOneAction snapToOneAction = new SnapToGridOneAction();
        JPopupMenu popupMenu = new JPopupMenu();
        addDropDownAction(popupMenu, new SnapToGridNoneAction(), toolMenuListener);
        addDropDownAction(popupMenu, new SnapToGridHalfAction(), toolMenuListener);
        addDropDownAction(popupMenu, snapToOneAction, toolMenuListener);
        addDropDownAction(popupMenu, new SnapToGridTwoAction(), toolMenuListener);
        addDropDownAction(popupMenu, new SnapToGridFiveAction(), toolMenuListener);
        addDropDownAction(popupMenu, new SnapToGridTenAction(), toolMenuListener);        
        
        snapDropDownButton = DropDownButtonFactory.createDropDownToggleButton(ImageUtilities.loadImageIcon(SnapToGridOneAction.LARGE_ICON_PATH, false), popupMenu);
        snapDropDownButton.setAction(snapToOneAction);
        return snapDropDownButton;
    }
    private void addDropDownAction(JPopupMenu popupMenu, Action action, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(action);
        if (actionListener != null) {
            menuItem.addActionListener(actionListener);
        }
        popupMenu.add(menuItem);
    }
}
