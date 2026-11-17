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
package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.ugs.platform.surfacescanner.MeshLevelManager;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.ugs.platform.surfacescanner.actions.CaptureSurfacePointAction;
import com.willwinder.ugs.platform.surfacescanner.actions.ScanSurfaceAction;
import com.willwinder.ugs.platform.surfacescanner.actions.ToggleApplyToGcodeAction;
import com.willwinder.ugs.platform.surfacescanner.actions.TogglePreviewAction;
import com.willwinder.ugs.platform.surfacescanner.actions.ToggleTouchPlateAction;
import com.willwinder.ugs.platform.surfacescanner.actions.UpdateMinMaxFromGcode;
import com.willwinder.ugs.platform.surfacescanner.renderable.AutoLevelPreview;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.PercentSpinner;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbPreferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoLevelerPanel extends JPanel {
    private static final String PREF_SETTINGS_EXPANDED = "settingsExpanded";
    /** Useful side-dock width. Longer buttons wrap to stay inside it. */
    private static final int TARGET_WIDTH = 240;

    public final Set<AutoLevelPanelListener> listeners = ConcurrentHashMap.newKeySet();
    private final transient SurfaceScanner surfaceScanner;
    private final transient MeshLevelManager meshLevelManager;
    private final transient AutoLevelPreview autoLevelPreview;
    private final AutoLevelSettings autoLevelSettings = new AutoLevelSettings();
    private UnitSpinner stepResolution;
    private UnitSpinner xMax;
    private UnitSpinner xMin;
    private UnitSpinner yMax;
    private UnitSpinner yMin;
    private UnitSpinner zMax;
    private UnitSpinner zMin;
    private PercentSpinner zRetract;
    private UnitSpinner zSurface;
    private boolean updatingControls = false;
    private JTable manualPointTable;
    private JLabel manualStatusLabel;
    private JButton settingsToggle;
    private JPanel settingsSummary;
    private JLabel settingsSummaryBounds;
    private JLabel settingsSummaryProbe;
    private JPanel settingsBody;
    private JLabel minLabel;
    private JLabel maxLabel;
    private JLabel xLabel;
    private JLabel yLabel;
    private JLabel zLabel;
    private JLabel resolutionLabel;
    private JLabel zSurfaceLabel;
    private JLabel zRetractLabel;
    private JButton useFileButton;
    private String fieldColumn;
    private String fieldSize;
    private JScrollPane settingsScroll;
    private boolean settingsExpanded = false;
    private boolean settingsSideBySide;

    public AutoLevelerPanel(SurfaceScanner surfaceScanner, MeshLevelManager meshLevelManager, AutoLevelPreview autoLevelPreview, AutoLevelSettings autoLevelSettings, UnitUtils.Units units) {
        this.surfaceScanner = surfaceScanner;
        this.meshLevelManager = meshLevelManager;
        this.autoLevelPreview = autoLevelPreview;

        this.autoLevelSettings.apply(autoLevelSettings);
        this.autoLevelSettings.setSettingChangeListener(() -> listeners.forEach(AutoLevelPanelListener::onSettingsChanged));

        initComponents();
        registerListeners();
        setUnits(units);
    }

    private void initComponents() {
        initializeControls();

        settingsScroll = createSettingsScroll(createSettingsPanel());
        JPanel pointList = createPointListPanel();

        // Settings can shrink below preferred height; the scroll pane then shows a bar instead of clipping.
        setLayout(new MigLayout("fill, insets 4 4 4 4, gap 0, hidemode 3", "[grow, fill]", "[0:pref][][grow, fill]"));
        add(settingsScroll, "growx, hmin 0, wrap");
        // Keep a real height. Otherwise this row is crushed to nothing once the settings section is expanded.
        add(new JSeparator(), "growx, gaptop 4:4:4, gapbottom 6:6:6, hmin pref, wrap");
        add(pointList, "grow");

        boolean expanded = NbPreferences.forModule(AutoLevelerPanel.class).getBoolean(PREF_SETTINGS_EXPANDED, false);
        setSettingsExpanded(expanded);
        updateSettingsSummary();
    }

    private static JScrollPane createSettingsScroll(JPanel settings) {
        JScrollPane scroll = new JScrollPane(settings,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }

    private void initializeControls() {
        xMin = new UnitSpinner(autoLevelSettings.getMinX(), Unit.MM);
        yMin = new UnitSpinner(autoLevelSettings.getMinY(), Unit.MM);
        zMin = new UnitSpinner(autoLevelSettings.getMinZ(), Unit.MM);
        xMax = new UnitSpinner(autoLevelSettings.getMaxX(), Unit.MM);
        yMax = new UnitSpinner(autoLevelSettings.getMaxY(), Unit.MM);
        zMax = new UnitSpinner(autoLevelSettings.getMaxZ(), Unit.MM);

        stepResolution = new UnitSpinner(autoLevelSettings.getStepResolution(), Unit.MM);
        zSurface = new UnitSpinner(autoLevelSettings.getZSurface(), Unit.MM);
        zRetract = new PercentSpinner(autoLevelSettings.getZRetract(), 0.001);
        zRetract.setToolTipText(Localization.getString("autoleveler.panel.z-retract.tooltip"));
        hideSpinnerUnits(xMin, yMin, zMin, xMax, yMax, zMax, stepResolution, zSurface);
        zRetract.setShowAbbreviation(false);
    }

    private JPanel createSettingsPanel() {
        minLabel = new JLabel(Localization.getString("autoleveler.panel.min"), SwingConstants.LEFT);
        maxLabel = new JLabel(Localization.getString("autoleveler.panel.max"), SwingConstants.LEFT);
        xLabel = new JLabel(Localization.getString("machineStatus.pin.x") + ':', SwingConstants.RIGHT);
        yLabel = new JLabel(Localization.getString("machineStatus.pin.y") + ':', SwingConstants.RIGHT);
        zLabel = new JLabel(Localization.getString("machineStatus.pin.z") + ':', SwingConstants.RIGHT);
        resolutionLabel = new JLabel(Localization.getString("autoleveler.panel.resolution") + ':', SwingConstants.RIGHT);
        zSurfaceLabel = new JLabel(Localization.getString("autoleveler.panel.z-surface") + ':', SwingConstants.RIGHT);
        zRetractLabel = new JLabel(Localization.getString("autoleveler.panel.z-retract") + ':', SwingConstants.RIGHT);
        useFileButton = commandButton(new UpdateMinMaxFromGcode(surfaceScanner), SwingConstants.CENTER);

        int fieldMax = maxFieldWidth(xMin, xMax, yMin, yMax, zMin, zMax, stepResolution, zSurface, zRetract);
        fieldColumn = "[40:pref:" + fieldMax + ", grow, sg field]";
        fieldSize = "growx, w 40:pref:" + fieldMax;

        settingsBody = new JPanel();
        settingsBody.setOpaque(false);
        settingsSideBySide = false;
        applySettingsBodyLayout(false);
        settingsBody.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSettingsBodyLayout();
            }
        });

        settingsToggle = new JButton(Localization.getString("autoleveler.panel.scan-settings"));
        settingsToggle.putClientProperty("JButton.buttonType", "borderless");
        settingsToggle.setHorizontalAlignment(SwingConstants.LEADING);
        settingsToggle.setFont(settingsToggle.getFont().deriveFont(Font.BOLD));
        settingsToggle.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        settingsToggle.addActionListener(e -> {
            setSettingsExpanded(!settingsExpanded);
            NbPreferences.forModule(AutoLevelerPanel.class).putBoolean(PREF_SETTINGS_EXPANDED, settingsExpanded);
        });

        settingsSummary = createSettingsSummary();

        JPanel settings = new ViewportWidthPanel(new MigLayout("insets 0, hidemode 3", "[grow]"));
        settings.add(settingsToggle, "growx, wrap");
        settings.add(settingsSummary, "growx, gapleft 18, gaptop 0, wmin 0, hmin pref, wrap");
        settings.add(settingsBody, "growx, gaptop 2, wrap");
        settings.add(createOptionsPanel(), "growx, gaptop 6, wmin 0, hmin 0");
        return settings;
    }

    /**
     * Fills the scroll viewport horizontally so a vertical scrollbar does not create a clipped side strip.
     */
    private static final class ViewportWidthPanel extends JPanel implements Scrollable {
        private ViewportWidthPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 16, 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JPanel createSettingsSummary() {
        settingsSummaryBounds = new JLabel();
        settingsSummaryProbe = new JLabel();

        JPanel summary = new JPanel(new MigLayout("insets 0, gap 0", "[grow]"));
        summary.setOpaque(false);
        summary.add(settingsSummaryBounds, "growx, wmin 0, wrap");
        summary.add(settingsSummaryProbe, "growx, wmin 0");
        return summary;
    }

    private JPanel createOptionsPanel() {
        return new WrappingRow(
                new JCheckBox(new TogglePreviewAction(autoLevelPreview)),
                new JCheckBox(new ToggleTouchPlateAction(autoLevelPreview)),
                new JCheckBox(new ToggleApplyToGcodeAction(meshLevelManager)));
    }

    private JPanel createPointListPanel() {
        JPanel pointList = new JPanel(new MigLayout("fill, insets 0, hidemode 3"));
        pointList.add(commandButton(new ScanSurfaceAction(surfaceScanner), SwingConstants.CENTER), "growx, wmin 0, wrap");
        pointList.add(commandButton(new CaptureSurfacePointAction(surfaceScanner), SwingConstants.CENTER), "growx, wmin 0, gaptop 4, wrap");
        manualPointTable = new JTable(new ManualPointTableModel(surfaceScanner));
        manualPointTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        manualPointTable.setFillsViewportHeight(true);
        manualPointTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        manualPointTable.setPreferredScrollableViewportSize(new Dimension(160, 90));
        useCoordinateFormat(manualPointTable);
        pointList.add(new JScrollPane(manualPointTable), "grow, pushy, gaptop 4, wrap");
        JButton removePoint = new JButton(Localization.getString("autoleveler.panel.remove-point"));
        removePoint.addActionListener(e -> surfaceScanner.removeManualPoint(manualPointTable.getSelectedRow()));
        removePoint.setEnabled(false);
        manualPointTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removePoint.setEnabled(manualPointTable.getSelectedRow() >= 0);
            }
        });
        JButton clearPoints = new JButton(Localization.getString("autoleveler.panel.clear-points"));
        clearPoints.addActionListener(e -> surfaceScanner.clearManualPoints());
        pointList.add(removePoint, "growx, split 2");
        pointList.add(clearPoints, "growx, wrap");
        manualStatusLabel = new JLabel(Localization.getString("autoleveler.panel.manual-status"));
        manualStatusLabel.setVisible(false);
        pointList.add(manualStatusLabel, "growx, wmin 0, wrap");
        surfaceScanner.addListener(this::updateManualStatus);
        updateManualStatus();
        return pointList;
    }

    private void setSettingsExpanded(boolean expanded) {
        settingsExpanded = expanded;
        settingsBody.setVisible(expanded);
        settingsSummary.setVisible(!expanded);
        Icon icon = UIManager.getIcon(expanded ? "Tree.expandedIcon" : "Tree.collapsedIcon");
        settingsToggle.setIcon(icon);
        if (expanded) {
            updateSettingsBodyLayout();
        }
        if (settingsScroll != null) {
            settingsScroll.revalidate();
        }
        revalidate();
    }

    private void updateSettingsBodyLayout() {
        if (settingsBody == null) {
            return;
        }
        int available = settingsBody.getWidth();
        if (available <= 0) {
            return;
        }
        boolean sideBySide = available >= sideBySideWidth();
        if (sideBySide == settingsSideBySide) {
            return;
        }
        applySettingsBodyLayout(sideBySide);
        if (settingsScroll != null) {
            settingsScroll.revalidate();
        }
        revalidate();
    }

    private int sideBySideWidth() {
        int axisLabel = Math.max(xLabel.getPreferredSize().width,
                Math.max(yLabel.getPreferredSize().width, zLabel.getPreferredSize().width));
        int probeLabel = Math.max(resolutionLabel.getPreferredSize().width,
                Math.max(zSurfaceLabel.getPreferredSize().width, zRetractLabel.getPreferredSize().width));
        int field = Math.max(xMin.getPreferredSize().width, stepResolution.getPreferredSize().width);
        // Axis label + two fields, gap, probe label + one field, plus MigLayout padding.
        return axisLabel + field * 2 + 12 + probeLabel + field + 24;
    }

    private void applySettingsBodyLayout(boolean sideBySide) {
        settingsSideBySide = sideBySide;
        settingsBody.removeAll();
        if (sideBySide) {
            // One grid so Resolution/Y/Z line up with the X/Y/Z rows.
            settingsBody.setLayout(new MigLayout("insets 0 2 0 0",
                    "[shrink] " + fieldColumn + " " + fieldColumn + " 12[shrink] " + fieldColumn));
            settingsBody.add(minLabel, "skip");
            settingsBody.add(maxLabel, "wrap");

            settingsBody.add(xLabel);
            settingsBody.add(xMin, fieldSize);
            settingsBody.add(xMax, fieldSize);
            settingsBody.add(resolutionLabel, "gapleft 12");
            settingsBody.add(stepResolution, fieldSize + ", wrap");

            settingsBody.add(yLabel);
            settingsBody.add(yMin, fieldSize);
            settingsBody.add(yMax, fieldSize);
            settingsBody.add(zSurfaceLabel, "gapleft 12");
            settingsBody.add(zSurface, fieldSize + ", wrap");

            settingsBody.add(zLabel);
            settingsBody.add(zMin, fieldSize);
            settingsBody.add(zMax, fieldSize);
            settingsBody.add(zRetractLabel, "gapleft 12");
            settingsBody.add(zRetract, fieldSize + ", wrap");

            settingsBody.add(useFileButton, "skip, span 2, growx, wmin 0");
        } else {
            settingsBody.setLayout(new MigLayout("insets 0 2 0 0", "[shrink] " + fieldColumn + " " + fieldColumn));
            settingsBody.add(minLabel, "skip");
            settingsBody.add(maxLabel, "wrap");
            settingsBody.add(xLabel);
            settingsBody.add(xMin, fieldSize);
            settingsBody.add(xMax, fieldSize + ", wrap");
            settingsBody.add(yLabel);
            settingsBody.add(yMin, fieldSize);
            settingsBody.add(yMax, fieldSize + ", wrap");
            settingsBody.add(zLabel);
            settingsBody.add(zMin, fieldSize);
            settingsBody.add(zMax, fieldSize + ", wrap");
            settingsBody.add(useFileButton, "skip, span 2, growx, wmin 0, wrap");

            settingsBody.add(resolutionLabel, "gaptop 8");
            settingsBody.add(stepResolution, fieldSize + ", span 2, wrap");
            settingsBody.add(zSurfaceLabel);
            settingsBody.add(zSurface, fieldSize + ", span 2, wrap");
            settingsBody.add(zRetractLabel);
            settingsBody.add(zRetract, fieldSize + ", span 2");
        }
        settingsBody.revalidate();
        settingsBody.repaint();
    }

    private void updateSettingsSummary() {
        if (settingsSummary == null) {
            return;
        }
        String unit = xMin.getUnits().getAbbreviation();
        String bounds = range("machineStatus.pin.x", xMin, xMax)
                + " " + range("machineStatus.pin.y", yMin, yMax);
        String probe = range("machineStatus.pin.z", zMin, zMax)
                + " · " + formatValue(stepResolution.getDoubleValue()) + unit;
        settingsSummaryBounds.setText("<html>" + bounds + "</html>");
        settingsSummaryProbe.setText("<html>" + probe + "</html>");
    }

    private static String range(String key, UnitSpinner min, UnitSpinner max) {
        return "<b>" + Localization.getString(key) + "</b>: " + formatValue(min.getDoubleValue()) + "-" + formatValue(max.getDoubleValue()) + min.getUnits().getAbbreviation();
    }

    private static void hideSpinnerUnits(UnitSpinner... spinners) {
        for (UnitSpinner spinner : spinners) {
            spinner.setShowAbbreviation(false);
        }
    }

    private static String formatValue(double value) {
        return com.willwinder.universalgcodesender.Utils.formatter.format(value);
    }

    private static JButton commandButton(Action action, int alignment) {
        JButton button = new JButton(action);
        button.setHorizontalAlignment(alignment);
        button.setMargin(new Insets(2, 6, 2, 6));
        return button;
    }

    /**
     * Lays its children out in a row and wraps when the panel is narrower than the row.
     * The preferred width stays at one child so the row does not force the dock wider.
     */
    private static final class WrappingRow extends JPanel {
        private static final int HGAP = 8;
        private static final int VGAP = 2;

        private WrappingRow(JComponent... children) {
            super(new FlowLayout(FlowLayout.LEFT, HGAP, VGAP));
            setOpaque(false);
            for (JComponent child : children) {
                add(child);
            }
            addComponentListener(new ComponentAdapter() {
                private int measuredWidth = -1;

                @Override
                public void componentResized(ComponentEvent e) {
                    int width = getWidth();
                    if (width != measuredWidth) {
                        measuredWidth = width;
                        revalidate();
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(widestChild(), layoutHeight(availableWidth()));
        }

        /**
         * Width the checkboxes will actually be laid out in. A size probe can set this
         * panel to its narrow preferred width; the parent is the width to wrap against.
         */
        private int availableWidth() {
            int own = getWidth();
            int parentWidth = getParent() != null ? getParent().getWidth() : 0;
            if (own > widestChild()) {
                return own;
            }
            if (parentWidth > 0) {
                return parentWidth;
            }
            return own > 0 ? own : TARGET_WIDTH;
        }

        private int widestChild() {
            int widest = 0;
            for (Component child : getComponents()) {
                if (child.isVisible()) {
                    widest = Math.max(widest, child.getPreferredSize().width);
                }
            }
            Insets insets = getInsets();
            return widest + insets.left + insets.right + HGAP * 2;
        }

        private int layoutHeight(int available) {
            Insets insets = getInsets();
            int maxWidth = Math.max(0, available - insets.left - insets.right - HGAP * 2);
            int x = 0;
            int rowHeight = 0;
            int rows = 1;
            for (Component child : getComponents()) {
                if (!child.isVisible()) {
                    continue;
                }
                Dimension size = child.getPreferredSize();
                if (x == 0 || x + size.width <= maxWidth) {
                    if (x > 0) {
                        x += HGAP;
                    }
                    x += size.width;
                    rowHeight = Math.max(rowHeight, size.height);
                } else {
                    rows++;
                    x = size.width;
                    rowHeight = Math.max(rowHeight, size.height);
                }
            }
            if (rowHeight == 0) {
                rowHeight = 1;
            }
            return insets.top + insets.bottom + VGAP + rows * rowHeight + Math.max(0, rows - 1) * VGAP;
        }
    }

    /**
     * Four digits plus the spinner buttons. The unit is shown in the collapsed summary, not in the field.
     */
    private static int maxFieldWidth(JSpinner... spinners) {
        int max = 0;
        for (JSpinner spinner : spinners) {
            FontMetrics metrics = spinner.getFontMetrics(spinner.getFont());
            int text = metrics.stringWidth("0000");
            int buttons = 0;
            for (Component child : spinner.getComponents()) {
                if (!(child instanceof JSpinner.DefaultEditor)) {
                    buttons += child.getPreferredSize().width;
                }
            }
            if (buttons <= 0) {
                buttons = Math.max(16, spinner.getPreferredSize().width / 5);
            }
            int padding = spinner.getInsets().left + spinner.getInsets().right;
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
                Insets editorInsets = editor.getInsets();
                Insets textInsets = editor.getTextField().getInsets();
                padding += editorInsets.left + editorInsets.right + textInsets.left + textInsets.right;
            }
            max = Math.max(max, text + buttons + padding);
        }
        return Math.max(max, 1);
    }

    private void updateManualStatus() {
        SwingUtilities.invokeLater(() -> {
            boolean showHint = !surfaceScanner.getScannedPoints().isEmpty() && !surfaceScanner.isValid();
            if (manualStatusLabel.isVisible() != showHint) {
                manualStatusLabel.setVisible(showHint);
                revalidate();
            }
        });
    }

    private static void useCoordinateFormat(JTable table) {
        NumberFormat format = new DecimalFormat("0.000", Localization.dfs);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number number ? format.format(number) : "");
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.setDefaultRenderer(Double.class, renderer);
    }

    private void registerListeners() {
        stepResolution.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        zRetract.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        xMin.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        xMax.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        yMin.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        yMax.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        zMin.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        zMax.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
        zSurface.addChangeListener((ChangeEvent e) -> syncControlsToSettings());
    }

    private void syncControlsToSettings() {
        if (updatingControls) {
            return;
        }

        autoLevelSettings.apply(copySettings());
        updateSpinnerLimits();
        updateSettingsSummary();
    }

    private void updateSpinnerLimits() {
        xMax.setMinimum(xMin.getDoubleValue());
        xMin.setMaximum(xMax.getDoubleValue());
        yMax.setMinimum(yMin.getDoubleValue());
        yMin.setMaximum(yMax.getDoubleValue());
        zMax.setMinimum(zMin.getDoubleValue());
        zMin.setMaximum(zMax.getDoubleValue());

        // There is no point in having the step resolution bigger than the largest size
        double stepResolutionMax = Math.max(Math.abs(xMin.getDoubleValue()) + xMax.getDoubleValue(), Math.abs(yMin.getDoubleValue()) + yMax.getDoubleValue());
        stepResolution.setMaximum(stepResolutionMax);
    }

    private AutoLevelSettings copySettings() {
        AutoLevelSettings newSettings = new AutoLevelSettings(autoLevelSettings);
        newSettings.setZSurface(zSurface.getDoubleValue(Unit.MM));
        newSettings.setStepResolution(stepResolution.getDoubleValue(Unit.MM));
        newSettings.setMinX(xMin.getDoubleValue(Unit.MM));
        newSettings.setMinY(yMin.getDoubleValue(Unit.MM));
        newSettings.setMinZ(zMin.getDoubleValue(Unit.MM));
        newSettings.setMaxX(xMax.getDoubleValue(Unit.MM));
        newSettings.setMaxY(yMax.getDoubleValue(Unit.MM));
        newSettings.setMaxZ(zMax.getDoubleValue(Unit.MM));
        newSettings.setZRetract(zRetract.getDoubleValue());
        return newSettings;
    }

    public AutoLevelSettings getSettings() {
        return autoLevelSettings;
    }

    public void setSettings(AutoLevelSettings autoLevelSettings) {
        this.autoLevelSettings.apply(autoLevelSettings);
        syncSettingsToControls(autoLevelSettings);
    }

    public void addAutoLevelerPanelListener(AutoLevelPanelListener listener) {
        listeners.add(listener);
    }

    private void syncSettingsToControls(AutoLevelSettings autoLevelSettings) {
        updatingControls = true;
        try {
            setSpinnerValue(stepResolution, autoLevelSettings.getStepResolution());
            setSpinnerValue(zSurface, autoLevelSettings.getZSurface());
            setSpinnerValue(xMin, autoLevelSettings.getMinX());
            setSpinnerValue(yMin, autoLevelSettings.getMinY());
            setSpinnerValue(zMin, autoLevelSettings.getMinZ());
            setSpinnerValue(xMax, autoLevelSettings.getMaxX());
            setSpinnerValue(yMax, autoLevelSettings.getMaxY());
            setSpinnerValue(zMax, autoLevelSettings.getMaxZ());

            if (zRetract.getDoubleValue() != autoLevelSettings.getZRetract()) {
                zRetract.setValue(autoLevelSettings.getZRetract());
            }

            updateSpinnerLimits();
            updateSettingsSummary();
        } finally {
            updatingControls = false;
        }
    }

    private static void setSpinnerValue(UnitSpinner spinner, double millimeters) {
        if (spinner.getDoubleValue(Unit.MM) != millimeters) {
            spinner.setValue(millimeters, Unit.MM);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (manualPointTable != null) {
            manualPointTable.setEnabled(enabled);
        }
        stepResolution.setEnabled(enabled);
        zRetract.setEnabled(enabled);
        xMin.setEnabled(enabled);
        xMax.setEnabled(enabled);
        yMin.setEnabled(enabled);
        yMax.setEnabled(enabled);
        zMin.setEnabled(enabled);
        zMax.setEnabled(enabled);
        zSurface.setEnabled(enabled);
    }

    public Position getMinPosition() {
        return new Position(xMin.getDoubleValue(Unit.MM), yMin.getDoubleValue(Unit.MM), zMin.getDoubleValue(Unit.MM), UnitUtils.Units.MM);
    }

    public Position getMaxPosition() {
        return new Position(xMax.getDoubleValue(Unit.MM), yMax.getDoubleValue(Unit.MM), zMax.getDoubleValue(Unit.MM), UnitUtils.Units.MM);
    }

    public void setUnits(UnitUtils.Units units) {
        Unit spinnerUnit = units == UnitUtils.Units.MM ? Unit.MM : Unit.INCH;
        updatingControls = true;
        try {
            stepResolution.setUnits(spinnerUnit);
            xMin.setUnits(spinnerUnit);
            xMax.setUnits(spinnerUnit);
            yMin.setUnits(spinnerUnit);
            yMax.setUnits(spinnerUnit);
            zMin.setUnits(spinnerUnit);
            zMax.setUnits(spinnerUnit);
            zSurface.setUnits(spinnerUnit);
        } finally {
            updatingControls = false;
        }

        syncSettingsToControls(autoLevelSettings);
    }
}
