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

package com.willwinder.ugs.platform.probe;

import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.ugs.platform.probe.renderable.HoleCenterPathPreview;
import com.willwinder.ugs.platform.probe.renderable.ProbePreviewManager;
import com.willwinder.ugs.platform.probe.renderable.XYProbePathPreview;
import com.willwinder.ugs.platform.probe.renderable.XYZProbePathPreview;
import com.willwinder.ugs.platform.probe.renderable.ZProbePathPreview;
import com.willwinder.ugs.platform.probe.ui.ProbeHoleCenterPanel;
import com.willwinder.ugs.platform.probe.ui.ProbeOutsideXYPanel;
import com.willwinder.ugs.platform.probe.ui.ProbeXYZPanel;
import com.willwinder.ugs.platform.probe.ui.ProbeZPanel;
import com.willwinder.ugs.platform.probe.ui.SettingsPanel;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = ProbeTopComponent.preferredId,
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = Mode.OUTPUT, openAtStartup = false)
@ActionID(
        category = ProbeTopComponent.ProbeCategory,
        id = ProbeTopComponent.ProbeActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "Probe",
        preferredID = ProbeTopComponent.preferredId
)
public final class ProbeTopComponent extends TopComponent implements UGSEventListener {
    public static final String preferredId = "AdvancedProbeTopComponent";
    public static final  String ProbeTitle = Localization.getString("platform.window.probe-module", lang);
    public static final  String ProbeTooltip = Localization.getString("platform.window.probe-module.tooltip", lang);
    public static final  String ProbeActionId = "com.willwinder.ugs.platform.probe.ProbeTopComponent.renamed";
    public static final  String ProbeCategory = LocalizingService.CATEGORY_WINDOW;
    // hole diameter tab
    private static final String HC_TAB = "Hole center";
    // xyz tab
    private static final String XYZ_TAB = "XYZ";
    // outside tab
    private static final String OUTSIDE_TAB = "XY";
    // z-probe tab
    private static final String Z_TAB = "Z";
    private static final String SETTINGS_TAB = "Settings";
    private transient final ProbePreviewManager probePreviewManager;
    private final JTabbedPane jtp = new JTabbedPane(SwingConstants.LEFT);
    private transient final BackendAPI backend;
    private ProbeHoleCenterPanel probeHoleCenterPanel;
    private ProbeXYZPanel probeXYZPanel;
    private ProbeOutsideXYPanel probeXYPanel;
    private ProbeZPanel probeZPanel;
    private SettingsPanel settingsPanel;

    public ProbeTopComponent() {
        setName(ProbeTitle);
        setToolTipText(ProbeTooltip);
        probePreviewManager = Lookup.getDefault().lookup(ProbePreviewManager.class);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        ProbeSettings.setSettingsUnits(backend.getSettings().getPreferredUnits());

        registerPreviews();
        initComponents();
        initListeners();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent) {
            probePreviewManager.updateSettings();

            boolean enablePanels = backend.isIdle() || !backend.isConnected();
            probeZPanel.setEnabled(enablePanels);
            probeXYPanel.setEnabled(enablePanels);
            probeXYZPanel.setEnabled(enablePanels);
            probeHoleCenterPanel.setEnabled(enablePanels);
            probeHoleCenterPanel.setEnabled(enablePanels);
            settingsPanel.setEnabled(enablePanels);
        } else if (evt instanceof SettingChangedEvent) {
            ProbeSettings.setSettingsUnits(backend.getSettings().getPreferredUnits());
        }
    }

    private void registerPreviews() {
        probePreviewManager.register(HC_TAB, new HoleCenterPathPreview());
        probePreviewManager.register(XYZ_TAB, new XYZProbePathPreview());
        probePreviewManager.register(OUTSIDE_TAB, new XYProbePathPreview());
        probePreviewManager.register(Z_TAB, new ZProbePathPreview());
    }

    private void initListeners() {
        jtp.addChangeListener(l -> controlChangeListener());
        ProbeSettings.addPreferenceChangeListener(e -> controlChangeListener());
        backend.addUGSEventListener(this);
    }

    private void controlChangeListener() {
        if (isShowing()) {
            ProbeSettings.setSelectedTabIdx(jtp.getSelectedIndex());
            probePreviewManager.setActive(jtp.getTitleAt(this.jtp.getSelectedIndex()));
        } else {
            probePreviewManager.inactivate();
        }
    }

    private void initComponents() {
        probeHoleCenterPanel = new ProbeHoleCenterPanel();
        probeXYZPanel = new ProbeXYZPanel();
        probeXYPanel = new ProbeOutsideXYPanel();
        probeZPanel = new ProbeZPanel();
        settingsPanel = new SettingsPanel();

        jtp.add(Z_TAB, probeZPanel);
        jtp.add(OUTSIDE_TAB, probeXYPanel);
        jtp.add(XYZ_TAB, probeXYZPanel);
        jtp.add(HC_TAB, probeHoleCenterPanel);
        jtp.add(SETTINGS_TAB, settingsPanel);
        jtp.setSelectedIndex(ProbeSettings.getSelectedTabIdx());

        this.setLayout(new BorderLayout());
        this.add(jtp);
    }

    public void selectSettingsTab() {
        // Select the settings tab (which is last)
        jtp.setSelectedIndex(jtp.getTabCount() - 1);
    }

    @Override
    public void componentOpened() {
        controlChangeListener();

        // Cleanup after renamed preferred ID.
        String id = WindowManager.getDefault().findTopComponentID(this);
        if (!StringUtils.equals(id, preferredId)) {
            this.close();
        }
    }

    @Override
    public void componentClosed() {
        controlChangeListener();
    }

    @Override
    protected void componentHidden() {
        controlChangeListener();
    }

    @Override
    protected void componentShowing() {
        controlChangeListener();
    }

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(ProbeCategory, ProbeActionId, ProbeTitle);
        }
    }
}
