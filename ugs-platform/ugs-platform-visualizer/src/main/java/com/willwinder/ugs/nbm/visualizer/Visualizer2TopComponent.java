/**
 * Setup JOGL canvas, GcodeRenderer and RendererInputHandler.
 */

/*
    Copywrite 2015-2016 Will Winder

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

import com.google.common.eventbus.EventBus;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BackendAPIReadOnly;
import java.awt.BorderLayout;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import java.util.prefs.Preferences;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbp.lib.eventbus.HighlightEventBus;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbm.visualizer//Visualizer2//EN",
        autostore = false
)
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
@Messages({
    "CTL_Visualizer2Action=Visualizer",
    "CTL_Visualizer2TopComponent=Visualizer",
    "HINT_Visualizer2TopComponent=This is the Visualizer"
})
public final class Visualizer2TopComponent extends TopComponent {
    static GLCapabilities glCaps;

    private GLJPanel panel;
    private GcodeRenderer renderer;
    private FPSAnimator animator;
    private final BackendAPIReadOnly backend;
    
    public Visualizer2TopComponent() {
        setName(LocalizingService.VisualizerTitle);
        setToolTipText(LocalizingService.VisualizerTooltip);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        glCaps = new GLCapabilities(null);

        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());

        setName(Bundle.CTL_Visualizer2TopComponent());
        setToolTipText(Bundle.HINT_Visualizer2TopComponent());
    }

    @Override
    protected void componentOpened() {
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

        renderer = new GcodeRenderer();
        
        animator = new FPSAnimator(p, 15);
        RendererInputHandler rih = new RendererInputHandler(renderer, animator);

        Preferences pref = NbPreferences.forModule(VisualizerOptionsPanel.class);
        pref.addPreferenceChangeListener(rih);

        if (backend.getProcessedGcodeFile() != null) {
            rih.setProcessedGcodeFile(backend.getProcessedGcodeFile().getAbsolutePath());
        } else if (backend.getGcodeFile() != null) {
            rih.setGcodeFile(backend.getGcodeFile().getAbsolutePath());
        }

        // Install listeners...

        EventBus eb = Lookup.getDefault().lookup(HighlightEventBus.class);
        if (eb != null) {
            eb.register(rih);
        }

        backend.addControllerListener(rih);
        backend.addUGSEventListener(rih);

        // shutdown hook...
        //frame.addWindowListener(rih);

        // key listener...
        p.addKeyListener(rih);

        // mouse wheel...
        p.addMouseWheelListener(rih);

        // mouse motion...
        p.addMouseMotionListener(rih);

        // mouse...
        p.addMouseListener(rih);

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
