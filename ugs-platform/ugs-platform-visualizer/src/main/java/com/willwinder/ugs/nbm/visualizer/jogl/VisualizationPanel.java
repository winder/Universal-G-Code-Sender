/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbm.visualizer.RendererInputHandler;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbp.core.actions.OpenLogDirectoryAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class VisualizationPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(VisualizationPanel.class.getName());
    private final transient BackendAPI backend;

    private GLJPanel panel;
    private transient RendererInputHandler rih;

    public VisualizationPanel() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        setLayout(new BorderLayout());
        add(initializeVisualizationPanel(), BorderLayout.CENTER);
    }

    private JComponent initializeVisualizationPanel() {
        try {
            panel = makeWindow();
            return panel;
        } catch (GLException exception) {
            JLabel errorMessage = new JLabel("<html>Could not initialize OpenGL visualization, please check the log file for details <a href='#'>messages.log</a></html>", JLabel.CENTER);
            errorMessage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new OpenLogDirectoryAction().actionPerformed(null);
                }
            });
            return errorMessage;
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        LOGGER.log(Level.INFO, "Component closed, panel = " + panel);
        if (panel == null) return;

        remove(panel);

        LOGGER.info("Destroying panel");
        //dispose of panel and native resources
        panel.destroy();
        panel = null;

        LOGGER.info("Done destroying");

        backend.removeUGSEventListener(rih);
    }

    private GLJPanel makeWindow() throws GLException {
        GLCapabilities glCaps = new GLCapabilities(null);
        final GLJPanel p = new GLJPanel(glCaps);

        GcodeRenderer renderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        if (renderer == null) {
            throw new IllegalArgumentException("Failed to access GcodeRenderer.");
        }

        FPSAnimator animator = new FPSAnimator(p, 15);
        this.rih = new RendererInputHandler(renderer, animator, backend, 4, 15);

        Preferences pref = NbPreferences.forModule(VisualizerOptionsPanel.class);
        pref.addPreferenceChangeListener(this.rih);

        File f = (backend.getProcessedGcodeFile() != null) ?
                backend.getProcessedGcodeFile() : backend.getGcodeFile();
        if (f != null) {
            this.rih.setGcodeFile(f.getAbsolutePath());
        }

        // Install listeners...
        backend.addUGSEventListener(this.rih);

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
