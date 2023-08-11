/*
    Copyright 2017-2023 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.processors.ArcExpander;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.LineSplitter;
import com.willwinder.universalgcodesender.gcode.processors.MeshLeveler;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.ProbeEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Settings.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;

/**
 * Top component which displays something.
 */
@TopComponent.Description(preferredID = "AutoLevelerTopComponent")
@TopComponent.Registration(mode = Mode.OUTPUT, openAtStartup = false)
@ActionID(category = AutoLevelerTopComponent.AutoLevelerCategory, id = AutoLevelerTopComponent.AutoLevelerActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:AutoLevelerTopComponent>",
        preferredID = "AutoLevelerTopComponent"
)
public final class AutoLevelerTopComponent extends TopComponent implements ItemListener, ChangeListener, UGSEventListener {
    private final BackendAPI backend;
    private final Settings settings;

    private AutoLevelPreview autoLevelPreview;
    private SurfaceScanner scanner;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Used to disable the change listener temporarily.
    private boolean bulkChanges = false;

    public final static String AutoLevelerTitle = Localization.getString("platform.window.autoleveler", lang);
    public final static String AutoLevelerTooltip = Localization.getString("platform.window.autoleveler.tooltip", lang);
    public final static String AutoLevelerActionId = "com.willwinder.ugs.platform.surfacescanner.AutoLevelerTopComponent";
    public final static String AutoLevelerCategory = LocalizingService.CATEGORY_WINDOW;

    private ImmutableList<CommandProcessor> activeCommandProcessors = ImmutableList.of();

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
      public Localizer() {
        super(AutoLevelerCategory, AutoLevelerActionId, AutoLevelerTitle);
      }
    }

    public AutoLevelerTopComponent() {
        initComponents();
        setName(AutoLevelerTitle);
        setToolTipText(AutoLevelerTooltip);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        settings = backend.getSettings();

        updateSettings();

        ChangeListener cl = this;
        stepResolution.addChangeListener(cl);
        zRetract.addChangeListener(cl);
        xMin.addChangeListener(cl);
        xMax.addChangeListener(cl);
        yMin.addChangeListener(cl);
        yMax.addChangeListener(cl);
        zMin.addChangeListener(cl);
        zMax.addChangeListener(cl);
        zSurface.addChangeListener(cl);

        // Localize...
        this.useLoadedFile.setText(Localization.getString("autoleveler.panel.use-file"));
        this.scanSurfaceButton.setText(Localization.getString("autoleveler.panel.scan-surface"));
        this.applyToGcode.setText(Localization.getString("autoleveler.panel.apply"));
        this.minLabel.setText(Localization.getString("autoleveler.panel.min"));
        this.maxLabel.setText(Localization.getString("autoleveler.panel.max"));
        this.xLabel.setText(Localization.getString("machineStatus.pin.x") + ':');
        this.yLabel.setText(Localization.getString("machineStatus.pin.y") + ':');
        this.zLabel.setText(Localization.getString("machineStatus.pin.z") + ':');
        this.zSurfaceLabel.setText(Localization.getString("autoleveler.panel.z-surface") + ':');
        this.resolutionLabel.setText(Localization.getString("autoleveler.panel.resolution") + ':');
        this.zRetractLabel.setText(Localization.getString("autoleveler.panel.z-retract") + ':');
        this.dataViewer.setText(Localization.getString("autoleveler.panel.view-data"));
        this.settingsButton.setText(Localization.getString("mainWindow.swing.settingsMenu"));
        this.generateTestDataButton.setText("Generate Test Data");
        this.visibleAutoLeveler.setText(Localization.getString("autoleveler.panel.visible"));
    }

    private void updateSettings() {
        this.bulkChanges = true;

        boolean isSettingChange = false;
        AutoLevelSettings als = settings.getAutoLevelSettings();
        if (getValue(this.stepResolution) != als.stepResolution) {
            this.stepResolution.setValue(als.stepResolution);
            isSettingChange = true;
        }
        if (getValue(this.zRetract) != als.zRetract) {
            this.zRetract.setValue(als.zRetract);
            isSettingChange = true;
        }
        if (getValue(this.zSurface) != als.zSurface) {
            this.zSurface.setValue(als.zSurface);
            isSettingChange = true;
        }
        this.bulkChanges = false;
        if (isSettingChange) {
            this.stateChanged(null);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ProbeEvent) {
            try {
                scanner.handleEvent((ProbeEvent) evt);
                if (scanner.isValid()) {
                    updateMeshLeveler();
                }
            } catch (Exception e) {
                // TODO make this error message more descriptive
                e.printStackTrace();
                GUIHelpers.displayErrorDialog(Localization.getString("autoleveler.probe-failed"));
            }
        } else if (evt instanceof SettingChangedEvent) {
            updateSettings();
        } else if (evt instanceof FileStateEvent) {
            FileState fileState = ((FileStateEvent) evt).getFileState();
            if (fileState == FileState.OPENING_FILE) {
                // file open clears the backend CommandProcessor list
                // (despite what the javadoc for applyCommandProcessor would suggest)
                updateMeshLeveler();
            }
        }
    }

    private double getValue(JSpinner spinner) {
        Object o = spinner.getValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ignored) {
        }
        return 0.0f;
    }

    private void updateScanner() {
        if (scanner.isValid()) {
            // Prompt the user before destroying the existing probe data?
            int result = JOptionPane.showConfirmDialog(new Frame(),
                    Localization.getString("autoleveler.panel.overwrite"),
                    Localization.getString("AutoLevelerTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        Settings.AutoLevelSettings autoLevelerSettings = this.settings.getAutoLevelSettings();
        autoLevelerSettings.stepResolution = getValue(this.stepResolution);
        autoLevelerSettings.zRetract = getValue(this.zRetract);
        autoLevelerSettings.zSurface = getValue(this.zSurface);
        settings.setAutoLevelSettings(autoLevelerSettings);

        double xOff = autoLevelerSettings.autoLevelProbeOffset.x;
        double yOff = autoLevelerSettings.autoLevelProbeOffset.y;
        Position corner1 = new Position(getValue(xMin) + xOff, getValue(yMin) + yOff, getValue(zMin), backend.getSettings().getPreferredUnits());
        Position corner2 = new Position(getValue(xMax) + xOff, getValue(yMax) + yOff, getValue(zMax), backend.getSettings().getPreferredUnits());

        scanner.update(corner1, corner2);

        if (autoLevelPreview != null) {
            autoLevelPreview.updateSettings(
                    scanner.getProbeStartPositions(),
                    scanner.getProbePositionGrid()
            );
        }
        updateMeshLeveler();
    }

    /**
     * JRadioButton's have strange state changes, so using item change.
     *
     * @param e
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            updateScanner();
        }
    }

    /**
     * The preview parameters were changed.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        // This state change handler is only for the visualizer.
        // prevent infinite loop, only call when the stateChange event was triggered by a swing component.
        if (bulkChanges || scanner == null || e == null) {
            return;
        }

        updateScanner();
    }

    private void updateMeshLeveler() {
        if (this.bulkChanges) {
            return;
        }

        Settings.AutoLevelSettings autoLevelSettings = this.settings.getAutoLevelSettings();
        try {
            this.bulkChanges = true;

            // Step 0: Remove previously active command processors
            for(CommandProcessor p : activeCommandProcessors) {
                backend.removeCommandProcessor(p);
            }
            if (!scanner.isValid() || !applyToGcode.isSelected()) {
                activeCommandProcessors = ImmutableList.of();
                activeLabel.setText("INACTIVE");
                activeLabel.setForeground(Color.RED);
                return;
            }

            ImmutableList.Builder<CommandProcessor> commandProcessors = ImmutableList.builder();

            // Step 1: Convert arcs to line segments.
            commandProcessors.add(new ArcExpander(true, autoLevelSettings.autoLevelArcSliceLength, GcodePreprocessorUtils.getDecimalFormatter()));

            // Step 2: Line splitter. No line should be longer than some fraction of "resolution"
            commandProcessors.add(new LineSplitter(getValue(stepResolution) / 4));

            // Step 3: Adjust Z heights codes based on mesh offsets.
            commandProcessors.add(
                    new MeshLeveler(getValue(this.zSurface),
                            scanner.getProbePositionGrid()));

            activeCommandProcessors = commandProcessors.build();
            for(CommandProcessor p : activeCommandProcessors) {
                backend.applyCommandProcessor(p);
            }
            activeLabel.setForeground(Color.GREEN);
            activeLabel.setText("ACTIVE");
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getMessage());
            Exceptions.printStackTrace(ex);
        } finally {
            this.bulkChanges = false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        minLabel = new javax.swing.JLabel();
        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        zLabel = new javax.swing.JLabel();
        zMin = new javax.swing.JSpinner();
        yMin = new javax.swing.JSpinner();
        xMin = new javax.swing.JSpinner();
        maxLabel = new javax.swing.JLabel();
        xMax = new javax.swing.JSpinner();
        yMax = new javax.swing.JSpinner();
        zMax = new javax.swing.JSpinner();
        visibleAutoLeveler = new javax.swing.JCheckBox();
        useLoadedFile = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        resolutionLabel = new javax.swing.JLabel();
        stepResolution = new javax.swing.JSpinner();
        zSurfaceLabel = new javax.swing.JLabel();
        zSurface = new javax.swing.JSpinner();
        dataViewer = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        generateTestDataButton = new javax.swing.JButton();
        zRetractLabel = new javax.swing.JLabel();
        zRetract = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        scanSurfaceButton = new javax.swing.JButton();
        applyToGcode = new javax.swing.JCheckBox();
        activeLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(minLabel, "Min");

        org.openide.awt.Mnemonics.setLocalizedText(xLabel, "X");

        org.openide.awt.Mnemonics.setLocalizedText(yLabel, "Y");

        org.openide.awt.Mnemonics.setLocalizedText(zLabel, "Z");

        zMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        yMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        xMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        org.openide.awt.Mnemonics.setLocalizedText(maxLabel, "Max");

        xMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        yMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        zMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        visibleAutoLeveler.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(visibleAutoLeveler, "Visible AutoLeveler");
        visibleAutoLeveler.addActionListener(this::visibleAutoLevelerActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(useLoadedFile, "Use Loaded File");
        useLoadedFile.addActionListener(this::useLoadedFileActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(xLabel)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(minLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(xMin))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(yLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(yMin))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(zLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(zMin)))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yMax, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                            .addComponent(zMax)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(maxLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(xMax, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(visibleAutoLeveler)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(useLoadedFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(visibleAutoLeveler)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(maxLabel)
                        .addGap(79, 79, 79)
                        .addComponent(zMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(minLabel)
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(xLabel)
                            .addComponent(xMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(xMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yLabel)
                            .addComponent(yMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zLabel)
                            .addComponent(zMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useLoadedFile)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, "Resolution:");

        stepResolution.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));

        org.openide.awt.Mnemonics.setLocalizedText(zSurfaceLabel, "Z Surface:");

        zSurface.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        org.openide.awt.Mnemonics.setLocalizedText(dataViewer, "View Data");
        dataViewer.addActionListener(this::dataViewerActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(settingsButton, "Settings");
        settingsButton.addActionListener(this::settingsButtonActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(generateTestDataButton, "Generate Test Data");
        generateTestDataButton.addActionListener(this::generateTestDataButtonActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(zRetractLabel, "Z Resolution:");

        zRetract.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataViewer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(generateTestDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionLabel)
                            .addComponent(zSurfaceLabel))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(stepResolution, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(zSurface, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(zRetractLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(zRetract, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resolutionLabel)
                    .addComponent(stepResolution, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zRetractLabel)
                    .addComponent(zRetract, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zSurfaceLabel)
                    .addComponent(zSurface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataViewer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateTestDataButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(scanSurfaceButton, "Scan Surface");
        scanSurfaceButton.addActionListener(this::scanSurfaceButtonActionPerformed);

        applyToGcode.setSelected(true);
        applyToGcode.setActionCommand("applyToGcode");
        applyToGcode.setLabel("Apply to Gcode");
        applyToGcode.addActionListener(this::applyToGcodeActionPerformed);

        org.openide.awt.Mnemonics.setLocalizedText(activeLabel, "INACTIVE");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scanSurfaceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(applyToGcode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(activeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scanSurfaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyToGcode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(activeLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1524, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }

    private void scanSurfaceButtonActionPerformed(java.awt.event.ActionEvent evt) {
        updateScanner();
        scanner.scan();
    }

    private void dataViewerActionPerformed(java.awt.event.ActionEvent evt) {
        List<Map<String, Double>> probeData = new ArrayList<>();

        // Collect data from grid.
        if (scanner != null && scanner.getProbePositionGrid() != null) {
            for (Position[] row : scanner.getProbePositionGrid()) {
                for (Position p : row) {
                    if (p != null) {
                        probeData.add(ImmutableMap.of(
                                "x", p.x,
                                "y", p.y,
                                "z", p.z
                        ));
                    }
                }
            }
        }

        JTextArea ta = new JTextArea(15, 30);
        ta.setText(GSON.toJson(probeData));
        JOptionPane.showMessageDialog(null, new JScrollPane(ta));
    }

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        OptionsDisplayer.getDefault().open("UGS/autoleveler");
    }

    private void useLoadedFileActionPerformed(java.awt.event.ActionEvent evt) {
        if (backend.getProcessedGcodeFile() == null) {
            return;
        }

        FileStats fs = backend.getSettings().getFileStats();
        Position min = fs.minCoordinate.getPositionIn(backend.getSettings().getPreferredUnits());
        Position max = fs.maxCoordinate.getPositionIn(backend.getSettings().getPreferredUnits());

        this.xMin.setValue(min.x);
        this.yMin.setValue(min.y);
        this.zMin.setValue(min.z);

        this.xMax.setValue(max.x);
        this.yMax.setValue(max.y);
        this.zMax.setValue(max.z);
    }

    private void generateTestDataButtonActionPerformed(java.awt.event.ActionEvent evt) {
        updateScanner();
        scanner.scanRandomData();
        updateMeshLeveler();
    }

    private void visibleAutoLevelerActionPerformed(java.awt.event.ActionEvent evt) {
        autoLevelPreview.setEnabled(visibleAutoLeveler.isSelected());
    }

    private void applyToGcodeActionPerformed(java.awt.event.ActionEvent evt) {
        updateMeshLeveler();
    }

    private javax.swing.JLabel activeLabel;
    private javax.swing.JCheckBox applyToGcode;
    private javax.swing.JButton dataViewer;
    private javax.swing.JButton generateTestDataButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel maxLabel;
    private javax.swing.JLabel minLabel;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JButton scanSurfaceButton;
    private javax.swing.JButton settingsButton;
    private javax.swing.JSpinner stepResolution;
    private javax.swing.JButton useLoadedFile;
    private javax.swing.JCheckBox visibleAutoLeveler;
    private javax.swing.JLabel xLabel;
    private javax.swing.JSpinner xMax;
    private javax.swing.JSpinner xMin;
    private javax.swing.JLabel yLabel;
    private javax.swing.JSpinner yMax;
    private javax.swing.JSpinner yMin;
    private javax.swing.JLabel zLabel;
    private javax.swing.JSpinner zMax;
    private javax.swing.JSpinner zMin;
    private javax.swing.JSpinner zRetract;
    private javax.swing.JLabel zRetractLabel;
    private javax.swing.JSpinner zSurface;
    private javax.swing.JLabel zSurfaceLabel;

    @Override
    public void componentOpened() {
        scanner = new SurfaceScanner(this.backend);
        if (autoLevelPreview == null) {
            autoLevelPreview = new AutoLevelPreview(Localization.getString("platform.visualizer.renderable.autolevel-preview"));
            RenderableUtils.registerRenderable(autoLevelPreview);
        }

    }

    @Override
    public void componentClosed() {
        RenderableUtils.removeRenderable(autoLevelPreview);
        autoLevelPreview = null;
    }

    public void writeProperties(java.util.Properties p) {
        // No properties
    }

    public void readProperties(java.util.Properties p) {
        // No properties
    }
}
