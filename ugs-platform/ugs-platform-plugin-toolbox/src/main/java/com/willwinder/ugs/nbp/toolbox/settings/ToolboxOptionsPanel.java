/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.toolbox.settings;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A options panel for modifying which actions that should be available
 *
 * @author Joacim Breiler
 */
public class ToolboxOptionsPanel extends AbstractOptionsPanel {

    private final ToolboxOptionsController controller;
    private DefaultListModel<ActionReference> availableActionsListModel = new DefaultListModel<>();
    private DefaultListModel<ActionReference> selectedActionsListModel = new DefaultListModel<>();
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JList<ActionReference> selectedActionsList;
    private JList<ActionReference> availableActionsList;

    ToolboxOptionsPanel(ToolboxOptionsController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void clear() {
        removeAll();
    }

    @Override
    public void load() {
        setLayout(new BorderLayout());
        removeAll();
        initComponents();
        initListeners();
    }

    private void initListeners() {
        selectedActionsList.addMouseListener(new DoubleClickDelegator((actionReference) -> {
            selectedActionsListModel.removeElement(actionReference);
            changer.changed();
        }));

        selectedActionsList.addListSelectionListener(e -> {
            boolean hasSelection = e.getFirstIndex() != -1;
            moveUpButton.setEnabled(hasSelection);
            moveDownButton.setEnabled(hasSelection);
        });

        availableActionsList.addMouseListener(new DoubleClickDelegator((actionReference) -> {
            selectedActionsListModel.addElement(actionReference);
            changer.changed();
        }));

        moveUpButton.addActionListener(e -> {
            int selectedIndex = selectedActionsList.getSelectedIndex();
            swap(selectedIndex, selectedIndex - 1);
            selectedActionsList.setSelectedIndex(selectedIndex - 1);
            selectedActionsList.ensureIndexIsVisible(selectedIndex - 1);
            changer.changed();
        });

        moveDownButton.addActionListener(e -> {
            int selectedIndex = selectedActionsListModel.indexOf(selectedActionsList.getSelectedValue());
            swap(selectedIndex, selectedIndex + 1);
            selectedActionsList.setSelectedIndex(selectedIndex + 1);
            selectedActionsList.ensureIndexIsVisible(selectedIndex + 1);
            changer.changed();
        });
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "fill", "[24px][fill]"));
        selectedActionsListModel.clear();
        availableActionsListModel.clear();

        panel.add(new JLabel("<html>Selected actions:<br/>(Double click to remove)</html>"));
        panel.add(new JLabel("<html>Available actions:<br/>(Double click to add)</html>"), "wrap");

        selectedActionsList = new JList<>(selectedActionsListModel);
        selectedActionsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        selectedActionsList.setCellRenderer(new ActionReferenceRenderer());

        JScrollPane jScrollPane = new JScrollPane(selectedActionsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setPreferredSize(new Dimension(200, 340));
        panel.add(jScrollPane, "grow");

        Settings.getActions().forEach(actionId -> controller.getActionRegistrationService().getActionById(actionId)
                .ifPresent(actionReference -> selectedActionsListModel.addElement(actionReference)));

        availableActionsList = new JList<>(availableActionsListModel);
        availableActionsList.setCellRenderer(new ActionReferenceRenderer());
        jScrollPane = new JScrollPane(availableActionsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setPreferredSize(new Dimension(200, 340));
        panel.add(jScrollPane, "grow, wrap");

        JPanel buttons = new JPanel();
        moveUpButton = new JButton("Move up", ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/toolbox/up.svg", false));
        moveUpButton.setEnabled(false);

        buttons.add(moveUpButton);
        moveDownButton = new JButton("Move down", ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/toolbox/down.svg", false));
        moveDownButton.setEnabled(false);

        buttons.add(moveDownButton);
        panel.add(buttons);

        controller.getActionRegistrationService().getCategories()
                .forEach(category -> {
                    CategoryActionReference seperatorElement = new CategoryActionReference();
                    seperatorElement.setId(category);
                    seperatorElement.setCategoryName(category);
                    availableActionsListModel.addElement(seperatorElement);
                    controller.getActionRegistrationService().getActionsByCategory(category).forEach(availableActionsListModel::addElement);
                });

        add(panel, BorderLayout.CENTER);
    }

    /**
     * Swap two elements in the list.
     *
     * @param index1 the index of one of the elements to swap
     * @param index2 the index of one of the elements to swap
     */
    private void swap(int index1, int index2) {
        ActionReference object1 = selectedActionsListModel.getElementAt(index1);
        ActionReference object2 = selectedActionsListModel.getElementAt(index2);
        selectedActionsListModel.set(index1, object2);
        selectedActionsListModel.set(index2, object1);
    }

    @Override
    public void store() {
        List<String> actionIdList = Collections.list(selectedActionsListModel.elements()).stream()
                .map(ActionReference::getId)
                .collect(Collectors.toList());

        Settings.setActions(actionIdList);
    }

    @Override
    public boolean valid() {
        return true;
    }
}
