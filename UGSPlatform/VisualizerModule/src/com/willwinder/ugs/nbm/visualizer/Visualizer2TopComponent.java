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

import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BackendAPIReadOnly;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.InputEvent;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

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
@ActionID(category = "Window", id = "com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_Visualizer2Action",
        preferredID = "Visualizer2TopComponent"
)
@Messages({
    "CTL_Visualizer2Action=Visualizer",
    "CTL_Visualizer2TopComponent=Visualizer",
    "HINT_Visualizer2TopComponent=This is the Visualizer"
})
public final class Visualizer2TopComponent extends TopComponent implements ControlStateListener {
    static GLCapabilities glCaps;

    private GLJPanel panel;
    private GcodeRenderer renderer;
    private FPSAnimator animator;
    private final BackendAPIReadOnly backend;
    
    public Visualizer2TopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addControlStateListener(this);
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
        panel = makeWindow("TestWindow", glCaps);
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
    
    private GLJPanel makeWindow(
        final String name, final GLCapabilities caps) {

        //final GLWindow window = GLWindow.create(caps);
        final GLJPanel p = new GLJPanel(caps);
        final JFrame frame = new JFrame(name);

        renderer = new GcodeRenderer();
        
        if (backend.getGcodeFile() != null)
            renderer.setGcodeFile(backend.getGcodeFile().getAbsolutePath());
        
        // Install a shutdown hook...
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowDeactivated(java.awt.event.WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        animator.stop();
                        componentClosed();
                    }
                }).start();
            }

            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
            }

            @Override
            public void windowIconified(java.awt.event.WindowEvent e) {
            }

            @Override
            public void windowDeiconified(java.awt.event.WindowEvent e) {
            }

            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
            }
        
        });

        // Mouse wheel...
        p.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getScrollAmount();
                renderer.mouseZoom(rotation);
            }
        });

        // Mouse motion...
        p.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int x = e.getX();
                    int y = e.getY();
                    
                    int panMouseButton = InputEvent.BUTTON2_MASK; // TODO: Make configurable

                    if (e.isShiftDown() || e.getModifiers() == panMouseButton) {
                        renderer.mousePan(new Point(x,y));
                    } else {
                        renderer.mouseRotate(new Point(x,y));
                    }
                }
            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                renderer.mouseMoved(new Point(x, y));
            }

        });

        p.addGLEventListener((GLEventListener) renderer);

        animator = new FPSAnimator(p, 15);
        animator.start();
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

    @Override
    public void ControlStateEvent(com.willwinder.universalgcodesender.model.ControlStateEvent cse) {
        switch (cse.getEventType()) {
            case FILE_CHANGED:
                if (renderer != null && animator != null) {
                    animator.pause();
                    renderer.setGcodeFile(backend.getGcodeFile().getAbsolutePath());
                    invalidate();
                    animator.resume();
                }
        }
    }
}
