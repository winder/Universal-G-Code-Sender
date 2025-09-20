package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.universalgcodesender.fx.component.InfoTooltip;
import com.willwinder.universalgcodesender.fx.component.SwitchButton;
import com.willwinder.universalgcodesender.fx.component.TabBar;
import com.willwinder.universalgcodesender.fx.dialog.ProcessorConfigDialog;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.gcode.util.CommandProcessorLoader;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.ControllerSettings;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessorConfigPane extends VBox {

    private final Map<String, VBox> tabs = new ConcurrentHashMap<>();
    private final VBox content;
    private static final Logger LOGGER = Logger.getLogger(ProcessorConfigPane.class.getName());

    public ProcessorConfigPane() {
        setSpacing(20);
        content = new VBox();
        VBox.setMargin(content, new Insets(0, 10, 0, 10));

        addTitleSection();
        addVerboseLoggingSection();
    }

    private void addVerboseLoggingSection() {
        TabBar tabBar = new TabBar();
        FirmwareUtils.getConfigFiles().values().forEach(config -> {
            VBox content = new VBox();
            addSettings(content, config, config.loader.getProcessorConfigs().Front);
            addSettings(content, config, config.loader.getProcessorConfigs().Custom);
            addSettings(content, config, config.loader.getProcessorConfigs().End);
            tabBar.addTab(config.loader.getName());
            tabs.put(config.loader.getName(), content);
        });

        content.setFillWidth(true);
        tabBar.selectedTabProperty().addListener((observable, oldValue, newValue) -> setSelectedTab(newValue.getText()));
        setSelectedTab(tabs.keySet().stream().findFirst().orElse(null));
        getChildren().add(tabBar);
        getChildren().add(content);
    }

    private void setSelectedTab(String name) {
        content.getChildren().clear();

        if (name != null) {
            content.getChildren().add(tabs.get(name));
        }
    }

    private void addSettings(VBox content, FirmwareUtils.ConfigTuple config, List<ControllerSettings.ProcessorConfig> processorConfigs) {
        if (processorConfigs.isEmpty()) {
            return;
        }

        processorConfigs.forEach(processorConfig -> {
            content.getChildren().add(createRow(config, processorConfig));
            content.getChildren().add(new Separator());
        });
    }

    private HBox createRow(FirmwareUtils.ConfigTuple configTuple, ControllerSettings.ProcessorConfig config) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setMinHeight(50);
        SwitchButton switchButton = new SwitchButton();
        switchButton.setSelected(config.enabled);
        switchButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            config.enabled = isSelected;
            save(configTuple);
        });
        switchButton.setDisable(!config.optional);

        hbox.getChildren().add(switchButton);
        Label label = new Label(Localization.getString(config.name));
        hbox.getChildren().add(label);

        InfoTooltip tooltip = new InfoTooltip(CommandProcessorLoader.getHelpForConfig(config));
        HBox.setMargin(tooltip, new Insets(0, 5, 0, 5));
        hbox.getChildren().add(tooltip);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().add(spacer);

        if(config.args != null) {
            Button settingsButton = new Button(Localization.getString("settings"), SvgLoader.loadImageIcon("icons/settings.svg", 16).orElse(null));
            settingsButton.setOnAction((e) -> {
                ProcessorConfigDialog processorConfigDialog = new ProcessorConfigDialog(getParent().getScene().getWindow(), config);
                processorConfigDialog.showAndWait();
                config.args = processorConfigDialog.getConfig();
                save(configTuple);
            });
            hbox.getChildren().add(settingsButton);
        }


        HBox.setMargin(switchButton, new Insets(0, 10, 0, 0));
        HBox.setHgrow(switchButton, Priority.NEVER);
        HBox.setHgrow(label, Priority.ALWAYS);

        return hbox;
    }

    private void save(FirmwareUtils.ConfigTuple configTuple) {
        try {
            FirmwareUtils.save(configTuple.file, configTuple.loader);
        } catch (IOException ex) {
            GUIHelpers.displayErrorDialog("Problem saving controller config: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.processor"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }
}
