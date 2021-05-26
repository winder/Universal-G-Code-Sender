/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.ugs.nbp.designer;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.gcode.SimpleGcodeRouter;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gui.DrawingContainer;
import com.willwinder.ugs.nbp.designer.gui.SelectionSettings;
import com.willwinder.ugs.nbp.designer.gui.ToolBox;
import com.willwinder.ugs.nbp.designer.gui.controls.Control;
import com.willwinder.ugs.nbp.designer.io.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.platform.UndoManagerAdapter;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@MultiViewElement.Registration(
        displayName = "#platform.window.designer",
        iconBase = "com/willwinder/ugs/nbp/designer/edit.png",
        mimeType = {"application/x-ugs", "application/x-svg"},
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "DesignerMultiviewElement",
        position = 1000
)
public class DesignerMultiviewElement extends JPanel implements MultiViewElement, SelectionListener {

    private static final long serialVersionUID = 0;
    private final Lookup lookup;
    private final File file;
    private MultiViewElementCallback callback;
    private SelectionSettings selectionSettings;
    private Controller controller;
    private DrawingContainer drawingContainer;
    private ToolBox tools;

    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2);
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 100, TimeUnit.MILLISECONDS, workQueue);
    private UndoManagerAdapter undoManager;

    public DesignerMultiviewElement(Lookup lookup) {
        this.lookup = lookup;
        MultiDataObject obj = lookup.lookup(MultiDataObject.class);
        assert obj != null;
        file = FileUtil.toFile(obj.getPrimaryFile());
        initialize();

        if (file != null && StringUtils.endsWithIgnoreCase(file.getName(), "svg")) {
            SvgReader svgReader = new SvgReader();
            svgReader.read(file).ifPresent(entity -> {
                controller.getDrawing().insertEntity(entity);
                controller.getDrawing().repaint();
            });
        } else {
            controller.newDrawing();
        }

        getActionMap().put("delete", new DeleteAction());
        getActionMap().put("select-all", new SelectAllAction());
    }

    /**
     * Constructs a new graphical user interface for the program and shows it.
     */
    public void initialize() {

        setLayout(new BorderLayout());

        UndoManager undoManager = new SimpleUndoManager();
        CentralLookup.getDefault().add(undoManager);

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelectionListener(this);
        CentralLookup.getDefault().add(selectionManager);

        controller = new Controller();
        CentralLookup.getDefault().add(controller);

        this.undoManager = new UndoManagerAdapter(controller.getUndoManager());

        tools = new ToolBox();
        selectionSettings = new SelectionSettings(controller);

        drawingContainer = new DrawingContainer(controller);
        controller.addListener(drawingContainer);

        add(tools, BorderLayout.WEST);
        add(drawingContainer, BorderLayout.CENTER);
        add(selectionSettings, BorderLayout.EAST);

        controller.newDrawing();
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return null;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void componentOpened() {

    }

    @Override
    public void componentClosed() {

    }

    @Override
    public void componentShowing() {

    }

    @Override
    public void componentHidden() {

    }

    @Override
    public void componentActivated() {

    }

    @Override
    public void componentDeactivated() {

    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoManager;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        if (file != null) {
            callback.getTopComponent().setDisplayName(file.getName());
        } else {
            callback.getTopComponent().setDisplayName("Unnamed drawing");
        }
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        generateGcode();
    }

    private void generateGcode() {

        if (executor.getActiveCount() > 1) {
            return;
        }

        executor.execute(() -> {
            SimpleGcodeRouter gcodeRouter = new SimpleGcodeRouter();

            AffineTransform affineTransform = AffineTransform.getScaleInstance(1, -1);
            affineTransform.translate(0, -controller.getDrawing().getHeight());

            List<String> collect = controller.getDrawing().getEntities().stream().map(shape -> {
                if (shape instanceof Control) {
                    return "";
                }

                try {
                    GcodePath gcodePath = gcodeRouter.toPath(shape, affineTransform);

                    /*if (shape.getCutSettings().getCutType() == CutType.POCKET) {
                        SimplePocket simplePocket = new SimplePocket(gcodePath);
                        gcodePath = simplePocket.toGcodePath();

                        SimpleOutline simpleOutline = new SimpleOutline(gcodePath);
                        simpleOutline.setDepth(shape.getCutSettings().getDepth());
                        gcodePath = simpleOutline.toGcodePath();
                    }*/

                    return gcodeRouter.toGcode(gcodePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }).collect(Collectors.toList());

            String gcode = String.join("\n", collect);

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

}
