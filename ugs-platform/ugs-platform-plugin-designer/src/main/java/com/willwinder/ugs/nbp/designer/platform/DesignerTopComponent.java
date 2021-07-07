package com.willwinder.ugs.nbp.designer.platform;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.actions.UndoManagerListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.io.IOUtils;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.text.DataEditorSupport;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A designer component for editing vector graphics that will be converted to gcode.
 *
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "DesignerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public class DesignerTopComponent extends TopComponent implements UndoManagerListener, SelectionListener {
    private static final long serialVersionUID = 3123334398723987873L;
    private static final Logger LOGGER = Logger.getLogger(DesignerTopComponent.class.getSimpleName());
    private static final ArrayBlockingQueue<Runnable> JOB_QUEUE = new ArrayBlockingQueue<>(1);
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, JOB_QUEUE);

    private final transient UndoManagerAdapter undoManagerAdapter;
    private final transient BackendAPI backend;
    private transient Controller controller;
    private final UgsDataObject dataObject;

    public DesignerTopComponent(UgsDataObject dataObject) {
        this.dataObject = dataObject;
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        controller = CentralLookup.getDefault().lookup(Controller.class);
        if (controller == null) {
            controller = new Controller(new SelectionManager(), new SimpleUndoManager());
            CentralLookup.getDefault().add(controller);
            CentralLookup.getDefault().add(controller.getUndoManager());
            CentralLookup.getDefault().add(controller.getSelectionManager());
        }

        undoManagerAdapter = new UndoManagerAdapter(controller.getUndoManager());
        setActivatedNodes(new DataNode[]{new DataNode(dataObject, Children.LEAF, dataObject.getLookup())});
        dataObject.addPropertyChangeListener(evt -> updateFilename());
        loadDesign(dataObject);
        updateFilename();
    }

    private void loadDesign(UgsDataObject dataObject) {
        try {
            File file = new File(dataObject.getPrimaryFile().getPath());
            Design design = new Design();
            if (file.exists()) {
                UgsDesignReader reader = new UgsDesignReader();
                design = reader.read(file).orElse(design);
            }
            controller.setDesign(design);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't load design from file " + dataObject.getPrimaryFile(), e);
        }
    }

    private void updateFilename() {
        setDisplayName(DataEditorSupport.annotateName(dataObject.getName(), false, dataObject.isModified(), false));
        setHtmlDisplayName(DataEditorSupport.annotateName(dataObject.getName(), true, dataObject.isModified(), false));
        setToolTipText(DataEditorSupport.toolTip(dataObject.getPrimaryFile(), dataObject.isModified(), false));
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());

        PlatformUtils.openSettings(controller);
        PlatformUtils.openEntitesTree(controller);

        DrawingContainer drawingContainer = new DrawingContainer(controller);
        controller.addListener(drawingContainer);
        controller.getUndoManager().addListener(this);
        controller.getSelectionManager().addSelectionListener(this);

        add(new ToolBox(controller), BorderLayout.NORTH);
        add(drawingContainer, BorderLayout.CENTER);

        setVisible(true);

        controller.getDrawing().repaint();
        getActionMap().put("delete", new DeleteAction(controller));
        getActionMap().put("select-all", new SelectAllAction(controller));
        generateGcode();
    }

    @Override
    public boolean canClose() {
        CloseCookie closeCookie = dataObject.getCookie(CloseCookie.class);
        if (closeCookie != null) {
            return closeCookie.close();
        }

        return true;
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        controller.getUndoManager().removeListener(this);
    }

    private void generateGcode() {
        if (!JOB_QUEUE.isEmpty()) {
            return;
        }

        EXECUTOR.execute(() -> {
            String gcode = Utils.toGcode(controller, controller.getDrawing().getEntities());

            try {
                File file = new File(Files.createTempDir(), dataObject.getName() + ".gcode");
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
        dataObject.setModified(true);
        if (!backend.isSendingFile()) {
            generateGcode();
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        controller.getDrawing().repaint();
        PlatformUtils.openSettings(controller);
        PlatformUtils.openEntitesTree(controller);
    }
}
