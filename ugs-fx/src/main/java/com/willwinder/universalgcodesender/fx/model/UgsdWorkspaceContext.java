package com.willwinder.universalgcodesender.fx.model;

import com.google.common.io.Files;
import com.willwinder.ugs.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;

import java.io.File;

public class UgsdWorkspaceContext extends WorkspaceContext {
    public UgsdWorkspaceContext(File file) {
        super(file);
    }

    @Override
    public void open() {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        try {
            UgsDesignReader reader = new UgsDesignReader();
            Design read = reader.read(file).orElseThrow();
            ControllerFactory.getController().setDesign(read);


            GcodeDesignWriter writer = new GcodeDesignWriter();
            File tempFile = new File(Files.createTempDir(), file.getName() + ".gcode");
            writer.write(tempFile, ControllerFactory.getController());

            BackendAPI backend = LookupService.lookup(BackendAPI.class);
            backend.setGcodeFile(tempFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a valid gcode file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public String getDisplayName() {
        return file.getName();
    }
}
