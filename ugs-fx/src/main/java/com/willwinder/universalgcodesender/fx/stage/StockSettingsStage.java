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
package com.willwinder.universalgcodesender.fx.stage;

import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.GcodeLines;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.CutSegment;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.StockDefinition;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.StockSpec;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ToolResolver;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.fx.service.ProgramToolResolution;
import com.willwinder.universalgcodesender.fx.service.StockSpecs;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings for the block of material the stock simulation cuts into: whether it is derived from
 * the program or given by hand, its position and size when given by hand, and how it is drawn.
 */
public class StockSettingsStage extends Stage {
    private static final Logger LOGGER = Logger.getLogger(StockSettingsStage.class.getName());

    private final ComboBox<StockSpec.Mode> mode = new ComboBox<>(FXCollections.observableArrayList(StockSpec.Mode.values()));
    private final UnitTextField minX;
    private final UnitTextField minY;
    private final UnitTextField width;
    private final UnitTextField length;
    private final UnitTextField top;
    private final UnitTextField thickness;
    private final SwitchButton showStock = new SwitchButton();
    private final ColorPicker color;
    private final ColorPicker deepColor;
    private final Label hint = new Label();
    private final List<UnitTextField> manualFields;
    private final Button useProgramBounds = new Button("Use program bounds");

    public StockSettingsStage(Window owner) {
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle("Stock settings");
        VisualizerSettings settings = VisualizerSettings.getInstance();

        mode.setValue(StockSpecs.parseMode(settings.stockModeProperty().get()));
        mode.setMaxWidth(Double.MAX_VALUE);
        mode.setConverter(new StringConverter<>() {
            @Override
            public String toString(StockSpec.Mode value) {
                return value == null ? "" : value.displayName();
            }

            @Override
            public StockSpec.Mode fromString(String text) {
                return StockSpec.Mode.AUTOMATIC;
            }
        });
        minX = field(settings.stockMinXProperty().get());
        minY = field(settings.stockMinYProperty().get());
        width = field(settings.stockWidthProperty().get());
        length = field(settings.stockLengthProperty().get());
        top = field(settings.stockTopProperty().get());
        thickness = field(settings.stockThicknessProperty().get());
        manualFields = List.of(minX, minY, width, length, top, thickness);
        showStock.selectedProperty().set(settings.showStockProperty().get());
        color = new ColorPicker(Color.web(settings.colorStockProperty().get()));
        color.setMinHeight(28);
        deepColor = new ColorPicker(Color.web(settings.colorStockDeepProperty().get()));
        deepColor.setMinHeight(28);
        hint.setWrapText(true);
        hint.setStyle("-fx-opacity: 0.75;");
        useProgramBounds.setOnAction(event -> fillFromProgram());

        mode.valueProperty().addListener((observable, was, value) -> updateMode());
        updateMode();

        setScene(createScene());
        setWidth(420);
        setHeight(640);
        setResizable(true);
        setOnShowing(event -> centerOnOwner());
    }

    private Scene createScene() {
        VBox form = new VBox(4,
                new SettingsRow("Show stock", showStock),
                new SettingsRow("Stock color", color),
                new SettingsRow("Deep cut color", deepColor),
                new Separator(),
                new SettingsRow("Stock size", "Automatic derives the block from the program: the cutting moves plus a margin, with the top at Z zero and the bottom at the deepest cut, assuming that cut goes through. Manual uses the size below.", mode),
                hint,
                new Separator(),
                new SettingsRow("X origin", "The X coordinate of the block's near left corner in the program's coordinates.", minX),
                new SettingsRow("Y origin", minY),
                new SettingsRow("Width (X)", width),
                new SettingsRow("Length (Y)", length),
                new SettingsRow("Top Z", "Where the top surface of the material is. Usually zero, with cuts going negative.", top),
                new SettingsRow("Thickness", thickness),
                useProgramBounds);
        form.setPadding(new Insets(16));

        Button cancel = new Button("Cancel");
        cancel.setOnAction(event -> close());
        Button apply = new Button("Apply");
        apply.setDefaultButton(true);
        apply.setOnAction(event -> onApply());
        ButtonBox buttons = new ButtonBox();
        ButtonBox.setButtonData(cancel, ButtonBox.ButtonData.CANCEL_CLOSE);
        ButtonBox.setButtonData(apply, ButtonBox.ButtonData.OK_DONE);
        buttons.getButtons().addAll(cancel, apply);

        BorderPane root = new BorderPane(form);
        root.setBottom(buttons);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        return scene;
    }

    private void updateMode() {
        boolean manual = mode.getValue() == StockSpec.Mode.MANUAL;
        manualFields.forEach(field -> field.setDisable(!manual));
        useProgramBounds.setDisable(!manual);
        hint.setText(manual
                ? "The simulation cuts the block given below. Cuts outside it are ignored, and cuts below it stop at the bottom."
                : "The block is derived from the loaded program: its cutting moves plus a margin, with the bottom at the deepest cut.");
    }

    /**
     * Fills the manual fields with the block that automatic mode would derive, as a starting point.
     */
    private void fillFromProgram() {
        File file = LookupService.lookup(BackendAPI.class).getGcodeFile();
        if (file == null || !file.exists()) {
            hint.setText("No program is loaded to take the bounds from.");
            return;
        }
        try {
            ToolResolver tools = ProgramToolResolution.forProgram(file, VisualizerSettings.getInstance().stockDefaultToolIdProperty().get());
            List<CutSegment> cuts = CutSegment.fromLineSegments(GcodeLines.parseSegments(file));
            Optional<StockDefinition> stock = StockDefinition.resolve(StockSpec.AUTOMATIC, cuts, tools);
            if (stock.isEmpty()) {
                hint.setText("The program has no cutting moves to take the bounds from.");
                return;
            }
            Bounds3 bounds = stock.get().bounds();
            minX.setValue(round(bounds.minX()));
            minY.setValue(round(bounds.minY()));
            width.setValue(round(bounds.width()));
            length.setValue(round(bounds.height()));
            top.setValue(round(bounds.maxZ()));
            thickness.setValue(round(bounds.depth()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not derive the stock from " + file, e);
            hint.setText("Could not read the program: " + e.getMessage());
        }
    }

    private void onApply() {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.showStockProperty().set(showStock.selectedProperty().get());
        settings.colorStockProperty().set(toWeb(color.getValue()));
        settings.colorStockDeepProperty().set(toWeb(deepColor.getValue()));
        settings.stockMinXProperty().set(minX.getValue());
        settings.stockMinYProperty().set(minY.getValue());
        settings.stockWidthProperty().set(Math.max(0, width.getValue()));
        settings.stockLengthProperty().set(Math.max(0, length.getValue()));
        settings.stockTopProperty().set(top.getValue());
        settings.stockThicknessProperty().set(Math.max(0, thickness.getValue()));
        // The mode is set last so a single reload sees the finished size
        settings.stockModeProperty().set(mode.getValue().name());
        close();
    }

    private static String toWeb(Color value) {
        return String.format("#%02X%02X%02X%02X",
                Math.round(value.getRed() * 255), Math.round(value.getGreen() * 255),
                Math.round(value.getBlue() * 255), Math.round(value.getOpacity() * 255));
    }

    private static double round(double value) {
        return Math.round(value * 1000) / 1000.0;
    }

    private static UnitTextField field(double value) {
        UnitTextField field = new UnitTextField(new UnitValue(Unit.MM, value), Unit.MM);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private void centerOnOwner() {
        Window owner = getOwner();
        if (owner != null) {
            setX(owner.getX() + owner.getWidth() / 2 - getWidth() / 2);
            setY(owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
        }
    }
}
