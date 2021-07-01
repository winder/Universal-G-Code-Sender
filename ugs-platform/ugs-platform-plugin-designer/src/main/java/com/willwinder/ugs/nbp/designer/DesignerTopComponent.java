package com.willwinder.ugs.nbp.designer;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManagerListener;
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
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@TopComponent.Description(
        preferredID = "DesignerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public class DesignerTopComponent extends CloneableTopComponent implements UndoManagerListener {

    private final SimpleUndoManager undoManager;
    private final SelectionManager selectionManager;
    private final ThreadPoolExecutor executor;
    private final Controller controller;
    private final UndoManagerAdapter undoManagerAdapter;
    private final ArrayBlockingQueue<Runnable> jobQueue;


    public DesignerTopComponent() {
        jobQueue = new ArrayBlockingQueue<>(1);
        executor = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, jobQueue);
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
        selectionManager.addSelectionListener(e -> {
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

        generateGcode();
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        undoManager.removeListener(this);
    }

    private void generateGcode() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        if (!jobQueue.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            String gcode = Utils.toGcode(controller, controller.getDrawing().getEntities());

            try {
                File file = new File(Files.createTempDir(), "_ugs_editor.gcode");
                FileWriter fileWriter = new FileWriter(file);
                IOUtils.write(gcode, fileWriter);
                fileWriter.close();
                backend.setGcodeFile(file);
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
