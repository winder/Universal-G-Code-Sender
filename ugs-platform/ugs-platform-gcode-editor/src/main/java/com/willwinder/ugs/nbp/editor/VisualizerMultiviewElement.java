package com.willwinder.ugs.nbp.editor;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbm.visualizer.RendererInputHandler;
import com.willwinder.ugs.nbm.visualizer.actions.CameraResetPreset;
import com.willwinder.ugs.nbm.visualizer.actions.CameraXPreset;
import com.willwinder.ugs.nbm.visualizer.actions.CameraYPreset;
import com.willwinder.ugs.nbm.visualizer.actions.CameraZPreset;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.io.File;
import java.util.prefs.Preferences;

@MultiViewElement.Registration(
        displayName = "#LBL_Gcode_Visualiser",
        iconBase = "com/willwinder/ugs/nbp/editor/edit.png",
        mimeType = "text/xgcode",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "Gcode",
        position = 1001
)
@NbBundle.Messages("LBL_Gcode_Visualiser=Visualiser")
public class VisualizerMultiviewElement extends JPanel implements MultiViewElement {
    private final BackendAPI backend;
    private final Lookup lookup;
    private JToolBar toolbar = new JToolBar();
    private GLJPanel panel;
    private RendererInputHandler rih;
    private MultiViewElementCallback multiViewElementCallback;

    public VisualizerMultiviewElement(Lookup lookup) {
        this.lookup = lookup;
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        toolbar.add(new JToolBar.Separator());
        toolbar.add(new CameraResetPreset());
        toolbar.add(new CameraXPreset());
        toolbar.add(new CameraYPreset());
        toolbar.add(new CameraZPreset());
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        //if (multiViewElementCallback == null) {
            return lookup;
        //}
        //return multiViewElementCallback.getTopComponent().getLookup();
    }

    @Override
    public void componentOpened() {
        panel = makeWindow();
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void componentClosed() {
        if (rih != null) {
            backend.removeControllerListener(rih);
            backend.removeUGSEventListener(rih);
        }

        if (panel == null) {
            return;
        }

        remove(panel);
        //dispose of panel and native resources
        panel.destroy();
        panel = null;
    }

    @Override
    public void componentShowing() {

    }

    @Override
    public void componentHidden() {

    }

    @Override
    public void componentActivated() {
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

    @Override
    public void componentDeactivated() {

    }


    @Override
    public UndoRedo getUndoRedo() {
        return null;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
        this.multiViewElementCallback = multiViewElementCallback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private GLJPanel makeWindow() {
        GLCapabilities glCaps = new GLCapabilities(null);
        final GLJPanel p = new GLJPanel(glCaps);

        GcodeRenderer renderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        if (renderer == null) {
            throw new IllegalArgumentException("Failed to access GcodeRenderer.");
        }

        FPSAnimator animator = new FPSAnimator(p, 15);
        this.rih = new RendererInputHandler(renderer, animator, backend);

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
