package com.willwinder.universalgcodesender.fx.component.visualizer.machine.common;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class STLModel {
    /**
     * Reads an STL file from the class path
     *
     * @param resource the resource to read
     * @return a CSG with the read STL
     * @throws IOException if the resource couldn't be read
     * @throws URISyntaxException if the URI to the resource was wrong
     */
    public static CSG readSTL(String resource) throws IOException, URISyntaxException {

        // This is a workaround for reading STL:s from class path since the Path.of doesn't
        // handle jdk.zipfs very well
        try (InputStream in = STLModel.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + resource);
            }

            Path tempFile = Files.createTempFile("temp", "stl");
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return STL.file(Path.of(tempFile.toUri()));
        }
    }
}
