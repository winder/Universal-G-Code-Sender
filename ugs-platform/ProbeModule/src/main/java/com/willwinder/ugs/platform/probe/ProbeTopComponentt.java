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

package com.willwinder.ugs.platform.probe;

import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.platform.probe.AbstractProbeService.ProbeContext;
import com.willwinder.ugs.platform.probe.renderable.CornerProbePathPreview;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.platform.probe//CornerProbeTopComponent//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CornerProbeTopComponentTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(
        category = "Window",
        id = LocalizingService.ProbeActionId)
@ActionReference(path = LocalizingService.PLUGIN_WINDOW)
@TopComponent.OpenActionRegistration(
        displayName = "Probe",
        preferredID = "CornerProbeTopComponentTopComponent"
)
public final class ProbeTopComponentt extends TopComponent implements UGSEventListener {
    private CornerProbePathPreview preview = null;

    private static final String X_DISTANCE = Localization.getString("autoleveler.option.offset-x") + ":";
    private static final String Y_DISTANCE = Localization.getString("autoleveler.option.offset-y") + ":";
    private static final String Z_DISTANCE = Localization.getString("autoleveler.option.offset-z") + ":";
    private static final String X_OFFSET = "X Offset:";
    private static final String Y_OFFSET = Localization.getString("autoleveler.option.offset-z") + ":";
    private static final String Z_OFFSET = Localization.getString("probe.plate-thickness") + ":";

    // outside tab
    private SpinnerNumberModel outsideXDistanceModel;
    private SpinnerNumberModel outsideYDistanceModel;
    private SpinnerNumberModel outsideXOffsetModel;
    private SpinnerNumberModel outsideYOffsetModel;
    private final JButton measureOutside = new JButton(Localization.getString("probe.measure.outside-corner"));

    // z-probe tab
    private final SpinnerNumberModel zProbeDistance;
    private final SpinnerNumberModel zProbeOffset;
    private final JButton  zProbeButton = new JButton(Localization.getString("probe.button"));


    // inside tab
    private SpinnerNumberModel insideXDistanceModel;
    private SpinnerNumberModel insideYDistanceModel;
    private SpinnerNumberModel insideXOffsetModel;
    private SpinnerNumberModel insideYOffsetModel;
    private final JButton measureInside = new JButton(Localization.getString("probe.measure.inside-corner"));

    // settings
    private JComboBox settingsWorkCoordinate;
    private JComboBox settingsUnits;
    private SpinnerNumberModel settingsProbeDiameter;
    private SpinnerNumberModel settingsFastFindRate;
    private SpinnerNumberModel settingsSlowMeasureRate;
    private SpinnerNumberModel settingsRetractAmount;


    private final JButton settings1 = new JButton("Settings");
    private final JButton settings2 = new JButton("Settings");

    private final ProbeService2 ps2;
    private final BackendAPI backend;

    public ProbeTopComponentt() {
        setName(LocalizingService.ProbeHelperTitle);
        setToolTipText(LocalizingService.ProbeHelperTooltip);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        ps2 = new ProbeService2(backend);

        double largeSpinner = 1000000;
        // TODO: Initialize from settings.
        // OUTSIDE TAB
        outsideXDistanceModel = new SpinnerNumberModel(10., -largeSpinner, largeSpinner, 0.1);
        outsideYDistanceModel = new SpinnerNumberModel(10., -largeSpinner, largeSpinner, 0.1);
        outsideXOffsetModel = new SpinnerNumberModel(2., -largeSpinner, largeSpinner, 0.1);
        outsideYOffsetModel = new SpinnerNumberModel(2., -largeSpinner, largeSpinner, 0.1);

        // Z PROBE TAB
        zProbeDistance = new SpinnerNumberModel(10., -largeSpinner, largeSpinner, 0.1);
        zProbeOffset = new SpinnerNumberModel(10., -largeSpinner, largeSpinner, 0.1);

        // INSIDE TAB
        insideXDistanceModel = new SpinnerNumberModel(10., -largeSpinner, largeSpinner, 0.1);
        insideYDistanceModel = new SpinnerNumberModel(10., -largeSpinner, largeSpinner, 0.1);
        insideXOffsetModel = new SpinnerNumberModel(2., -largeSpinner, largeSpinner, 0.1);
        insideYOffsetModel = new SpinnerNumberModel(2., -largeSpinner, largeSpinner, 0.1);

        // SETTINGS TAB
        settingsWorkCoordinate = new JComboBox(new String[]{"G54", "G55", "G56", "G57", "G58", "G59"});
        settingsUnits = new JComboBox(new String[]{
            Localization.getString("mainWindow.swing.mmRadioButton"),
            Localization.getString("mainWindow.swing.inchRadioButton")
        });
        settingsProbeDiameter = new SpinnerNumberModel(10., 0., largeSpinner, 0.1);
        settingsFastFindRate = new SpinnerNumberModel(250., 1, largeSpinner, 1.);
        settingsSlowMeasureRate = new SpinnerNumberModel(100., 1, largeSpinner, 1.);
        settingsRetractAmount = new SpinnerNumberModel(15., 10, largeSpinner, 1.);

        measureOutside.addActionListener((e) -> {
            ProbeContext pc = new AbstractProbeService.ProbeContext(
                1, backend.getMachinePosition(),
                get(outsideXDistanceModel), get(outsideYDistanceModel), 100., 1);
                ps2.performOutsideCornerProbe(pc);
            });

        measureInside.addActionListener((e) -> {
            ProbeContext pc = new AbstractProbeService.ProbeContext(
                1, backend.getMachinePosition(),
                get(insideXDistanceModel), get(insideYDistanceModel), 100., 1);
                ps2.performInsideCornerProbe(pc);
            });

        initComponents();
        updateControls();

        this.outsideXDistanceModel.addChangeListener(l -> controlChangeListener());
        this.outsideYDistanceModel.addChangeListener(l -> controlChangeListener());
        this.insideXDistanceModel.addChangeListener(l -> controlChangeListener());
        this.insideYDistanceModel.addChangeListener(l -> controlChangeListener());

        this.zProbeDistance.addChangeListener(l -> controlChangeListener());
        this.zProbeOffset.addChangeListener(l -> controlChangeListener());

        this.outsideXOffsetModel.addChangeListener(l -> controlChangeListener());
        this.outsideYOffsetModel.addChangeListener(l -> controlChangeListener());
        this.insideXOffsetModel.addChangeListener(l -> controlChangeListener());
        this.insideYOffsetModel.addChangeListener(l -> controlChangeListener());
    }

    private void controlChangeListener() {
        if (preview != null) {
            // if (tab == corner)
            this.preview.updateSpacing(get(outsideXDistanceModel), get(outsideYDistanceModel),
                    get(outsideXOffsetModel), get(outsideYOffsetModel));
            // else if (tab == z)
            //        this.zPreview.update
        }
    }

    public void updateControls() {
        boolean enabled = backend.isIdle();
        this.measureInside.setEnabled(enabled);
        this.measureOutside.setEnabled(enabled);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    // deal with casting the spinner model to a double.
    private static double get(SpinnerNumberModel model) {
        return (double) model.getValue();
    }

    private void initComponents() {
        //setMinimumSize(new Dimension(500, 230));
        //setPreferredSize(new Dimension(500, 240));

        JPanel inside = new JPanel(new MigLayout("flowy, wrap 2"));
        inside.add(new JLabel(X_DISTANCE));
        inside.add(new JLabel(Y_DISTANCE));
        inside.add(new JSpinner(insideXDistanceModel), "growx");
        inside.add(new JSpinner(insideYDistanceModel), "growx");

        inside.add(new JLabel(X_OFFSET));
        inside.add(new JLabel(Y_OFFSET));
        inside.add(new JSpinner(insideXOffsetModel), "growx");
        inside.add(new JSpinner(insideYOffsetModel), "growx");

        inside.add(measureInside, "spanx 2, spany 2, growx, growy");

        // OUTSIDE TAB
        JPanel outside = new JPanel(new MigLayout("flowy, wrap 2"));
        outside.add(new JLabel(X_DISTANCE));
        outside.add(new JLabel(Y_DISTANCE));
        outside.add(new JSpinner(outsideXDistanceModel), "growx");
        outside.add(new JSpinner(outsideYDistanceModel), "growx");

        outside.add(new JLabel(X_OFFSET));
        outside.add(new JLabel(Y_OFFSET));
        outside.add(new JSpinner(outsideXOffsetModel), "growx");
        outside.add(new JSpinner(outsideYOffsetModel), "growx");

        outside.add(measureOutside, "spanx 2, spany 2, growx, growy");

        // Z PROBE TAB
        JPanel z = new JPanel(new MigLayout("wrap 4"));
        z.add(new JLabel(Localization.getString("probe.plate-thickness")));
        z.add(new JSpinner(this.zProbeDistance), "growx");

        z.add(this.zProbeButton, "spanx 2, spany 2, growx, growy");

        z.add(new JLabel(Localization.getString("probe.probe-distance")));
        z.add(new JSpinner(this.zProbeDistance), "growx");
        
        // SETTINGS TAB
        JPanel settings = new JPanel(new MigLayout("wrap 6"));
        settings.add(new JLabel(Localization.getString("probe.units") + ":"), "al right");
        settings.add(settingsUnits, "growx");

        settings.add(new JLabel(Localization.getString("probe.endmill-radius") + ":"), "al right");
        settings.add(new JSpinner(settingsProbeDiameter), "growx");

        settings.add(new JLabel(Localization.getString("probe.find-rate") + ":"), "al right");
        settings.add(new JSpinner(settingsFastFindRate), "growx");

        settings.add(new JLabel("Work Coordinates:"), "al right");
        settings.add(settingsWorkCoordinate, "growx");

        settings.add(new JLabel(Localization.getString("probe.measure-rate") + ":"), "al right");
        settings.add(new JSpinner(settingsSlowMeasureRate), "growx");

        settings.add(new JLabel(Localization.getString("probe.retract-amount") + ":"), "al right");
        settings.add(new JSpinner(settingsRetractAmount), "growx");

        JTabbedPane jtp = new JTabbedPane(JTabbedPane.LEFT);
        jtp.add("XY", outside);
        jtp.add("Z", z);
        //jtp.add("inside", inside);
        jtp.add("settings", settings);

        this.setLayout(new BorderLayout());
        this.add(jtp);
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        if (this.preview == null) {
            // if (tab == corner)
            this.preview = new CornerProbePathPreview(Localization.getString("probe.visualizer.corner-preview"));
            this.controlChangeListener();
            RenderableUtils.registerRenderable(this.preview);
        }
        // if (tab == z)
        //    this.zpreview = ...
    }

    @Override
    public void componentClosed() {
        RenderableUtils.removeRenderable(this.preview);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
