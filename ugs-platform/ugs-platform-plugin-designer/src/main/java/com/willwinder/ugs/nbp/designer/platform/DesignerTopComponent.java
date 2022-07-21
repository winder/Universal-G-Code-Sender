/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.platform;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.Throttler;
import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.actions.UndoManagerListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.PopupMenuFactory;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.text.DataEditorSupport;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.io.File;
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
    private final transient Throttler refreshThrottler;
    private final transient UndoManagerAdapter undoManagerAdapter;
    private final transient BackendAPI backend;
    private transient Controller controller;
    private final UgsDataObject dataObject;
    private static DrawingContainer drawingContainer;

    public DesignerTopComponent(UgsDataObject dataObject) {
        super();
        this.dataObject = dataObject;
        refreshThrottler = new Throttler(this::generateGcode, 1000);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        controller = CentralLookup.getDefault().lookup(Controller.class);
        if (controller == null) {
            controller = new Controller(new SelectionManager(), new SimpleUndoManager());
            CentralLookup.getDefault().add(controller);
            CentralLookup.getDefault().add(controller.getUndoManager());
            CentralLookup.getDefault().add(controller.getSelectionManager());
        }

        // We need to reuse the drawing container for each loaded file
        if (drawingContainer == null) {
            drawingContainer = new DrawingContainer(controller);
        }

        undoManagerAdapter = new UndoManagerAdapter(controller.getUndoManager());
        controller.getSelectionManager().addSelectionListener(this);
        controller.getDrawing().setComponentPopupMenu(new PopupMenuFactory().createPopupMenu(controller));
        setActivatedNodes(new DataNode[]{new DataNode(dataObject, Children.LEAF, dataObject.getLookup())});
        dataObject.addPropertyChangeListener(evt -> updateFilename());
        loadDesign(dataObject);
        updateFilename();
        PlatformUtils.registerActions(getActionMap(), controller, this);
    }

    private void loadDesign(UgsDataObject dataObject) {
        Design design = new Design();
        try {
            File file = new File(dataObject.getPrimaryFile().getPath());
            if (file.exists()) {
                UgsDesignReader reader = new UgsDesignReader();
                design = reader.read(file).orElse(design);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't load design from file " + dataObject.getPrimaryFile(), e);
        }
        controller.setDesign(design);
    }

    private void updateFilename() {
        setDisplayName(DataEditorSupport.annotateName(dataObject.getName(), false, dataObject.isModified(), false));
        setHtmlDisplayName(DataEditorSupport.annotateName(dataObject.getName(), true, dataObject.isModified(), false));
        setToolTipText(DataEditorSupport.toolTip(dataObject.getPrimaryFile(), dataObject.isModified(), false));
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        removeAll();
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());

        ToolBox toolbox = new ToolBox(controller);
        add(toolbox, BorderLayout.NORTH);
        add(drawingContainer, BorderLayout.CENTER);

        setVisible(true);

        controller.getUndoManager().addListener(this);
        controller.getDrawing().repaint();
        refreshThrottler.run();
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
        controller.release();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        PlatformUtils.registerActions(getActionMap(), controller, this);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        PlatformUtils.openSettings(controller);
        PlatformUtils.openEntitesTree(controller);
    }

    private void generateGcode() {
        DesignWriter designWriter = new GcodeDesignWriter();
        try {
            File file = new File(Files.createTempDir(), dataObject.getName() + ".gcode");
            designWriter.write(file, controller);
            backend.setGcodeFile(file);
        } catch (Exception e) {
            throw new RuntimeException("Could not generate gcode");
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoManagerAdapter;
    }

    @Override
    public void onChanged() {
        dataObject.setModified(true);
        if (!backend.isSendingFile()) {
            refreshThrottler.run();
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        controller.getDrawing().repaint();
        PlatformUtils.openSettings(controller);
        PlatformUtils.openEntitesTree(controller);
        requestActive();
    }
}
