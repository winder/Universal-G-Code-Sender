package com.willwinder.universalgcodesender.fx.model;

import com.google.common.io.Files;
import com.willwinder.ugs.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Optional;

public class UgsdWorkspaceContext extends WorkspaceContext {
    public static final String FILE_EXTENSION = "ugsd";

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

            GcodeDesignWriter writer = new GcodeDesignWriter();
            String name = file != null ? file.getName() : "untitled";
            File tempFile = new File(Files.createTempDir(), name + ".gcode");
            writer.write(tempFile, controller);

            BackendAPI backend = LookupService.lookup(BackendAPI.class);
            backend.setGcodeFile(tempFile);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a valid design file: " + (file != null ? file.getAbsolutePath() : "untitled"), e);
        }
    }

    /**
     * The workspace size is derived from the design drawing itself, i.e. the bounding box of the
     * drawn entities, rather than from the generated gcode.
     */
    @Override
    public Optional<WorkspaceBounds> getBounds() {
        Controller controller = ControllerFactory.getController();
        Rectangle2D bounds = controller.getDrawing().getRootEntity().getBounds();
        if (bounds == null || bounds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new WorkspaceBounds(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
    }
}
