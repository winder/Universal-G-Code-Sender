/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.logic.ToolLibraryListener;
import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Master-detail manager dialog for the Tool Library. Left list shows all tools; right side edits
 * the selected entry. Add/Duplicate/Delete/Revert operate on the selection. Changes are debounce-
 * persisted by the service.
 */
public class ToolLibraryDialog extends JDialog {
    private final ToolLibraryService service;
    private final UnitUtils.Units preferredUnits;
    private final DefaultListModel<ToolDefinition> listModel = new DefaultListModel<>();
    private JList<ToolDefinition> toolList;
    private ToolEditorPanel editorPanel;
    private JButton addButton;
    private JButton duplicateButton;
    private JButton deleteButton;
    private JButton revertButton;
    private JButton closeButton;
    private final ToolLibraryListener libraryListener = this::onLibraryChangedExternally;

    public ToolLibraryDialog(Window owner, ToolLibraryService service, UnitUtils.Units preferredUnits) {
        super(owner, "Tool Library", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.preferredUnits = preferredUnits;
        initComponents();
        refreshList(null);
        service.addListener(libraryListener);
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[250!][grow]", "[grow][]"));
        setPreferredSize(new Dimension(720, 520));

        toolList = new JList<>(listModel);
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ToolDefinition t) {
                    EndmillShape shape = t.getShape() == null ? EndmillShape.CUSTOM : t.getShape();
                    setIcon(new ToolShapeIcon(shape, 16));
                    setText(t.getName() == null ? t.getId() : t.getName());
                }
                return this;
            }
        });
        toolList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectionChanged();
            }
        });

        JPanel leftPanel = new JPanel(new MigLayout("fill, insets 4", "[grow]", "[grow][]"));
        leftPanel.add(new JScrollPane(toolList), "grow, wrap");

        JPanel listButtons = new JPanel(new MigLayout("insets 0, fillx", "[grow][grow][grow][grow]"));
        addButton = new JButton("Add");
        addButton.addActionListener(this::onAdd);
        duplicateButton = new JButton("Duplicate");
        duplicateButton.addActionListener(this::onDuplicate);
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this::onDelete);
        revertButton = new JButton("Revert");
        revertButton.addActionListener(this::onRevert);
        listButtons.add(addButton, "growx");
        listButtons.add(duplicateButton, "growx");
        listButtons.add(deleteButton, "growx");
        listButtons.add(revertButton, "growx");
        leftPanel.add(listButtons, "growx");

        editorPanel = new ToolEditorPanel(preferredUnits);
        editorPanel.setChangeListener(this::onEditorChanged);

        add(leftPanel, "grow");
        add(new JScrollPane(editorPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "grow, wrap");

        JPanel bottom = new JPanel(new BorderLayout());
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel closeWrapper = new JPanel(new MigLayout("insets 4, align right"));
        closeWrapper.add(closeButton);
        bottom.add(closeWrapper, BorderLayout.EAST);
        add(bottom, "spanx 2, growx");

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void refreshList(String preferredSelectionId) {
        List<ToolDefinition> tools = service.getTools();
        listModel.clear();
        tools.forEach(listModel::addElement);
        if (listModel.isEmpty()) {
            editorPanel.setTool(null, true);
            updateButtonState();
            return;
        }
        int selectIndex = 0;
        if (preferredSelectionId != null) {
            for (int i = 0; i < listModel.size(); i++) {
                if (preferredSelectionId.equals(listModel.get(i).getId())) {
                    selectIndex = i;
                    break;
                }
            }
        }
        toolList.setSelectedIndex(selectIndex);
    }

    private void onSelectionChanged() {
        ToolDefinition selected = toolList.getSelectedValue();
        editorPanel.setTool(selected, false);
        updateButtonState();
    }

    private void updateButtonState() {
        ToolDefinition selected = toolList.getSelectedValue();
        boolean hasSelection = selected != null;
        boolean isBuiltIn = hasSelection && selected.isBuiltIn();
        boolean isCustom = hasSelection && selected.isCustomSentinel();
        duplicateButton.setEnabled(hasSelection && !isCustom);
        deleteButton.setEnabled(hasSelection && !isBuiltIn);
        revertButton.setEnabled(isBuiltIn && !isCustom);
    }

    private void onEditorChanged(ToolDefinition edited) {
        if (edited == null) return;
        try {
            service.updateTool(edited);
            int index = toolList.getSelectedIndex();
            if (index >= 0) {
                listModel.set(index, edited);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tool Library", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onAdd(ActionEvent e) {
        ToolDefinition newTool = new ToolDefinition();
        newTool.setName("New tool");
        newTool.setShape(EndmillShape.UPCUT);
        newTool.setDiameter(3.0);
        newTool.setDiameterUnit(UnitUtils.Units.MM);
        newTool.setFeedSpeed(900);
        newTool.setPlungeSpeed(300);
        newTool.setDepthPerPass(1.0);
        newTool.setStepOverPercent(0.4);
        newTool.setMaxSpindleSpeed(18000);
        ToolDefinition added = service.addTool(newTool);
        refreshList(added.getId());
    }

    private void onDuplicate(ActionEvent e) {
        ToolDefinition selected = toolList.getSelectedValue();
        if (selected == null) return;
        ToolDefinition copy = service.duplicate(selected.getId());
        refreshList(copy.getId());
    }

    private void onDelete(ActionEvent e) {
        ToolDefinition selected = toolList.getSelectedValue();
        if (selected == null) return;
        if (selected.isBuiltIn()) {
            JOptionPane.showMessageDialog(this, "Built-in tools cannot be deleted. Use Revert to restore defaults.",
                    "Tool Library", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete tool \"" + selected.getName() + "\"?",
                "Delete tool", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        service.deleteTool(selected.getId());
        refreshList(null);
    }

    private void onRevert(ActionEvent e) {
        ToolDefinition selected = toolList.getSelectedValue();
        if (selected == null || !selected.isBuiltIn()) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "Restore default values for \"" + selected.getName() + "\"?\nThe name will be kept.",
                "Revert tool", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        ToolDefinition reset = service.revertToDefault(selected.getId());
        refreshList(reset.getId());
    }

    private void onLibraryChangedExternally() {
        SwingUtilities.invokeLater(() -> {
            ToolDefinition selected = toolList.getSelectedValue();
            refreshList(selected == null ? null : selected.getId());
        });
    }

    @Override
    public void dispose() {
        service.removeListener(libraryListener);
        super.dispose();
    }

    public static void show(Window owner, UnitUtils.Units preferredUnits) {
        ToolLibraryService service = LookupService.lookup(ToolLibraryService.class);
        ToolLibraryDialog dialog = new ToolLibraryDialog(owner, service, preferredUnits);
        dialog.setVisible(true);
    }
}
