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

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.platform.surfacescanner.MeshLevelManager;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.ugs.platform.surfacescanner.Utils;
import com.willwinder.ugs.platform.surfacescanner.actions.ScanSurfaceAction;
import com.willwinder.ugs.platform.surfacescanner.actions.ToggleApplyToGcodeAction;
import com.willwinder.ugs.platform.surfacescanner.actions.TogglePreviewAction;
import com.willwinder.ugs.platform.surfacescanner.actions.UpdateMinMaxFromGcode;
import com.willwinder.ugs.platform.surfacescanner.renderable.AutoLevelPreview;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.PercentSpinner;
import com.willwinder.universalgcodesender.uielements.components.Spinner;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoLevelerPanel extends JPanel {
    public final Set<AutoLevelPanelListener> listeners = ConcurrentHashMap.newKeySet();
    private final transient SurfaceScanner surfaceScanner;
    private final transient MeshLevelManager meshLevelManager;
    private final transient AutoLevelPreview autoLevelPreview;
    private final AutoLevelSettings autoLevelSettings = new AutoLevelSettings();
    private Spinner stepResolution;
    private Spinner xMax;
    private Spinner xMin;
    private Spinner yMax;
    private Spinner yMin;
    private Spinner zMax;
    private Spinner zMin;
    private PercentSpinner zRetract;
    private Spinner zSurface;

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
        xMin = new Spinner(autoLevelSettings.getMinX());
        yMin = new Spinner(autoLevelSettings.getMinY());
        zMin = new Spinner(autoLevelSettings.getMinZ());
        xMax = new Spinner(autoLevelSettings.getMaxX());
        yMax = new Spinner(autoLevelSettings.getMaxY());
        zMax = new Spinner(autoLevelSettings.getMaxZ());

        stepResolution = new Spinner(autoLevelSettings.getStepResolution());
        zSurface = new Spinner(autoLevelSettings.getZSurface());
        zRetract = new PercentSpinner(autoLevelSettings.getZRetract(), 0.001);
        zRetract.setToolTipText(Localization.getString("autoleveler.panel.z-retract.tooltip"));

        JLabel minLabel = new JLabel(Localization.getString("autoleveler.panel.min"), SwingConstants.LEFT);
        JLabel maxLabel = new JLabel(Localization.getString("autoleveler.panel.max"), SwingConstants.LEFT);
        JLabel xLabel = new JLabel(Localization.getString("machineStatus.pin.x") + ':', SwingConstants.RIGHT);
        JLabel yLabel = new JLabel(Localization.getString("machineStatus.pin.y") + ':', SwingConstants.RIGHT);
        JLabel zLabel = new JLabel(Localization.getString("machineStatus.pin.z") + ':', SwingConstants.RIGHT);
        JLabel resolutionLabel = new JLabel(Localization.getString("autoleveler.panel.resolution") + ':', SwingConstants.RIGHT);
        JLabel zSurfaceLabel = new JLabel(Localization.getString("autoleveler.panel.z-surface") + ':', SwingConstants.RIGHT);
        JLabel zRetractLabel = new JLabel(Localization.getString("autoleveler.panel.z-retract") + ':', SwingConstants.RIGHT);

        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new MigLayout("fill", "[shrink][80:80, sg 1][80:80, sg 1]"));

        jPanel1.add(minLabel, "skip");
        jPanel1.add(maxLabel, "wrap");

        jPanel1.add(xLabel, "growx");
        jPanel1.add(xMin, "growx");
        jPanel1.add(xMax, "growx, wrap");

        jPanel1.add(yLabel, "growx");
        jPanel1.add(yMin, "growx");
        jPanel1.add(yMax, "growx, wrap");

        jPanel1.add(zLabel, "growx");
        jPanel1.add(zMin, "growx");
        jPanel1.add(zMax, "growx, wrap");
        jPanel1.add(new JButton(new UpdateMinMaxFromGcode(surfaceScanner)), "skip, spanx 2, growx");

        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new MigLayout("fill", "[shrink][80:80, sg1]"));
        jPanel2.add(new JLabel(" "), "growx, spanx, wrap");
        jPanel2.add(resolutionLabel, "growx");
        jPanel2.add(stepResolution, "growx, wrap");
        jPanel2.add(zSurfaceLabel, "growx");
        jPanel2.add(zSurface, "growx, wrap");
        jPanel2.add(zRetractLabel, "growx");
        jPanel2.add(zRetract, "growx, wrap");

        JPanel jPanel3 = new JPanel(new MigLayout("fill"));
        jPanel3.add(new JLabel(" "), "growx, spanx, wrap");
        jPanel3.add(new JButton(new ScanSurfaceAction(surfaceScanner)), "growx, wrap");
        jPanel3.add(new JLabel(" "), "growx, spanx, wrap");
        jPanel3.add(new JCheckBox(new TogglePreviewAction(autoLevelPreview)), "growx, wrap");
        jPanel3.add(new JCheckBox(new ToggleApplyToGcodeAction(meshLevelManager)), "growx, wrap");

        setLayout(new MigLayout("fill"));

        add(jPanel1, "growx, center, top");
        add(jPanel2, "growx, center, top");
        add(new JSeparator(SwingConstants.VERTICAL), "center, growy");
        add(jPanel3, "growx, center, top");
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
        AutoLevelSettings newSettings = copySettings();

        if (!Utils.removeProbeData(surfaceScanner)) {
            return;
        }

        autoLevelSettings.apply(newSettings);
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
        newSettings.setZSurface(zSurface.getDoubleValue());
        newSettings.setStepResolution(stepResolution.getDoubleValue());
        newSettings.setMinX(xMin.getDoubleValue());
        newSettings.setMinY(yMin.getDoubleValue());
        newSettings.setMinZ(zMin.getDoubleValue());
        newSettings.setMaxX(xMax.getDoubleValue());
        newSettings.setMaxY(yMax.getDoubleValue());
        newSettings.setMaxZ(zMax.getDoubleValue());
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
        if (stepResolution.getDoubleValue() != autoLevelSettings.getStepResolution()) {
            stepResolution.setValue(autoLevelSettings.getStepResolution());
        }

        if (zRetract.getDoubleValue() != autoLevelSettings.getZRetract()) {
            zRetract.setValue(autoLevelSettings.getZRetract());
        }

        if (zSurface.getDoubleValue() != autoLevelSettings.getZSurface()) {
            zSurface.setValue(autoLevelSettings.getZSurface());
        }

        if (xMin.getDoubleValue() != autoLevelSettings.getMinX()) {
            xMin.getModel().setValue(autoLevelSettings.getMinX());
        }

        if (yMin.getDoubleValue() != autoLevelSettings.getMinY()) {
            yMin.getModel().setValue(autoLevelSettings.getMinY());
        }

        if (zMin.getDoubleValue() != autoLevelSettings.getMinZ()) {
            zMin.getModel().setValue(autoLevelSettings.getMinZ());
        }

        if (xMax.getDoubleValue() != autoLevelSettings.getMaxX()) {
            xMax.getModel().setValue(autoLevelSettings.getMaxX());
        }

        if (yMax.getDoubleValue() != autoLevelSettings.getMaxY()) {
            yMax.getModel().setValue(autoLevelSettings.getMaxY());
        }

        if (zMax.getDoubleValue() != autoLevelSettings.getMaxZ()) {
            zMax.getModel().setValue(autoLevelSettings.getMaxZ());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
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
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        return new Position(xMin.getDoubleValue(), yMin.getDoubleValue(), zMin.getDoubleValue(), backendAPI.getSettings().getPreferredUnits());
    }

    public Position getMaxPosition() {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        return new Position(xMax.getDoubleValue(), yMax.getDoubleValue(), zMax.getDoubleValue(), backendAPI.getSettings().getPreferredUnits());
    }

    public void setUnits(UnitUtils.Units units) {
        double stepSize = units == UnitUtils.Units.MM ? 0.1 : 0.01;

        stepResolution.setStepSize(stepSize);
        xMin.setStepSize(stepSize);
        xMax.setStepSize(stepSize);
        yMin.setStepSize(stepSize);
        yMax.setStepSize(stepSize);
        zMin.setStepSize(stepSize);
        zMax.setStepSize(stepSize);
        zSurface.setStepSize(stepSize);
    }
}
