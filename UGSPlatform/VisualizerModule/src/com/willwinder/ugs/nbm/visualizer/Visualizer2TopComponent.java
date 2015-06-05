/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BackendAPIReadOnly;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.InputEvent;
import javax.media.opengl.GLCapabilities;
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
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_Visualizer2Action",
        preferredID = "Visualizer2TopComponent"
)
@Messages({
    "CTL_Visualizer2Action=Visualizer2",
    "CTL_Visualizer2TopComponent=Visualizer2 Window",
    "HINT_Visualizer2TopComponent=This is a Visualizer2 window"
})
public final class Visualizer2TopComponent extends TopComponent {
    static GLCapabilities glCaps;
    int quad_x = 5;
    int quad_y = 5;
    private NewtCanvasAWT canvas;
    private GcodeRenderer renderer;
    
    private BackendAPIReadOnly backend;
    
    public Visualizer2TopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        glCaps = new GLCapabilities(null);

        setMinimumSize(new java.awt.Dimension(320, 240));
        setPreferredSize(new java.awt.Dimension(640, 480));
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
        window.addGLEventListener(renderer);

        final FPSAnimator animator = new FPSAnimator(window, 60);
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
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


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
