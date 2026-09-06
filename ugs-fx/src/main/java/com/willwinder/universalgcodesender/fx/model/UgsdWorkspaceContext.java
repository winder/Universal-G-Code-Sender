package com.willwinder.universalgcodesender.fx.model;

import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.fx.service.DesignGcodeService;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Optional;

/**
 * A design workspace. Opening it loads the design into the designer controller and generates its
 * G-code; from then on a {@link DesignGcodeService} keeps the G-code in step with every change,
 * so the toolpath is always current without saving.
 */
public class UgsdWorkspaceContext extends WorkspaceContext {
    public static final String FILE_EXTENSION = "ugsd";
    private DesignGcodeService gcodeService;

    public UgsdWorkspaceContext(File file) {
        super(file);
    }

    @Override
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public void open() {
        try {
            Controller controller = ControllerFactory.getController();
            if (file == null) {
                // A brand new design that has not been saved yet - start from an empty drawing.
                controller.newDrawing();
            } else {
                if (!file.exists()) {
                    throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
                }
                Design read = new UgsDesignReader().read(file).orElseThrow();
                controller.setDesign(read);
            }
            String name = file != null ? file.getName() : "untitled";
            gcodeService = new DesignGcodeService(controller, LookupService.lookup(BackendAPI.class), name);
            gcodeService.bind();
            gcodeService.regenerateAsync();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a valid design file: " + (file != null ? file.getAbsolutePath() : "untitled"), e);
        }
    }

    @Override
    public void close() {
        if (gcodeService != null) {
            gcodeService.unbind();
            gcodeService = null;
        }
    }

    /**
     * Whether the design's G-code is being regenerated; always false before the workspace is
     * opened or after it is closed.
     */
    public ReadOnlyBooleanProperty gcodeBusyProperty() {
        return gcodeService != null ? gcodeService.busyProperty() : new SimpleBooleanProperty(false);
    }

    /**
     * The workspace size is derived from the design drawing itself, i.e. the bounding box of the
     * drawn entities, rather than from the generated gcode.
     */
    @Override
    public Optional<WorkspaceBounds> getBounds() {
        Controller controller = ControllerFactory.getController();
        Rectangle2D bounds = controller.getModel().getRootEntity().getBounds();
        if (bounds == null || bounds.isEmpty()) {
            return Optional.empty();
        }
        double minX = Math.min(bounds.getMinX(), 0);
        double minY = Math.min(bounds.getMinY(), 0);
        double maxX = Math.max(bounds.getMaxX(), 0);
        double maxY = Math.max(bounds.getMaxY(), 0);
        return Optional.of(new WorkspaceBounds(minX, minY, maxX, maxY));
    }
}
