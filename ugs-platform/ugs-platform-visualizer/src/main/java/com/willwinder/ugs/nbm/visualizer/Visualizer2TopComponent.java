/*
    Copyright 2015-2018 Will Winder

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

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;

/**
 * Setup JOGL canvas, GcodeRenderer and RendererInputHandler.
 */
@TopComponent.Description(
        preferredID = "Visualizer2TopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "visualizer", openAtStartup = true)
@ActionID(category = Visualizer2TopComponent.VisualizerCategory, id = Visualizer2TopComponent.VisualizerActionId)
@ActionReference(path = Visualizer2TopComponent.VisualizerWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:VisualizerTopComponent>",
        preferredID = "VisualizerTopComponent"
)
public final class Visualizer2TopComponent extends TopComponent {

    private GLJPanel panel;
    private RendererInputHandler rih;
    private final BackendAPI backend;

    public final static String VisualizerTitle = Localization.getString("platform.window.visualizer", lang);
    public final static String VisualizerTooltip = Localization.getString("platform.window.visualizer.tooltip", lang);
    public final static String VisualizerWindowPath = LocalizingService.MENU_WINDOW;
    public final static String VisualizerActionId = "com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent";
    public final static String VisualizerCategory = LocalizingService.CATEGORY_WINDOW;

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
      public Localizer() {
        super(VisualizerCategory, VisualizerActionId, VisualizerTitle);
      }
    }

    public Visualizer2TopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
    }

    @Override
    protected void componentOpened() {
        setName(VisualizerTitle);
        setToolTipText(VisualizerTooltip);
        super.componentOpened();
        panel = makeWindow();
        add(panel, BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();

        if (rih != null) {
            backend.removeControllerListener(rih);
            backend.removeUGSEventListener(rih);
        }

        System.out.println("Component closed, panel = " + panel);
        if (panel == null) return;

        remove(panel);
        //dispose of panel and native resources
        panel.destroy();
        panel = null;
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        if (panel != null) {
            panel.setSize(getSize());
            //need to update complete component tree
            invalidate();
            
            if (getTopLevelAncestor() != null) {
                getTopLevelAncestor().invalidate();
                getTopLevelAncestor().revalidate();
            }
        }
    }
    
    private GLJPanel makeWindow() {
        GLCapabilities glCaps = new GLCapabilities(null);
        final GLJPanel p = new GLJPanel(glCaps);

        GcodeRenderer renderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        if (renderer == null) {
            throw new IllegalArgumentException("Failed to access GcodeRenderer.");
        }

        FPSAnimator animator = new FPSAnimator(p, 15);
        this.rih = new RendererInputHandler(renderer, animator,
                new VisualizerPopupMenu(backend, renderer),
                backend.getSettings());

        Preferences pref = NbPreferences.forModule(VisualizerOptionsPanel.class);
        pref.addPreferenceChangeListener(this.rih);

        File f = (backend.getProcessedGcodeFile() != null) ?
                backend.getProcessedGcodeFile() : backend.getGcodeFile();
        if (f != null) {
            this.rih.setGcodeFile(f.getAbsolutePath());
        }

        // Install listeners...
        backend.addControllerListener(this.rih);
        backend.addUGSEventListener(this.rih);

        // shutdown hook...
        //frame.addWindowListener(this.rih);

        // key listener...
        p.addKeyListener(this.rih);

        // mouse wheel...
        p.addMouseWheelListener(this.rih);

        // mouse motion...
        p.addMouseMotionListener(this.rih);

        // mouse...
        p.addMouseListener(this.rih);

        p.addGLEventListener(renderer);

        return p;
    }
}
