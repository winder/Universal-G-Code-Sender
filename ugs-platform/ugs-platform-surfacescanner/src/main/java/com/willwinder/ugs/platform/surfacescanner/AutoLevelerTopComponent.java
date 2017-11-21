/*
    Copyright 2017 Will Winder

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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.ArcExpander;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.LineSplitter;
import com.willwinder.universalgcodesender.gcode.processors.MeshLeveler;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Settings.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3d;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "AutoLevelerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = AutoLevelerTopComponent.AutoLevelerCategory, id = AutoLevelerTopComponent.AutoLevelerActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:AutoLevelerTopComponent>",
        preferredID = "AutoLevelerTopComponent"
)
public final class AutoLevelerTopComponent extends TopComponent implements ItemListener, ChangeListener, UGSEventListener {
    private final BackendAPI backend;
    private final Settings settings;

    private AutoLevelPreview r;
    private SurfaceScanner scanner;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Used to disable the change listener temporarily.
    private boolean bulkChanges = false;
    boolean scanningSurface = false;

    public final static String AutoLevelerTitle = Localization.getString("platform.window.autoleveler", lang);
    public final static String AutoLevelerTooltip = Localization.getString("platform.window.autoleveler.tooltip", lang);
    public final static String AutoLevelerActionId = "com.willwinder.ugs.platform.surfacescanner.AutoLevelerTopComponent";
    public final static String AutoLevelerCategory = LocalizingService.CATEGORY_WINDOW;

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
        xMin.addChangeListener(cl);
        xMax.addChangeListener(cl);
        yMin.addChangeListener(cl);
        yMax.addChangeListener(cl);
        zMin.addChangeListener(cl);
        zMax.addChangeListener(cl);
        unitInch.addItemListener(this);
        unitMM.addItemListener(this);

        // Localize...
        this.useLoadedFile.setText(Localization.getString("autoleveler.panel.use-file"));
        this.scanSurfaceButton.setText(Localization.getString("autoleveler.panel.scan-surface"));
        this.applyToGcode.setText(Localization.getString("autoleveler.panel.apply"));
        this.unitMM.setText("mm");
        this.unitInch.setText("inch");
        this.minLabel.setText(Localization.getString("autoleveler.panel.min"));
        this.maxLabel.setText(Localization.getString("autoleveler.panel.max"));
        this.xLabel.setText(Localization.getString("machineStatus.pin.x") + ':');
        this.yLabel.setText(Localization.getString("machineStatus.pin.y") + ':');
        this.zLabel.setText(Localization.getString("machineStatus.pin.z") + ':');
        this.zSurfaceLabel.setText(Localization.getString("autoleveler.panel.z-surface") + ':');
        this.resolutionLabel.setText(Localization.getString("autoleveler.panel.resolution") + ':');
        this.dataViewer.setText(Localization.getString("autoleveler.panel.view-data"));
        this.settingsButton.setText(Localization.getString("mainWindow.swing.settingsMenu"));
        this.generateTestDataButton.setText("Generate Test Data");
    }

    private void updateSettings() {
        this.bulkChanges = true;

        // Only set units radio button the first time.
        if (!this.unitInch.isSelected() && !this.unitMM.isSelected()) {
            Units u = settings.getPreferredUnits();
            this.unitInch.setSelected(u == Units.INCH);
            this.unitMM.setSelected(u == Units.MM);
        }

        AutoLevelSettings als = settings.getAutoLevelSettings();
        this.stepResolution.setValue(als.stepResolution);
        this.zSurface.setValue(als.zSurface);

        this.bulkChanges = false;
        this.stateChanged(null);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isProbeEvent()) {
            if (!scanningSurface) return;

            Position probe = evt.getProbePosition();
            Position offset = this.settings.getAutoLevelSettings().autoLevelProbeOffset;

            if (probe.getUnits() == Units.UNKNOWN || offset.getUnits() == Units.UNKNOWN) {
                System.out.println("Unknown units in autoleveler receiving probe.");
            }

            offset = offset.getPositionIn(probe.getUnits());

            scanner.probeEvent(new Position(
                    probe.x + offset.x,
                    probe.y + offset.y,
                    probe.z + offset.z,
                    probe.getUnits()));
        }

        else if(evt.isSettingChangeEvent()) {
            updateSettings();
        }
    }

    private double getValue(JSpinner spinner) {
        Object o = spinner.getValue();
        try {
            return Double.parseDouble(o.toString());
        } catch(Exception ignored) {
        }
        return 0.0f;
    }

    private AutoLevelSettings updateScanner(Units units) {
        Settings.AutoLevelSettings autoLevelerSettings = this.settings.getAutoLevelSettings();
        double xOff = autoLevelerSettings.autoLevelProbeOffset.x;
        double yOff = autoLevelerSettings.autoLevelProbeOffset.y;

        Position corner1 = new Position(getValue(xMin) + xOff, getValue(yMin) + yOff, getValue(zMin), units);
        Position corner2 = new Position(getValue(xMax) + xOff, getValue(yMax) + yOff, getValue(zMax), units);

        autoLevelerSettings.stepResolution = getValue(this.stepResolution);
        autoLevelerSettings.zSurface = getValue(this.zSurface);

        scanner.update(corner1, corner2, autoLevelerSettings.stepResolution);

        if (r != null) {
            r.updateSettings(
                    scanner.getProbeStartPositions(),
                    scanner.getUnits(),
                    scanner.getProbePositionGrid(),
                    scanner.getMaxXYZ(),
                    scanner.getMinXYZ());
        }

        return autoLevelerSettings;
    }

    /**
     * JRadioButton's have strange state changes, so using item change.
     * @param e 
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getItem() == unitMM) {
                updateScanner(Units.MM);
            } else {
                updateScanner(Units.INCH);
            }
        }
    }

    /**
     * The preview parameters were changed.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        // This state change handler is only for the visualizer.
        if (bulkChanges || scanner == null) {
            return;
        }

        Units units = this.unitInch.isSelected() ? Units.INCH : Units.MM;

        Settings.AutoLevelSettings autoLevelSettings = updateScanner(units);
        autoLevelSettings.zSurface = getValue(this.zSurface);
        autoLevelSettings.stepResolution = getValue(this.stepResolution);

        // prevent infinite loop, only call when the stateChange event was triggered by a swing component.
        if (e != null) {
            settings.setAutoLevelSettings(autoLevelSettings);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        unitGroup = new javax.swing.ButtonGroup();
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
        jPanel2 = new javax.swing.JPanel();
        resolutionLabel = new javax.swing.JLabel();
        stepResolution = new javax.swing.JSpinner();
        zSurfaceLabel = new javax.swing.JLabel();
        zSurface = new javax.swing.JSpinner();
        dataViewer = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        generateTestDataButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        scanSurfaceButton = new javax.swing.JButton();
        applyToGcode = new javax.swing.JButton();
        unitMM = new javax.swing.JRadioButton();
        unitInch = new javax.swing.JRadioButton();
        useLoadedFile = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(minLabel, "Min");

        org.openide.awt.Mnemonics.setLocalizedText(xLabel, "X");

        org.openide.awt.Mnemonics.setLocalizedText(yLabel, "Y");

        org.openide.awt.Mnemonics.setLocalizedText(zLabel, "Z");

        zMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        zMin.setSize(new java.awt.Dimension(33, 26));

        yMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        yMin.setSize(new java.awt.Dimension(33, 26));

        xMin.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        xMin.setSize(new java.awt.Dimension(33, 26));

        org.openide.awt.Mnemonics.setLocalizedText(maxLabel, "Max");

        xMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        xMax.setSize(new java.awt.Dimension(33, 26));

        yMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        yMax.setSize(new java.awt.Dimension(33, 26));

        zMax.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        zMax.setSize(new java.awt.Dimension(33, 26));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(xLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minLabel)
                            .addComponent(xMin, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(zLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(zMin))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(yLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(yMin)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxLabel)
                    .addComponent(yMax, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                    .addComponent(xMax)
                    .addComponent(zMax))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(maxLabel)
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(67, 67, 67)
                                .addComponent(zMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(xMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9)
                                .addComponent(yMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(minLabel)
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(xLabel)
                            .addComponent(xMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yLabel)
                            .addComponent(yMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zLabel)
                            .addComponent(zMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, "Resolution:");

        stepResolution.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));

        org.openide.awt.Mnemonics.setLocalizedText(zSurfaceLabel, "Z Surface:");

        zSurface.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));

        org.openide.awt.Mnemonics.setLocalizedText(dataViewer, "View Data");
        dataViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataViewerActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(settingsButton, "Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(generateTestDataButton, "Generate Test Data");
        generateTestDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateTestDataButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataViewer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(resolutionLabel)
                                .addGap(40, 40, 40)
                                .addComponent(stepResolution, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(zSurfaceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(zSurface, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(generateTestDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        scanSurfaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanSurfaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(applyToGcode, "Apply to Gcode");
        applyToGcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToGcodeActionPerformed(evt);
            }
        });

        unitGroup.add(unitMM);
        unitMM.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(unitMM, "mm");

        unitGroup.add(unitInch);
        org.openide.awt.Mnemonics.setLocalizedText(unitInch, "inch");

        org.openide.awt.Mnemonics.setLocalizedText(useLoadedFile, "Use Loaded File");
        useLoadedFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useLoadedFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useLoadedFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(unitMM)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(unitInch)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(applyToGcode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scanSurfaceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unitMM)
                    .addComponent(unitInch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useLoadedFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scanSurfaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyToGcode)
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
                .addContainerGap(1519, Short.MAX_VALUE))
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
    }// </editor-fold>//GEN-END:initComponents

    private void scanSurfaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanSurfaceButtonActionPerformed
        if (scanner == null || scanner.getProbeStartPositions() == null || scanner.getProbeStartPositions().isEmpty()) {
            return;
        }

        scanningSurface = true;
        try {
            Units u = this.unitMM.isSelected() ? Units.MM : Units.INCH;
            AutoLevelSettings als = settings.getAutoLevelSettings();
            for (Position p : scanner.getProbeStartPositions()) {
                backend.sendGcodeCommand(true, String.format("G90 G21 G0 X%f Y%f Z%f", p.x, p.y, p.z));
                backend.probe("Z", als.probeSpeed, this.scanner.getProbeDistance(), u);
                backend.sendGcodeCommand(true, String.format("G90 G21 G0 Z%f", p.z));
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            scanningSurface = false;
        }
    }//GEN-LAST:event_scanSurfaceButtonActionPerformed

    private void dataViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataViewerActionPerformed
        List<Map<String,Double>> probeData = new ArrayList<>();

        // Collect data from grid.
        if (scanner != null && scanner.getProbePositionGrid() != null) {
            for (Point3d[] row : scanner.getProbePositionGrid()) {
                for (Point3d p : row) {
                    if (p != null) {
                        probeData.add(ImmutableMap.<String,Double>of(
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
    }//GEN-LAST:event_dataViewerActionPerformed

    private void applyToGcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToGcodeActionPerformed
        GcodeParser gcp = new GcodeParser();
        Settings.AutoLevelSettings autoLevelSettings = this.settings.getAutoLevelSettings();

        // Step 0: Get rid of comments.
        gcp.addCommandProcessor(new  CommentProcessor());

        // Step 1: The arc processor and line processors NO LONGER need to be split!

        // Step 2: Must convert arcs to line segments.
        gcp.addCommandProcessor(new ArcExpander(true, autoLevelSettings.autoLevelArcSliceLength));

        // Step 3: Line splitter. No line should be longer than some fraction of "resolution"
        gcp.addCommandProcessor(new LineSplitter(getValue(stepResolution)/10));

        // Step 4: Adjust Z heights codes based on mesh offsets.
        gcp.addCommandProcessor(new MeshLeveler(getValue(this.zSurface), scanner.getProbePositionGrid(), scanner.getUnits()));

        try {
            backend.applyGcodeParser(gcp);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_applyToGcodeActionPerformed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        OptionsDisplayer.getDefault().open("UGS/autoleveler");
    }//GEN-LAST:event_settingsButtonActionPerformed

    private void useLoadedFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useLoadedFileActionPerformed
        if (backend.getProcessedGcodeFile() == null) {
            return;
        }

        FileStats fs = backend.getSettings().getFileStats();

        Units u = this.unitMM.isSelected() ? Units.MM : Units.INCH;
        Position min = fs.minCoordinate.getPositionIn(u);
        Position max = fs.maxCoordinate.getPositionIn(u);

        this.xMin.setValue(min.x);
        this.yMin.setValue(min.y);
        this.zMin.setValue(min.z);

        this.xMax.setValue(max.x);
        this.yMax.setValue(max.y);
        this.zMax.setValue(max.z);
    }//GEN-LAST:event_useLoadedFileActionPerformed

    private void generateTestDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateTestDataButtonActionPerformed
        // Generate some random test data.
        Random random = new Random();

        Settings.AutoLevelSettings autoLevelerSettings = this.settings.getAutoLevelSettings();
        for (Position p : scanner.getProbeStartPositions()) {
            Position probe = new Position(p);
            p.z = ((random.nextBoolean() ? -1 : 1) * random.nextFloat()) + getValue(this.zSurface);
            scanner.probeEvent(p);
        }
    }//GEN-LAST:event_generateTestDataButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyToGcode;
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
    private javax.swing.ButtonGroup unitGroup;
    private javax.swing.JRadioButton unitInch;
    private javax.swing.JRadioButton unitMM;
    private javax.swing.JButton useLoadedFile;
    private javax.swing.JLabel xLabel;
    private javax.swing.JSpinner xMax;
    private javax.swing.JSpinner xMin;
    private javax.swing.JLabel yLabel;
    private javax.swing.JSpinner yMax;
    private javax.swing.JSpinner yMin;
    private javax.swing.JLabel zLabel;
    private javax.swing.JSpinner zMax;
    private javax.swing.JSpinner zMin;
    private javax.swing.JSpinner zSurface;
    private javax.swing.JLabel zSurfaceLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        GUIHelpers.displayHelpDialog(Localization.getString("experimental.feature"));
        scanner = new SurfaceScanner(Units.MM);
        if (r == null) {
            r = new AutoLevelPreview(Localization.getString("platform.visualizer.renderable.autolevel-preview"));
            RenderableUtils.registerRenderable(r);
        }

    }

    @Override
    public void componentClosed() {
        RenderableUtils.removeRenderable(r);
        r = null;
    }

    void writeProperties(java.util.Properties p) {
    }

    void readProperties(java.util.Properties p) {
    }
}
