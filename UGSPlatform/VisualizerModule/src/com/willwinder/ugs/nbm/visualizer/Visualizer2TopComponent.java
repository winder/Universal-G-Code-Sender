/*
    Copywrite 2015 Will Winder

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

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
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
    int quad_x = 5;
    int quad_y = 5;
    private NewtCanvasAWT canvas;
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
        canvas = makeWindow("TestWindow", glCaps);
        add(canvas, BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        remove(canvas);
        //dispose of canvas and native resources
        canvas.destroy();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        if (canvas != null) {
            canvas.setSize(getSize());
            //need to update complete component tree
            invalidate();
            
            if (getTopLevelAncestor() != null) {
                getTopLevelAncestor().invalidate();
                getTopLevelAncestor().revalidate();
            }
        }
    }

    private NewtCanvasAWT makeWindow(
        final String name, final GLCapabilities caps) {

        final GLWindow window = GLWindow.create(caps);
        renderer = new GcodeRenderer();
        
        if (backend.getFile() != null)
            renderer.setGcodeFile(backend.getFile().getAbsolutePath());
        
        window.setTitle(name);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(
                final WindowEvent e) {
                // System.exit(0);
            }
        });
        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseEvent me) {
                float[] rotation = me.getRotation();
                
                renderer.mouseZoom(Math.round(me.getRotation()[1]));
            }
            
            @Override
            public void mouseDragged(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();
                
                int panMouseButton = InputEvent.BUTTON2_MASK; // TODO: Make configurable

                if (me.isShiftDown() || me.getModifiers() == panMouseButton) {
                    renderer.mousePan(new Point(x,y));
                } else {
                    renderer.mouseRotate(new Point(x,y));
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();

                renderer.mouseMoved(new Point(x, y));
            }
        });
        window.addGLEventListener((GLEventListener) renderer);

        animator = new FPSAnimator(window, 60);
        animator.start();
        NewtCanvasAWT canvas = new NewtCanvasAWT(window);
        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                quad_x = e.getX();
                quad_y = window.getHeight() - e.getY();
            }
        });
        return canvas;
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
                    renderer.setGcodeFile(backend.getFile().getAbsolutePath());
                    invalidate();
                    animator.resume();
                }
        }
    }
}
