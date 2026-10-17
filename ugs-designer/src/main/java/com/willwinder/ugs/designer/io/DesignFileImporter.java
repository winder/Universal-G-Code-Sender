/*
    Copyright 2022-2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io;

import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.io.c2d.C2dReader;
import com.willwinder.ugs.designer.io.dxf.DxfReader;
import com.willwinder.ugs.designer.io.eagle.EaglePnpReader;
import com.willwinder.ugs.designer.io.excellon.ExcellonReader;
import com.willwinder.ugs.designer.io.gerber.GerberReader;
import com.willwinder.ugs.designer.io.kicad.KiCadPosReader;
import com.willwinder.ugs.designer.io.stl.StlReader;
import com.willwinder.ugs.designer.io.svg.SvgReader;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.utils.Settings;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Imports external files into a design. This is UI toolkit agnostic so that it can be shared between
 * the Swing and JavaFX front ends as well as drag'n'drop handlers.
 */
public class DesignFileImporter {

    /**
     * A file format that can be imported, used by the front ends to build their file chooser filters.
     *
     * @param description the human-readable description including the extensions
     * @param extensions  the file extensions without a leading dot
     */
    public record FileFormat(String description, List<String> extensions) {
        public FileFormat(String description, String... extensions) {
            this(description, Arrays.asList(extensions));
        }
    }

    public static final List<FileFormat> FILE_FORMATS = List.of(
            new FileFormat("Scalable Vector Graphics (.svg)", "svg"),
            new FileFormat("Autodesk CAD (.dxf)", "dxf"),
            new FileFormat("Carbide Create (.c2d)", "c2d"),
            new FileFormat("Eagle (.mnt, .mnb)", "mnt", "mnb"),
            new FileFormat("Excellon (.drl)", "drl"),
            new FileFormat("KiCad (.pos)", "pos"),
            new FileFormat("Gerber (.gbr)", "gbr"),
            new FileFormat("UGS design (.ugsd)", "ugsd"),
            new FileFormat("Stereolithography (.stl)", "stl"),
            new FileFormat("Portable Network Graphics (.png)", "png"),
            new FileFormat("JPEG (.jpg)", "jpg", "jpeg"),
            new FileFormat("Bitmap (.bmp)", "bmp")
    );

    public static final FileFormat ALL_SUPPORTED_FORMATS = new FileFormat("All supported formats",
            FILE_FORMATS.stream().flatMap(format -> format.extensions().stream()).toList());

    private static final List<String> RASTER_EXTENSIONS = List.of("png", "jpg", "jpeg", "bmp");

    private DesignFileImporter() {
    }

    public static boolean isSupportedFile(File file) {
        return ALL_SUPPORTED_FORMATS.extensions().stream().anyMatch(extension -> hasExtension(file, extension));
    }

    public static boolean isRasterFile(File file) {
        return RASTER_EXTENSIONS.stream().anyMatch(extension -> hasExtension(file, extension));
    }

    /**
     * Reads a vector design from the given file.
     *
     * @param file     the file to read
     * @param settings the settings used by readers that depend on user preferences
     * @return the design or an empty optional if the file format is not supported or could not be parsed
     */
    public static Optional<Design> readDesign(File file, Settings settings) {
        String name = file.getName();
        if (hasExtension(file, "svg")) {
            return new SvgReader().read(file);
        } else if (hasExtension(file, "dxf")) {
            return new DxfReader(settings).read(file);
        } else if (hasExtension(file, "c2d")) {
            return new C2dReader().read(file);
        } else if (hasExtension(file, "mnt") || hasExtension(file, "mnb")) {
            return new EaglePnpReader().read(file);
        } else if (hasExtension(file, "pos")) {
            return new KiCadPosReader().read(file);
        } else if (hasExtension(file, "ugsd")) {
            return new UgsDesignReader().read(file);
        } else if (StringUtils.endsWithIgnoreCase(name, "-drl.gbr")) {
            // Treat as a KiCad drill file
            return new GerberReader(true).read(file);
        } else if (hasExtension(file, "gbr")) {
            return new GerberReader().read(file);
        } else if (hasExtension(file, "drl")) {
            return new ExcellonReader().read(file);
        } else if (hasExtension(file, "stl")) {
            return new StlReader().read(file);
        }
        return Optional.empty();
    }

    /**
     * Imports the file into the design managed by the controller. Raster images are inserted as a
     * raster entity, all other formats are read as vector designs and their entities are added and
     * selected. Must be called on the thread that owns the design model.
     *
     * @param controller the controller of the design to import into
     * @param settings   the settings used by readers that depend on user preferences
     * @param file       the file to import
     * @throws DesignReaderException if the file could not be read
     */
    public static void importFile(Controller controller, Settings settings, File file) {
        if (isRasterFile(file)) {
            controller.getModel().insertEntity(new Raster(file));
            return;
        }

        Design design = readDesign(file, settings)
                .orElseThrow(() -> new DesignReaderException("Could not open: " + file.getName()));
        controller.setTool(Tool.SELECT);
        controller.addEntities(design.getEntities());
        controller.getSelectionManager().addSelection(design.getEntities());
        controller.repaintDrawing();
    }

    private static boolean hasExtension(File file, String extension) {
        return StringUtils.endsWithIgnoreCase(file.getName(), "." + extension);
    }
}
