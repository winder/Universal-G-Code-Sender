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

import com.willwinder.ugs.nbp.designer.actions.UndoManagerListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.PopupMenuFactory;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.text.DataEditorSupport;
import org.openide.windows.TopComponent;

import java.awt.BorderLayout;
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
    private static DrawingContainer drawingContainer;
    private final transient UndoManagerAdapter undoManagerAdapter;
    private final transient BackendAPI backend;
    private final transient Controller controller;
    private final UgsDataObject dataObject;

    public DesignerTopComponent(UgsDataObject dataObject) {
        super();
        this.dataObject = dataObject;
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        controller = ControllerFactory.getController();

        // We need to reuse the drawing container for each loaded file
        if (drawingContainer == null) {
            drawingContainer = new DrawingContainer(controller);
        }

        undoManagerAdapter = new UndoManagerAdapter(controller.getUndoManager());
        controller.getSelectionManager().addSelectionListener(this);
        controller.getDrawing().setComponentPopupMenu(PopupMenuFactory.createPopupMenu());
        setActivatedNodes(new DataNode[]{new DataNode(dataObject, Children.LEAF, dataObject.getLookup())});
        dataObject.addPropertyChangeListener(evt -> updateFilename());
        loadDesign(dataObject);
        updateFilename();
        PlatformUtils.registerActions(getActionMap(), this);
    }

    private void loadDesign(UgsDataObject dataObject) {
        try {
            File file = new File(dataObject.getPrimaryFile().getPath());
            if (file.exists()) {
                controller.loadFile(file);
            }
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
        try {
            backend.unsetGcodeFile();
        } catch (Exception e) {
            // Never mind
        }
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        PlatformUtils.registerActions(getActionMap(), this);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        PlatformUtils.openSettings(controller);
        PlatformUtils.openEntitesTree(controller);
    }


    @Override
    public UndoRedo getUndoRedo() {
        return undoManagerAdapter;
    }

    @Override
    public void onChanged() {
        dataObject.setModified(true);

    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        controller.getDrawing().repaint();
        PlatformUtils.openSettings(controller);
        PlatformUtils.openEntitesTree(controller);
        requestActive();
    }
}
