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

import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.ugs.platform.surfacescanner.renderable.AutoLevelPreview;
import com.willwinder.ugs.platform.surfacescanner.ui.AutoLevelerPanel;
import com.willwinder.ugs.platform.surfacescanner.ui.AutoLevelerToolbar;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.*;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;

import java.awt.*;

import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;

@TopComponent.Description(preferredID = "AutoLevelerTopComponent")
@TopComponent.Registration(mode = Mode.OUTPUT, openAtStartup = false)
@ActionID(category = AutoLevelerTopComponent.AutoLevelerCategory, id = AutoLevelerTopComponent.AutoLevelerActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:AutoLevelerTopComponent>",
        preferredID = "AutoLevelerTopComponent"
)
public final class AutoLevelerTopComponent extends TopComponent implements UGSEventListener {
    public static final String AutoLevelerTitle = Localization.getString("platform.window.autoleveler", lang);
    public static final String AutoLevelerActionId = "com.willwinder.ugs.platform.surfacescanner.AutoLevelerTopComponent";
    public static final String AutoLevelerCategory = LocalizingService.CATEGORY_WINDOW;
    private transient BackendAPI backend;
    private transient AutoLevelPreview autoLevelPreview;
    private transient SurfaceScanner scanner;
    private transient MeshLevelManager meshLevelManager;
    private transient AutoLevelerPanel autoLevelerPanel;

    public AutoLevelerTopComponent() {
        setName(AutoLevelerTitle);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ProbeEvent) {
            try {
                scanner.handleEvent((ProbeEvent) evt);
            } catch (Exception e) {
                // TODO make this error message more descriptive
                e.printStackTrace();
                GUIHelpers.displayErrorDialog(Localization.getString("autoleveler.probe-failed"));
            } finally {
                updatePreview();
            }
        } else if (evt instanceof SettingChangedEvent) {
            autoLevelerPanel.setSettings(backend.getSettings().getAutoLevelSettings());
            autoLevelerPanel.setUnits(backend.getSettings().getPreferredUnits());
        } else if (evt instanceof FileStateEvent) {
            FileState fileState = ((FileStateEvent) evt).getFileState();
            if (fileState == FileState.OPENING_FILE) {
                // file open clears the backend CommandProcessor list
                // (despite what the javadoc for applyCommandProcessor would suggest)
                updatePreview();
            }
        } else if (evt instanceof ControllerStatusEvent) {
            boolean isIdle = (backend.isConnected() && backend.isIdle()) || !backend.isConnected();
            autoLevelerPanel.setEnabled(isIdle);
        }
    }

    @Override
    public void componentOpened() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        scanner = new SurfaceScanner(this.backend);
        scanner.addListener(this::updatePreview);

        meshLevelManager = new MeshLevelManager(scanner, backend);

        if (autoLevelPreview == null) {
            autoLevelPreview = new AutoLevelPreview(Localization.getString("platform.visualizer.renderable.autolevel-preview"));
        }

        initComponents();
        updateSettingsAndRefresh();
    }

    @Override
    public void componentClosed() {
        RenderableUtils.removeRenderable(autoLevelPreview);
        autoLevelPreview = null;
        meshLevelManager.clear();
        backend.removeUGSEventListener(this);
        removeAll();
    }

    @Override
    protected void componentHidden() {
        if (autoLevelPreview != null) {
            RenderableUtils.removeRenderable(autoLevelPreview);
        }
    }

    @Override
    protected void componentShowing() {
        if (autoLevelPreview != null) {
            RenderableUtils.registerRenderable(autoLevelPreview);
        }
    }

    private void updatePreview() {
        if (autoLevelPreview != null) {
            autoLevelPreview.updateSettings(
                    scanner.getProbeStartPositions(),
                    scanner.getProbePositionGrid()
            );
        }

        meshLevelManager.update();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {
        autoLevelerPanel = new AutoLevelerPanel(scanner, meshLevelManager, autoLevelPreview, backend.getSettings().getAutoLevelSettings(), backend.getSettings().getPreferredUnits());
        autoLevelerPanel.addAutoLevelerPanelListener(this::updateSettingsAndRefresh);

        this.setLayout(new BorderLayout());
        this.add(new AutoLevelerToolbar(scanner), BorderLayout.NORTH);
        this.add(autoLevelerPanel, BorderLayout.CENTER);
    }

    private void updateSettingsAndRefresh() {
        // Update the settings
        backend.getSettings().getAutoLevelSettings().apply(autoLevelerPanel.getSettings());
        scanner.update(autoLevelerPanel.getMinPosition(), autoLevelerPanel.getMaxPosition());
        scanner.reset();
        updatePreview();
    }

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(AutoLevelerCategory, AutoLevelerActionId, AutoLevelerTitle);
        }
    }
}
