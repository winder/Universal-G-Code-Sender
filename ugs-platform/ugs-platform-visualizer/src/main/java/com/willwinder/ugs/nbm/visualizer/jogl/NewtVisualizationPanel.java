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

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbm.visualizer.RendererInputHandler;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_NEWT_SAMPLES;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbp.core.actions.OpenLogDirectoryAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A visualization rendering panel which uses JOGL NEWT
 *
 * @author Joacim Breiler
 */
public class NewtVisualizationPanel extends JPanel {
    private static final ScheduledExecutorService UPDATE_SIZE_SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final Logger LOGGER = Logger.getLogger(NewtVisualizationPanel.class.getName());
    private final transient BackendAPI backend;
    private NewtCanvasAWT panel;
    private transient RendererInputHandler rih;
    private transient GLWindow glWindow;

    public NewtVisualizationPanel() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        setLayout(new BorderLayout());
        add(initializeVisualizationPanel(), BorderLayout.CENTER);
    }

    private Component initializeVisualizationPanel() {
        try {
            panel = makeWindow();
            return panel;
        } catch (GLException exception) {
            JLabel errorMessage = new JLabel("<html>Could not initialize OpenGL visualization, please check the log file for details <a href='#'>messages.log</a></html>", SwingConstants.CENTER);
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

    private NewtCanvasAWT makeWindow() throws GLException {
        GLCapabilities glCaps = new GLCapabilities(null);
        int numberOfSamples = VisualizerOptions.getIntegerOption(VISUALIZER_OPTION_NEWT_SAMPLES, 4);
        glCaps.setSampleBuffers(numberOfSamples > 0);
        glCaps.setNumSamples(numberOfSamples);
        glCaps.setDoubleBuffered(true);
        glCaps.setHardwareAccelerated(true);
        glCaps.setAlphaBits(8);
        glCaps.setOnscreen(true);

        glWindow = GLWindow.create(glCaps);
        if (glWindow.isSurfaceLockedByOtherThread()) {
            Thread surfaceLockOwner = glWindow.getSurfaceLockOwner();
            LOGGER.warning("Surface locked by another thread (" + surfaceLockOwner + "), attempting to unlock");
            glWindow.unlockSurface();
        }

        GcodeRenderer renderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        if (renderer == null) {
            throw new IllegalArgumentException("Failed to access GcodeRenderer.");
        }

        FPSAnimator animator = new FPSAnimator(glWindow, 60);
        this.rih = new RendererInputHandler(renderer, animator, backend, 30, 60);

        Preferences pref = NbPreferences.forModule(VisualizerOptionsPanel.class);
        pref.addPreferenceChangeListener(this.rih);

        File f = (backend.getProcessedGcodeFile() != null) ? backend.getProcessedGcodeFile() : backend.getGcodeFile();
        if (f != null) {
            this.rih.setGcodeFile(f.getAbsolutePath());
        }

        // Install listeners...
        backend.addUGSEventListener(this.rih);
        glWindow.addGLEventListener(renderer);

        NewtCanvasAWT p = new NewtCanvasAWT(glWindow);
        p.setShallUseOffscreenLayer(true);
        p.setBackground(Color.BLACK);

        p.setIgnoreRepaint(true);
        glWindow.setSurfaceScale(new float[]{ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE});

        // Workaround for linux, register listeners after the window has been created
        ThreadHelper.invokeLater(() -> {
            glWindow.addMouseListener(new NewtMouseListenerAdapter(p, this.rih, this.rih, this.rih));
            glWindow.addKeyListener(new NewtKeyboardListenerAdapter(p, this.rih));
            glWindow.addWindowListener(new com.jogamp.newt.event.WindowAdapter() {
                @Override
                public void windowResized(final com.jogamp.newt.event.WindowEvent e) {
                    resize();
                }
            });
        }, 500);
        return p;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        resize();
    }

    private void resize() {
        UPDATE_SIZE_SCHEDULER.execute(() -> {
            if (glWindow.isVisible()) {
                glWindow.setPosition(0, 0);
                glWindow.setSize(getWidth(), getHeight());
            }
        });
    }
}
