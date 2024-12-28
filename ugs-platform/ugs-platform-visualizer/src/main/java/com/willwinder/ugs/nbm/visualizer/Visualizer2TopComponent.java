/*
    Copyright 2015-2022 Will Winder

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
package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbm.visualizer.jogl.NewtVisualizationPanel;
import com.willwinder.ugs.nbm.visualizer.jogl.VisualizationPanel;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Setup JOGL canvas, GcodeRenderer and RendererInputHandler.
 */
@TopComponent.Description(
        preferredID = "VisualizerTopComponent"
)
@TopComponent.Registration(mode = com.willwinder.ugs.nbp.lib.Mode.EDITOR_SECONDARY, openAtStartup = true)
@ActionID(category = Visualizer2TopComponent.VisualizerCategory, id = Visualizer2TopComponent.VisualizerActionId)
@ActionReference(path = Visualizer2TopComponent.VisualizerWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:VisualizerTopComponent>",
        preferredID = "VisualizerTopComponent"
)
public final class Visualizer2TopComponent extends TopComponent {
    public final static String VisualizerTitle = Localization.getString("platform.window.visualizer", lang);
    public final static String VisualizerTooltip = Localization.getString("platform.window.visualizer.tooltip", lang);
    public final static String VisualizerWindowPath = LocalizingService.MENU_WINDOW;
    public final static String VisualizerActionId = "com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent";
    public final static String VisualizerCategory = LocalizingService.CATEGORY_WINDOW;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Visualizer2TopComponent GL Init"));

    public Visualizer2TopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
    }

    /**
     * Fixes for commit: dd68a4ef9fd211642f284024bb651fa9bf0be64c
     * 1. No longer using custom "visualizer" mode.
     */
    private void cleanup() {
        Mode mode = WindowManager.getDefault().findMode(this);
        if (mode != null && StringUtils.equals("visualizer", mode.getName())) {
            this.close();
        }
    }

    @Override
    protected void componentOpened() {
        cleanup();

        setName(VisualizerTitle);
        setToolTipText(VisualizerTooltip);
        super.componentOpened();

        removeAll();
        add(new VisualizerToolBar(), BorderLayout.NORTH);

        JPanel borderedPanel = new JPanel();
        borderedPanel.setLayout(new BorderLayout());
        borderedPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        if (VisualizerOptions.getBooleanOption(VisualizerOptions.VISUALIZER_OPTION_LEGACY, true)) {
            borderedPanel.add(new VisualizationPanel(), BorderLayout.CENTER);
        } else {
            createAndAddNewtPanel(borderedPanel);
        }
        add(borderedPanel, BorderLayout.CENTER);
    }

    private void createAndAddNewtPanel(JPanel borderedPanel) {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            executor.execute(() -> {
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        borderedPanel.add(new NewtVisualizationPanel(), BorderLayout.CENTER);
                        borderedPanel.revalidate();
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(VisualizerCategory, VisualizerActionId, VisualizerTitle);
        }
    }
}
