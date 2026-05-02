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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;

/**
 * Lightweight picker — a list of library tools with a read-only preview of the selected tool's
 * fields. Returns the chosen tool via {@link #pick(Window, UnitUtils.Units)} or an empty optional
 * if the user cancels. A "Manage Library…" button opens the full {@link ToolLibraryDialog}.
 */
public class ToolLibraryPickerDialog extends JDialog {
    private final ToolLibraryService service;
    private final UnitUtils.Units preferredUnits;
    private final DefaultListModel<ToolDefinition> listModel = new DefaultListModel<>();
    private JList<ToolDefinition> toolList;
    private ToolEditorPanel previewPanel;
    private ToolDefinition result;

    public ToolLibraryPickerDialog(Window owner, ToolLibraryService service, UnitUtils.Units preferredUnits) {
        super(owner, "Pick a Tool", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.preferredUnits = preferredUnits;
        initComponents();
        refreshList();
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
                ToolDefinition selected = toolList.getSelectedValue();
                previewPanel.setTool(selected, true);
            }
        });
        toolList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && toolList.getSelectedValue() != null) {
                    acceptAndClose();
                }
            }
        });

        add(new JScrollPane(toolList), "grow");

        previewPanel = new ToolEditorPanel(preferredUnits);
        add(new JScrollPane(previewPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "grow, wrap");

        JPanel bottom = new JPanel(new MigLayout("insets 4, fillx", "[grow][][]"));
        JButton manageButton = new JButton("Manage Library…");
        manageButton.addActionListener(e -> ToolLibraryDialog.show(this, preferredUnits));
        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> acceptAndClose());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> { result = null; dispose(); });
        bottom.add(manageButton, "");
        bottom.add(selectButton, "tag ok");
        bottom.add(cancelButton, "tag cancel");
        add(bottom, "spanx 2, growx");

        getRootPane().registerKeyboardAction(e -> { result = null; dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(getPreferredSize());
        setMinimumSize(new Dimension(560, 400));
        setLocationRelativeTo(getOwner());
    }

    private void refreshList() {
        List<ToolDefinition> tools = service.getTools();
        listModel.clear();
        tools.forEach(listModel::addElement);
        if (!listModel.isEmpty()) {
            toolList.setSelectedIndex(0);
        }
    }

    private void acceptAndClose() {
        result = toolList.getSelectedValue();
        dispose();
    }

    public Optional<ToolDefinition> getResult() {
        return Optional.ofNullable(result);
    }

    public static Optional<ToolDefinition> pick(Window owner, UnitUtils.Units preferredUnits) {
        ToolLibraryService service = LookupService.lookup(ToolLibraryService.class);
        ToolLibraryPickerDialog dialog = new ToolLibraryPickerDialog(owner, service, preferredUnits);
        dialog.setVisible(true);
        return dialog.getResult();
    }
}
