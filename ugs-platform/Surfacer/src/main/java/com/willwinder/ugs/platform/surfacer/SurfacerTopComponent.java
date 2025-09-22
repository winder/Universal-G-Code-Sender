/*
 * Copyright (C) 2025 dimic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.platform.surfacer;

import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.SwingHelpers;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;


/**
 * Top component which displays something.
 */

 @TopComponent.Description(
        preferredID = "SurfacerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = Mode.OUTPUT, openAtStartup = false)

@ActionID(category = LocalizingService.CATEGORY_WINDOW, id = "com.willwinder.ugp.tools.SurfacerTopComponent")
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SurfacerAction",
        preferredID = "SurfacerTopComponent"
)
@Messages({
    "CTL_SurfacerAction=Surfacer",
    "CTL_SurfacerTopComponent=Surfacer",
    "HINT_SurfacerTopComponent=This is a Surfacer window"
})
public final class SurfacerTopComponent extends TopComponent implements UGSEventListener {

    private static final double DEPTH_MAX = -0.0001;
    private static final double CUT_STEP_MAX = -0.0001;
    private static final double ANGLE_MIN = 0;
    private static final double ANGLE_MAX = 90;
     
    private static final Prefs prefs = new Prefs();
    private static final Gcode gcode = new Gcode();
    private transient final BackendAPI backend;
    
    private TextFieldUnit units = TextFieldUnit.MM;
    private TextFieldUnit rateUnits = TextFieldUnit.MM_PER_MINUTE;
    
    public SurfacerTopComponent() {
        initComponents();
       
        setName(Bundle.CTL_SurfacerTopComponent());
        setToolTipText(Bundle.HINT_SurfacerTopComponent());
        
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        gcode.init(prefs, backend);
        
        lVersion.setText("v0.30");

        updateControls();
        initListeners();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent) {
        } else if (evt instanceof SettingChangedEvent) {
            updateControls();
        }
    }
    
    private double mmToUnits(double mmvalue) {
        return (units == TextFieldUnit.MM) ? mmvalue : mmvalue / 25.4;
    }

    private double unitsToMM(double value) {
        return (units == TextFieldUnit.MM) ? value : value * 25.4;
    }

    private void updateToUnitsDouble(UnitSpinner spinner, String key) {
        double value = mmToUnits(prefs.getDouble(key));  // this has to be called before updating the spinner
        if (prefs.isRateKey(key)) {
            spinner.setUnits(rateUnits);
        } else {
            spinner.setUnits(units);
        }
        spinner.setValue(value);
    }

    private void updateDouble(UnitSpinner spinner, String key) {
        double value = prefs.getDouble(key);
        spinner.setValue(value);
    }
    
    private void updateControls() {
        units = backend.getSettings().getPreferredUnits() == UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;
        rateUnits = (units == TextFieldUnit.MM) ? TextFieldUnit.MM_PER_MINUTE : TextFieldUnit.INCHES_PER_MINUTE;

        cbPattern.setSelectedIndex(prefs.getInt(Prefs.KEY_PATTERN));
        setPatternControls((String)cbPattern.getSelectedItem());
        updateDouble(sAngle, Prefs.KEY_ANGLE);
        if (prefs.getBoolean(Prefs.KEY_CLIMB_CUT))
        {
            rbClimbCut.setSelected(true);
        } else {
            rbConventionalCut.setSelected(true);
        }
        
        updateToUnitsDouble(sX0, Prefs.KEY_X0);
        updateToUnitsDouble(sY0, Prefs.KEY_Y0);
        updateToUnitsDouble(sX1, Prefs.KEY_X1);
        updateToUnitsDouble(sY1, Prefs.KEY_Y1);

        updateToUnitsDouble(sZSafe, Prefs.KEY_Z_SAFE);
        updateToUnitsDouble(sZStart, Prefs.KEY_Z_START);
        updateToUnitsDouble(sDepth, Prefs.KEY_DEPTH);
        updateToUnitsDouble(sCutStep, Prefs.KEY_CUT_STEP);
        updateToUnitsDouble(sPlungeRate, Prefs.KEY_PLUNGE_RATE);
        updateToUnitsDouble(sXYFeedrate, Prefs.KEY_XY_FEEDRATE);

        updateToUnitsDouble(sFinishCut, Prefs.KEY_FINISH_CUT);
        sFinishCount.setValue(prefs.getInt(Prefs.KEY_FINISH_COUNT));
        updateToUnitsDouble(sFinishFeedrate, Prefs.KEY_FINISH_FEEDRATE);
        
        updateToUnitsDouble(sToolDiameter, Prefs.KEY_TOOL_DIAMETER);
        updateDouble(sOverlap, Prefs.KEY_OVERLAP);
        updateDouble(sSpindleSpeed, Prefs.KEY_SPINDLE_SPEED);
    }    
    
    private void setPatternControls(String item) {
        if (item.toLowerCase().contains("raster")) {
            sAngle.setEnabled(true);
        } else {
            sAngle.setEnabled(false);
        }
    }
    
    private void initListeners() {
        backend.addUGSEventListener(this);

        cbPattern.addActionListener(l -> { changePatternSetting(cbPattern.getSelectedIndex()); });
        sAngle.addChangeListener(l -> {
            if (sAngle.getDoubleValue() > ANGLE_MAX) sAngle.setValue(ANGLE_MAX);
            if (sAngle.getDoubleValue() < ANGLE_MIN) sAngle.setValue(ANGLE_MIN);
            changeSetting(Prefs.KEY_ANGLE, sAngle.getDoubleValue());
        });
        
        sX0.addChangeListener(l -> changeSetting(Prefs.KEY_X0, sX0.getDoubleValue()));
        sY0.addChangeListener(l -> changeSetting(Prefs.KEY_Y0, sY0.getDoubleValue()));
        sX1.addChangeListener(l -> changeSetting(Prefs.KEY_X1, sX1.getDoubleValue()));
        sY1.addChangeListener(l -> changeSetting(Prefs.KEY_Y1, sY1.getDoubleValue()));

        
        sZSafe.addChangeListener(l -> changeSetting(Prefs.KEY_Z_SAFE, sZSafe.getDoubleValue()));
        sZStart.addChangeListener(l -> changeSetting(Prefs.KEY_Z_START, sZStart.getDoubleValue()));
        sDepth.addChangeListener(l -> {
            if (sDepth.getDoubleValue() > DEPTH_MAX) sDepth.setValue(DEPTH_MAX);
            changeSetting(Prefs.KEY_DEPTH, sDepth.getDoubleValue());
        });
        sCutStep.addChangeListener(l -> {
            if (sCutStep.getDoubleValue() > CUT_STEP_MAX) sCutStep.setValue(CUT_STEP_MAX);
            changeSetting(Prefs.KEY_CUT_STEP, sCutStep.getDoubleValue());
        });
        sPlungeRate.addChangeListener(l -> changeSetting(Prefs.KEY_PLUNGE_RATE, sPlungeRate.getDoubleValue()));
        sXYFeedrate.addChangeListener(l -> changeSetting(Prefs.KEY_XY_FEEDRATE, sXYFeedrate.getDoubleValue()));

        sFinishCut.addChangeListener(l -> {
            if (sFinishCut.getDoubleValue() > CUT_STEP_MAX) sFinishCut.setValue(CUT_STEP_MAX);
            changeSetting(Prefs.KEY_FINISH_CUT, sFinishCut.getDoubleValue());
        });
        sFinishCount.addChangeListener(l -> changeSetting(Prefs.KEY_FINISH_COUNT, (int)sFinishCount.getValue()));
        sFinishFeedrate.addChangeListener(l -> changeSetting(Prefs.KEY_FINISH_FEEDRATE, sFinishFeedrate.getDoubleValue()));
        
        sToolDiameter.addChangeListener(l -> changeSetting(Prefs.KEY_TOOL_DIAMETER, sToolDiameter.getDoubleValue()));
        sOverlap.addChangeListener(l -> changeSetting(Prefs.KEY_OVERLAP, sOverlap.getDoubleValue()));
        sSpindleSpeed.addChangeListener(l -> changeSetting(Prefs.KEY_SPINDLE_SPEED, sSpindleSpeed.getDoubleValue()));

        bGenerate.addActionListener(l -> generate());
        bExport.addActionListener(l -> export());
    }

    private void changePatternSetting(int new_index) {
        setPatternControls((String)cbPattern.getSelectedItem());
        changeSetting(Prefs.KEY_PATTERN, new_index);
    }
    
    private void changeSetting(String key, int value) { prefs.putInt(key, value); }
    private void changeSetting(String key, boolean value) { prefs.putBoolean(key, value); }
    private void changeSetting(String key, double value) {
        if (prefs.isConvertKey(key)) {
            prefs.putDouble(key, unitsToMM(value));
        } else {
            prefs.putDouble(key, value);
        }
    }

    private void generate() {
        gcode.generate(null);
        GUIHelpers.openGcodeFile(gcode.getFile(), backend);
    }
    
    private void export() {
        String sourceDir = backend.getSettings().getLastOpenedFilename();
        SwingHelpers
            .createFile(sourceDir)
            .ifPresent(file -> {
                gcode.generate(file);
                GUIHelpers.openGcodeFile(gcode.getFile(), backend);
            });
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgClimbCut = new javax.swing.ButtonGroup();
        lVersion = new javax.swing.JLabel();
        bGenerate = new javax.swing.JButton();
        jTabs = new javax.swing.JTabbedPane();
        pnlGeometry = new javax.swing.JPanel();
        sX0 = new UnitSpinner(0, units, null, null, 0.1d);
        sY0 = new UnitSpinner(0, units, null, null, 0.1d);
        sX1 = new UnitSpinner(0, units, null, null, 0.1d);
        sY1 = new UnitSpinner(0, units, null, null, 0.1d);
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbPattern = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        sAngle = new UnitSpinner(0, TextFieldUnit.DEGREE, 0d, 90d, 1d);
        rbClimbCut = new javax.swing.JRadioButton();
        rbConventionalCut = new javax.swing.JRadioButton();
        pnlZ = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        sCutStep = new UnitSpinner(CUT_STEP_MAX, units, null, CUT_STEP_MAX, 0.1d);
        sDepth = new UnitSpinner(DEPTH_MAX, units, null, DEPTH_MAX, 0.1d);
        sZStart = new UnitSpinner(0, units, null, null, 0.1d);
        sZSafe = new UnitSpinner(0, units, null, null, 0.1d);
        jLabel10 = new javax.swing.JLabel();
        sPlungeRate = new UnitSpinner(0, rateUnits, 0d, null, 1d);
        pnlFinish = new javax.swing.JPanel();
        sFinishCut = new UnitSpinner(CUT_STEP_MAX, units, null, CUT_STEP_MAX, 0.1d);
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        sFinishCount = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        sFinishFeedrate = new UnitSpinner(0, rateUnits, 0d, null, 1d);
        pnlTool = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        sToolDiameter = new UnitSpinner(0, units, 0.d, null, 0.1d);
        jLabel6 = new javax.swing.JLabel();
        sOverlap = new UnitSpinner(0.3, TextFieldUnit.PERCENT, 0.d, 0.99d, 0.01d);
        sXYFeedrate = new UnitSpinner(0, rateUnits, 0d, null, 1d);
        jLabel14 = new javax.swing.JLabel();
        sSpindleSpeed = new UnitSpinner(10000, TextFieldUnit.ROTATIONS_PER_MINUTE, 0.d, 10000.d, 100.d);
        jLabel15 = new javax.swing.JLabel();
        bExport = new javax.swing.JButton();

        lVersion.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lVersion.setForeground(new java.awt.Color(153, 153, 153));
        org.openide.awt.Mnemonics.setLocalizedText(lVersion, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.lVersion.text_1")); // NOI18N
        lVersion.setAlignmentY(0.0F);

        org.openide.awt.Mnemonics.setLocalizedText(bGenerate, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.bGenerate.text")); // NOI18N
        bGenerate.setToolTipText(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.bGenerate.toolTipText")); // NOI18N
        bGenerate.setActionCommand(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.bGenerate.actionCommand")); // NOI18N

        jTabs.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        sX0.setMinimumSize(new java.awt.Dimension(120, 22));

        sY0.setMinimumSize(new java.awt.Dimension(120, 22));

        sX1.setMinimumSize(new java.awt.Dimension(120, 22));

        sY1.setMinimumSize(new java.awt.Dimension(120, 22));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel2.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel5.text")); // NOI18N

        cbPattern.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Raster", "Spiral" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel13.text")); // NOI18N

        bgClimbCut.add(rbClimbCut);
        org.openide.awt.Mnemonics.setLocalizedText(rbClimbCut, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.rbClimbCut.text")); // NOI18N
        rbClimbCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbClimbCutActionPerformed(evt);
            }
        });

        bgClimbCut.add(rbConventionalCut);
        org.openide.awt.Mnemonics.setLocalizedText(rbConventionalCut, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.rbConventionalCut.text")); // NOI18N
        rbConventionalCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbConventionalCutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlGeometryLayout = new javax.swing.GroupLayout(pnlGeometry);
        pnlGeometry.setLayout(pnlGeometryLayout);
        pnlGeometryLayout.setHorizontalGroup(
            pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGeometryLayout.createSequentialGroup()
                .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGeometryLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlGeometryLayout.createSequentialGroup()
                                .addComponent(rbClimbCut)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rbConventionalCut))))
                    .addGroup(pnlGeometryLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sX1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sX0, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sY0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sY1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlGeometryLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(308, Short.MAX_VALUE))
        );
        pnlGeometryLayout.setVerticalGroup(
            pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGeometryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbClimbCut)
                    .addComponent(rbConventionalCut))
                .addGap(18, 18, 18)
                .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sX0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sY0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGeometryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sX1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel2.AccessibleContext.accessibleName")); // NOI18N

        jTabs.addTab(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.pnlGeometry.TabConstraints.tabTitle"), pnlGeometry); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel10.text")); // NOI18N

        javax.swing.GroupLayout pnlZLayout = new javax.swing.GroupLayout(pnlZ);
        pnlZ.setLayout(pnlZLayout);
        pnlZLayout.setHorizontalGroup(
            pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlZLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlZLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlZLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sCutStep, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlZLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sDepth, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlZLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sZStart, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlZLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sZSafe, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlZLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(3, 3, 3)
                        .addComponent(sPlungeRate, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(436, Short.MAX_VALUE))
        );
        pnlZLayout.setVerticalGroup(
            pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlZLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sZSafe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sZStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(14, 14, 14)
                .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sCutStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(pnlZLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sPlungeRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jTabs.addTab(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.pnlZ.TabConstraints.tabTitle"), pnlZ); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel16.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel17.text")); // NOI18N

        sFinishCount.setEditor(new javax.swing.JSpinner.NumberEditor(sFinishCount, ""));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel19.text")); // NOI18N

        javax.swing.GroupLayout pnlFinishLayout = new javax.swing.GroupLayout(pnlFinish);
        pnlFinish.setLayout(pnlFinishLayout);
        pnlFinishLayout.setHorizontalGroup(
            pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFinishLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFinishLayout.createSequentialGroup()
                        .addGroup(pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sFinishFeedrate, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sFinishCount, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFinishLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sFinishCut, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(450, Short.MAX_VALUE))
        );
        pnlFinishLayout.setVerticalGroup(
            pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFinishLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sFinishCut, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(6, 6, 6)
                .addGroup(pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sFinishCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFinishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sFinishFeedrate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addContainerGap(100, Short.MAX_VALUE))
        );

        jTabs.addTab(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.pnlFinish.TabConstraints.tabTitle"), pnlFinish); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel1.text")); // NOI18N

        sToolDiameter.setMinimumSize(new java.awt.Dimension(120, 22));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel6.text")); // NOI18N

        sOverlap.setMinimumSize(new java.awt.Dimension(120, 22));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel14.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.jLabel15.text")); // NOI18N

        javax.swing.GroupLayout pnlToolLayout = new javax.swing.GroupLayout(pnlTool);
        pnlTool.setLayout(pnlToolLayout);
        pnlToolLayout.setHorizontalGroup(
            pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToolLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(sXYFeedrate)
                    .addComponent(sSpindleSpeed)
                    .addComponent(sOverlap, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sToolDiameter, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                .addContainerGap(420, Short.MAX_VALUE))
        );
        pnlToolLayout.setVerticalGroup(
            pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToolLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sToolDiameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sOverlap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sSpindleSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addGroup(pnlToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sXYFeedrate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addContainerGap(48, Short.MAX_VALUE))
        );

        jTabs.addTab(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.pnlTool.TabConstraints.tabTitle"), pnlTool); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bExport, org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.bExport.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bExport, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jTabs)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabs)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bGenerate)
                            .addComponent(bExport))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(lVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        lVersion.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.lVersion.AccessibleContext.accessibleName")); // NOI18N
        lVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SurfacerTopComponent.class, "SurfacerTopComponent.lVersion.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void rbConventionalCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbConventionalCutActionPerformed
        changeSetting(Prefs.KEY_CLIMB_CUT, false);// TODO add your handling code here:
    }//GEN-LAST:event_rbConventionalCutActionPerformed

    private void rbClimbCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbClimbCutActionPerformed
        changeSetting(Prefs.KEY_CLIMB_CUT, true);
    }//GEN-LAST:event_rbClimbCutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bExport;
    private javax.swing.JButton bGenerate;
    private javax.swing.ButtonGroup bgClimbCut;
    private javax.swing.JComboBox<String> cbPattern;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTabbedPane jTabs;
    private javax.swing.JLabel lVersion;
    private javax.swing.JPanel pnlFinish;
    private javax.swing.JPanel pnlGeometry;
    private javax.swing.JPanel pnlTool;
    private javax.swing.JPanel pnlZ;
    private javax.swing.JRadioButton rbClimbCut;
    private javax.swing.JRadioButton rbConventionalCut;
    private UnitSpinner sAngle;
    private UnitSpinner sCutStep;
    private UnitSpinner sDepth;
    private javax.swing.JSpinner sFinishCount;
    private UnitSpinner sFinishCut;
    private UnitSpinner sFinishFeedrate;
    private UnitSpinner sOverlap;
    private UnitSpinner sPlungeRate;
    private UnitSpinner sSpindleSpeed;
    private UnitSpinner sToolDiameter;
    private UnitSpinner sX0;
    private UnitSpinner sX1;
    private UnitSpinner sXYFeedrate;
    private UnitSpinner sY0;
    private UnitSpinner sY1;
    private UnitSpinner sZSafe;
    private UnitSpinner sZStart;
    // End of variables declaration//GEN-END:variables
}
