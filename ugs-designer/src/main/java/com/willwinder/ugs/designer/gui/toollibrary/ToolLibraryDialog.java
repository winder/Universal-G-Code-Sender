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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

/**
 * Master-detail manager dialog for the Tool Library. Left list shows all tools; right side edits
 * the selected entry. Add/Duplicate/Delete/Revert operate on the selection. Changes are debounce-
 * persisted by the service.
 */
public class ToolLibraryDialog extends JDialog {
    private static final int MINIMUM_HEIGHT = 400;

    /**
     * Cancelling a pick abandons the selection, not the edits — those are already persisted.
     */
    enum Mode {
        MANAGE("Tool Library"),
        PICK("Select Tool");

        private final String title;

        Mode(String title) {
            this.title = title;
        }
    }

    private final ToolLibraryService service;
    private final UnitUtils.Units preferredUnits;
    private final Mode mode;
    private final DefaultListModel<ToolDefinition> listModel = new DefaultListModel<>();
    private JList<ToolDefinition> toolList;
    private ToolEditorPanel editorPanel;
    private JButton addButton;
    private JButton duplicateButton;
    private JButton deleteButton;
    private JButton revertButton;
    private final ToolLibraryListener libraryListener = this::onLibraryChangedExternally;
    private int pendingSelfTriggeredEvents = 0;
    private ToolDefinition result;

    public ToolLibraryDialog(Window owner, ToolLibraryService service, UnitUtils.Units preferredUnits) {
        this(owner, service, preferredUnits, Mode.MANAGE, null);
    }

    /**
     * @param selectedToolId the tool to open on, or {@code null} to open on the first tool
     */
    ToolLibraryDialog(Window owner, ToolLibraryService service, UnitUtils.Units preferredUnits,
                      Mode mode, String selectedToolId) {
        super(owner, mode.title, ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.preferredUnits = preferredUnits;
        this.mode = mode;
        initComponents();
        refreshList(selectedToolId);
        service.addListener(libraryListener);
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[::400][grow]", "[grow][]"));
        setPreferredSize(new Dimension(780, 520));

        toolList = new JList<>(listModel);
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.setCellRenderer(new ToolListCellRenderer());
        toolList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectionChanged();
            }
        });
        if (mode == Mode.PICK) {
            toolList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && toolList.getSelectedValue() != null) {
                        acceptAndClose();
                    }
                }
            });
        }

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

        JScrollPane editorScrollPane = new JScrollPane(editorPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setMinimumSize(minimumScrollPaneSize(editorScrollPane));

        add(leftPanel, "grow");
        add(editorScrollPane, "grow, wrap");

        add(createBottomBar(), "spanx 2, growx");

        getRootPane().registerKeyboardAction(e -> cancelAndClose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(getPreferredSize());
        setMinimumSize(minimumDialogSize());
        setLocationRelativeTo(getOwner());
    }

    private JPanel createBottomBar() {
        JPanel bottom = new JPanel(new MigLayout("insets 4, fillx", "[grow][][]"));
        if (mode == Mode.PICK) {
            JButton selectButton = new JButton("Select");
            selectButton.addActionListener(e -> acceptAndClose());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> cancelAndClose());
            bottom.add(selectButton, "skip 1, tag ok");
            bottom.add(cancelButton, "tag cancel");
        } else {
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> cancelAndClose());
            bottom.add(closeButton, "skip 2, tag ok");
        }
        return bottom;
    }

    private void acceptAndClose() {
        ToolDefinition selected = toolList.getSelectedValue();
        // Read back through the service so a just-committed edit is included in the returned tool.
        result = selected == null ? null : service.getById(selected.getId()).orElse(selected);
        dispose();
    }

    private void cancelAndClose() {
        result = null;
        dispose();
    }

    /**
     * The tool the user picked, or empty when the dialog was cancelled or opened to manage the
     * library. Edits made while the dialog was open are persisted either way.
     */
    public Optional<ToolDefinition> getResult() {
        return Optional.ofNullable(result);
    }

    private Dimension minimumScrollPaneSize(JScrollPane scrollPane) {
        Insets insets = scrollPane.getInsets();
        int width = editorPanel.getMinimumSize().width
                + scrollPane.getVerticalScrollBar().getPreferredSize().width
                + insets.left + insets.right;
        return new Dimension(width, 0);
    }

    private Dimension minimumDialogSize() {
        Dimension contentMinimum = getContentPane().getMinimumSize();
        return new Dimension(contentMinimum.width, Math.max(MINIMUM_HEIGHT, contentMinimum.height));
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
        deleteButton.setEnabled(hasSelection && !isCustom);
        revertButton.setEnabled(isBuiltIn && !isCustom);
    }

    private void onEditorChanged(ToolDefinition edited) {
        if (edited == null) return;
        pendingSelfTriggeredEvents++;
        try {
            service.updateTool(edited);
            replaceInList(edited);
        } catch (RuntimeException ex) {
            pendingSelfTriggeredEvents--;
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tool Library", JOptionPane.WARNING_MESSAGE);
            restoreEditorFromLibrary(edited.getId());
        }
    }

    /**
     * Replaces a tool's row by id rather than at the selected index. An edit can be delivered
     * after the selection has moved on, and must never be written onto another tool's row.
     */
    private void replaceInList(ToolDefinition tool) {
        for (int i = 0; i < listModel.size(); i++) {
            if (tool.getId().equals(listModel.get(i).getId())) {
                listModel.set(i, tool);
                return;
            }
        }
    }

    /**
     * Puts the stored values back in the editor after a rejected edit so the panel never shows a
     * value the library did not accept. The editor is only reset while it still shows that tool.
     */
    private void restoreEditorFromLibrary(String id) {
        service.getById(id).ifPresent(stored -> {
            replaceInList(stored);
            ToolDefinition selected = toolList.getSelectedValue();
            if (selected != null && id.equals(selected.getId())) {
                editorPanel.setTool(stored, false);
            }
        });
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
        newTool.setToolNumber(service.nextAvailableToolNumber());
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
        if (pendingSelfTriggeredEvents > 0) {
            pendingSelfTriggeredEvents--;
            return;
        }
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

    /**
     * Opens the library for management. Nothing is returned — edits persist as they are made.
     *
     * @param selectedToolId the tool to open on, or {@code null} to open on the first tool
     */
    public static void show(Window owner, UnitUtils.Units preferredUnits, String selectedToolId) {
        open(owner, preferredUnits, Mode.MANAGE, selectedToolId);
    }

    /**
     * Opens the library to choose a tool, returning the chosen tool or empty if cancelled. The
     * tools stay editable, so the user can adjust one and select it in the same visit.
     *
     * @param selectedToolId the tool to open on, or {@code null} to open on the first tool
     */
    public static Optional<ToolDefinition> pick(Window owner, UnitUtils.Units preferredUnits, String selectedToolId) {
        return open(owner, preferredUnits, Mode.PICK, selectedToolId);
    }

    private static Optional<ToolDefinition> open(Window owner, UnitUtils.Units preferredUnits, Mode mode,
                                                 String selectedToolId) {
        ToolLibraryService service = LookupService.lookup(ToolLibraryService.class);
        ToolLibraryDialog dialog = new ToolLibraryDialog(owner, service, preferredUnits, mode, selectedToolId);
        dialog.setVisible(true);
        return dialog.getResult();
    }
}
