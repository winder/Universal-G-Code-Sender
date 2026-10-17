/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.io.DesignFileImporter;
import com.willwinder.ugs.designer.io.DesignReaderException;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Opens a file chooser and imports the chosen file into the current design. Parsing the file may be
 * slow for large models so it runs on a background thread, while the model mutation is dispatched
 * back to the JavaFX thread which owns the designer model in the FX visualizer.
 */
public class DesignImportAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(DesignImportAction.class.getName());
    public static final String ICON_BASE = "icons/import.svg";

    public DesignImportAction() {
        super(Localization.getString("platform.designer.import"), Localization.getString("platform.designer.import"), Localization.getString("actions.category.designer"), ICON_BASE);
    }

    @Override
    public void handleAction(ActionEvent event) {
        Window owner = resolveWindow(event);
        Settings settings = LookupService.lookup(BackendAPI.class).getSettings();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Localization.getString("platform.designer.import"));
        fileChooser.setInitialDirectory(resolveInitialDirectory(settings));
        fileChooser.getExtensionFilters().addAll(createExtensionFilters());

        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return;
        }

        settings.setLastWorkingDirectory(file.getParent());
        ThreadHelper.invokeLater(() -> importFile(owner, settings, file));
    }

    private void importFile(Window owner, Settings settings, File file) {
        try {
            if (DesignFileImporter.isRasterFile(file)) {
                Raster raster = new Raster(file);
                Platform.runLater(() -> ControllerFactory.getController().getModel().insertEntity(raster));
                return;
            }

            Design design = DesignFileImporter.readDesign(file, settings)
                    .orElseThrow(() -> new DesignReaderException("Could not open: " + file.getName()));
            Platform.runLater(() -> addToDesign(design));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not import file " + file.getName(), e);
            Platform.runLater(() -> showError(owner, file));
        }
    }

    private static void addToDesign(Design design) {
        Controller controller = ControllerFactory.getController();
        controller.setTool(Tool.SELECT);
        controller.addEntities(design.getEntities());
        controller.getSelectionManager().addSelection(design.getEntities());
    }

    private static void showError(Window owner, File file) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Could not import file " + file.getName(), ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(Localization.getString("platform.designer.import"));
        alert.initOwner(owner);
        alert.showAndWait();
    }

    static List<FileChooser.ExtensionFilter> createExtensionFilters() {
        return Stream.concat(Stream.of(DesignFileImporter.ALL_SUPPORTED_FORMATS), DesignFileImporter.FILE_FORMATS.stream())
                .map(format -> new FileChooser.ExtensionFilter(format.description(),
                        format.extensions().stream().map(extension -> "*." + extension).toList()))
                .toList();
    }

    private static File resolveInitialDirectory(Settings settings) {
        File directory = new File(settings.getLastWorkingDirectory());
        return directory.isDirectory() ? directory : null;
    }

    private static Window resolveWindow(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node node && node.getScene() != null) {
            return node.getScene().getWindow();
        }
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }
}
