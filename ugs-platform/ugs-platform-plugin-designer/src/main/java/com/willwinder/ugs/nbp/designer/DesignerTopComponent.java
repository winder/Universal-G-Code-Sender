package com.willwinder.ugs.nbp.designer;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.gcode.SimpleGcodeRouter;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManagerListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.platform.UndoManagerAdapter;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.io.IOUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@TopComponent.Description(
        preferredID = "DesignerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:DesignerTopComponent>",
        preferredID = "DesignerTopComponent"
)
@ActionID(category = "Window", id = "DesignerTopComponent")
@ActionReference(path = "Menu/Window")
public class DesignerTopComponent extends CloneableTopComponent implements UndoManagerListener {

    private final SimpleUndoManager undoManager;
    private final SelectionManager selectionManager;
    private final ThreadPoolExecutor executor;
    private final Controller controller;
    private final UndoManagerAdapter undoManagerAdapter;


    public DesignerTopComponent() {
        executor = new ThreadPoolExecutor(1, 1, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2));
        undoManager = new SimpleUndoManager();
        undoManagerAdapter = new UndoManagerAdapter(undoManager);
        selectionManager = new SelectionManager();
        controller = new Controller(selectionManager, undoManager);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());

        CentralLookup.getDefault().add(undoManager);
        CentralLookup.getDefault().add(selectionManager);

        Utils.openSettings(controller);

        DrawingContainer drawingContainer = new DrawingContainer(controller);
        controller.addListener(drawingContainer);
        selectionManager.addSelectionListener((e) -> {
            drawingContainer.repaint();
            Utils.openSettings(controller);
            Utils.openEntitesTree(controller);
        });

        // When something changed in drawing, rerender
        undoManager.addListener(this);

        add(new ToolBox(controller), BorderLayout.NORTH);
        add(drawingContainer, BorderLayout.CENTER);

        setVisible(true);

        controller.getDrawing().repaint();

        getActionMap().put("delete", new DeleteAction(controller));
        getActionMap().put("select-all", new SelectAllAction(controller));

    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        undoManager.removeListener(this);
    }

    private void generateGcode() {
        if (executor.getActiveCount() > 1) {
            return;
        }

        executor.execute(() -> {
            String gcode = Utils.toGcode(controller, controller.getDrawing().getEntities());

            try {
                File file = new File(Files.createTempDir(), "_ugs_editor.gcode");
                FileWriter fileWriter = new FileWriter(file);
                IOUtils.write(gcode, fileWriter);
                fileWriter.close();
                BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
                backend.setGcodeFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoManagerAdapter;
    }

    @Override
    public void onChanged() {
        generateGcode();
    }
}
