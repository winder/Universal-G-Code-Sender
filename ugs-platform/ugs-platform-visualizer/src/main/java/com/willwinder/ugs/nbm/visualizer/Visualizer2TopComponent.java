/**
 * Setup JOGL canvas, GcodeRenderer and RendererInputHandler.
 */

/*
    Copyright 2015-2017 Will Winder

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

import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.google.common.eventbus.EventBus;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.BorderLayout;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import java.util.prefs.Preferences;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbp.lib.eventbus.HighlightEventBus;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;
import java.io.File;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbPreferences;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "Visualizer2TopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "visualizer", openAtStartup = true)
@ActionID(category = LocalizingService.VisualizerCategory, id = LocalizingService.VisualizerActionId)
@ActionReference(path = LocalizingService.VisualizerWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:VisualizerTopComponent>",
        preferredID = "VisualizerTopComponent"
)
public final class Visualizer2TopComponent extends TopComponent {
    static GLCapabilities glCaps;

    private GLJPanel panel;
    private GcodeRenderer renderer;
    private FPSAnimator animator;
    private RendererInputHandler rih;
    private final BackendAPI backend;
    
    public Visualizer2TopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        glCaps = new GLCapabilities(null);

        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
    }

    @Override
    protected void componentOpened() {
        setName(LocalizingService.VisualizerTitle);
        setToolTipText(LocalizingService.VisualizerTooltip);
        super.componentOpened();
        panel = makeWindow(glCaps);
        add(panel, BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
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
    
    private GLJPanel makeWindow(final GLCapabilities caps) {
        final GLJPanel p = new GLJPanel(caps);

        renderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        if (renderer == null) {
            throw new IllegalArgumentException("Failed to access GcodeRenderer.");
        }
        
        animator = new FPSAnimator(p, 15);
        this.rih = new RendererInputHandler(renderer, animator,
                new VisualizerPopupMenu(backend),
                backend.getSettings());

        Preferences pref = NbPreferences.forModule(VisualizerOptionsPanel.class);
        pref.addPreferenceChangeListener(this.rih);

        File f = (backend.getProcessedGcodeFile() != null) ?
                backend.getProcessedGcodeFile() : backend.getGcodeFile();
        if (f != null) {
            this.rih.setGcodeFile(f.getAbsolutePath());
        }

        // Install listeners...

        EventBus eb = Lookup.getDefault().lookup(HighlightEventBus.class);
        if (eb != null) {
            eb.register(this.rih);
        }

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

        p.addGLEventListener((GLEventListener) renderer);

        return p;
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
