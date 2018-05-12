/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.ugs.platform.dowel;

import static com.willwinder.universalgcodesender.utils.SwingHelpers.getDouble;
import static com.willwinder.universalgcodesender.utils.SwingHelpers.getInt;
import static com.willwinder.universalgcodesender.utils.SwingHelpers.selectedUnit;
import static com.willwinder.universalgcodesender.utils.SwingHelpers.unitIdx;

import com.google.gson.Gson;
import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.SwingHelpers;

import net.miginfocom.swing.MigLayout;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.*;
import javax.swing.border.Border;
import org.openide.modules.OnStart;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.platform.dowel//Dowel//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DowelTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(
        category = DowelTopComponent.DowelCategory,
        id = DowelTopComponent.DowelActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "Dowel",
        preferredID = "DowelTopComponent"
)
public final class DowelTopComponent extends TopComponent {
  public final static String DowelTitle = Localization.getString("platform.window.dowel-module", lang);
  public final static String DowelTooltip = Localization.getString("platform.window.dowel-module.tooltip", lang);
  public final static String DowelActionId = "com.willwinder.ugs.platform.dowel.DowelTopComponent";
  public final static String DowelCategory = LocalizingService.CATEGORY_WINDOW;

  private final static String JSON_PROPERTY = "dowel_settings_json";
  private final static String ERROR_GENERATING = "An error occurred generating dowel program: ";
  private final static String ERROR_LOADING = "An error occurred loading generated dowel program: ";

  private final BackendAPI backend;

  private final JButton generateGcodeButton = new JButton(
          Localization.getString("platform.plugin.dowel-module.generate"));
  private final JButton exportGcodeButton = new JButton(
          Localization.getString("platform.plugin.dowel-module.export"));

  private final SpinnerNumberModel numDowelsX;
  private final SpinnerNumberModel numDowelsY;
  private final SpinnerNumberModel dowelDiameter;
  private final SpinnerNumberModel dowelLength;
  private final SpinnerNumberModel bitDiameter;
  private final SpinnerNumberModel feed;
  private final SpinnerNumberModel cutDepth;
  private final SpinnerNumberModel safetyHeight;
  private final JComboBox<String> units;

  private static final Gson GSON = new Gson();

  private final DowelGenerator generator;
  private final DowelPreview preview;

  @OnStart
  public static class Localizer extends TopComponentLocalizer {
    public Localizer() {
      super(DowelCategory, DowelActionId, DowelTitle);
    }
  }

  public DowelTopComponent() {
    setName(DowelTitle);
    setToolTipText(DowelTooltip);

    backend = CentralLookup.getDefault().lookup(BackendAPI.class);

    double doubleSpinner = 1000000;
    int intSpinner = 1000000;

    numDowelsX = new SpinnerNumberModel(3, 1, intSpinner, 1);
    numDowelsY = new SpinnerNumberModel(3, 1, intSpinner, 1);
    dowelDiameter = new SpinnerNumberModel(5, 0, doubleSpinner, 0.1);
    dowelLength = new SpinnerNumberModel(10, 0, doubleSpinner, 0.1);
    bitDiameter = new SpinnerNumberModel(3.17, 0, doubleSpinner, 0.1);
    feed = new SpinnerNumberModel(100, 1, doubleSpinner, 1);
    cutDepth = new SpinnerNumberModel(2, 0, doubleSpinner, 0.1);
    safetyHeight = new SpinnerNumberModel(5, 0, doubleSpinner, 0.1);

    units = new JComboBox<>(SwingHelpers.getUnitOptions());

    generator = new DowelGenerator(getSettings());
    preview = new DowelPreview(
            Localization.getString("platform.plugin.dowel-module.preview"),generator);

    setupGui();
  }

  private void setupGui() {
    // Button callbacks
    generateGcodeButton.addActionListener(al -> generateGcode());
    exportGcodeButton.addActionListener(al -> exportGcode());

    // Change listeners
    numDowelsX.addChangeListener(l -> controlChangeListener());
    numDowelsY.addChangeListener(l -> controlChangeListener());
    dowelDiameter.addChangeListener(l -> controlChangeListener());
    dowelLength.addChangeListener(l -> controlChangeListener());
    bitDiameter.addChangeListener(l -> controlChangeListener());
    feed.addChangeListener(l -> controlChangeListener());
    cutDepth.addChangeListener(l -> controlChangeListener());
    safetyHeight.addChangeListener(l -> controlChangeListener());
    units.addActionListener(l -> controlChangeListener());
    Border blackline = BorderFactory.createLineBorder(Color.black);

    // Dowel settings
    JPanel dowelPanel = new JPanel();
    dowelPanel.setBorder(BorderFactory.createTitledBorder(blackline,
            Localization.getString("platform.plugin.dowel-module.dowel")));
    dowelPanel.setLayout(new MigLayout("fillx, wrap 4"));

    dowelPanel.add(new JLabel(
            Localization.getString("platform.plugin.dowel-module.x")), "growx");
    dowelPanel.add(new JSpinner(numDowelsX), "growx");

    dowelPanel.add(new JLabel(
            Localization.getString("platform.plugin.dowel-module.diameter")), "growx");
    dowelPanel.add(new JSpinner(dowelDiameter), "growx");

    dowelPanel.add(new JLabel(
            Localization.getString("platform.plugin.dowel-module.y")), "growx");
    dowelPanel.add(new JSpinner(numDowelsY), "growx");

    dowelPanel.add(new JLabel(
            Localization.getString("platform.plugin.dowel-module.length")), "growx");
    dowelPanel.add(new JSpinner(dowelLength), "growx");

    dowelPanel.add(generateGcodeButton, "growx, span 2");
    dowelPanel.add(exportGcodeButton, "growx, span 2");

    // Gcode settings
    JPanel cutPanel = new JPanel();
    cutPanel.setBorder(BorderFactory.createTitledBorder(blackline,
            Localization.getString("mainWindow.swing.settingsMenu")));
    cutPanel.setLayout(new MigLayout("fillx, wrap 4"));

    cutPanel.add(new JLabel(Localization.getString("gcode.setting.units")), "growx");
    cutPanel.add(units, "growx");

    cutPanel.add(new JLabel(
            Localization.getString("gcode.setting.feed")), "growx");
    cutPanel.add(new JSpinner(feed), "growx");

    cutPanel.add(new JLabel(
            Localization.getString("gcode.setting.endmill-diameter")), "growx");
    cutPanel.add(new JSpinner(bitDiameter), "growx");

    cutPanel.add(new JLabel(
            Localization.getString("gcode.setting.cut-depth")), "growx");
    cutPanel.add(new JSpinner(cutDepth), "growx");

    cutPanel.add(new JLabel(
            Localization.getString("gcode.setting.safety-height")), "growx");
    cutPanel.add(new JSpinner(safetyHeight), "growx");

    // Put it all together
    setLayout(new MigLayout("fillx, wrap 2"));
    add(dowelPanel, "grow");
    add(cutPanel, "grow");
  }

  private void generateAndLoadGcode(File file) {
    try {
      try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
        generator.generate(writer);
      }
      backend.setGcodeFile(file);
    } catch (IOException e) {
      GUIHelpers.displayErrorDialog(ERROR_GENERATING + e.getLocalizedMessage());
    } catch (Exception e) {
      GUIHelpers.displayErrorDialog(ERROR_LOADING + e.getLocalizedMessage());
    }

  }

  private void generateGcode() {
    Path path = null;
    try {
      path = Files.createTempFile("dowel_program", ".gcode");
      File file = path.toFile();
      generateAndLoadGcode(file);
    } catch (IOException e) {
      GUIHelpers.displayErrorDialog(ERROR_LOADING + e.getLocalizedMessage());
    }
  }

  private void exportGcode() {
    String sourceDir = backend.getSettings().getLastOpenedFilename();
    SwingHelpers
          .createFile(sourceDir)
          .ifPresent(file -> generateAndLoadGcode(file));
  }

  private void controlChangeListener() {
    this.generator.setSettings(getSettings());
  }

  public DowelSettings getSettings() {
    return new DowelSettings(
        getInt(this.numDowelsX),
        getInt(this.numDowelsY),
        getDouble(this.dowelDiameter),
        getDouble(this.dowelLength),
        getDouble(this.bitDiameter),
        getDouble(this.feed),
        getDouble(this.cutDepth),
        getDouble(this.safetyHeight),
        selectedUnit(this.units.getSelectedIndex()));
  }

  @Override
  public void componentOpened() {
    RenderableUtils.registerRenderable(preview);
  }

  @Override
  public void componentClosed() {
    RenderableUtils.removeRenderable(preview);
  }

  public void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    p.setProperty(JSON_PROPERTY, GSON.toJson(getSettings()));
  }

  public void readProperties(java.util.Properties p) {
    if (p.containsKey(JSON_PROPERTY)) {
      String json = p.getProperty(JSON_PROPERTY);
      try {
        DowelSettings ds = new Gson().fromJson(json, DowelSettings.class);

        this.numDowelsX.setValue(ds.getNumDowelsX());
        this.numDowelsY.setValue(ds.getNumDowelsY());
        this.dowelDiameter.setValue(ds.getDowelDiameter());
        this.dowelLength.setValue(ds.getDowelLength());
        this.bitDiameter.setValue(ds.getBitDiameter());
        this.feed.setValue(ds.getFeed());
        this.cutDepth.setValue(ds.getCutDepth());
        this.safetyHeight.setValue(ds.getSafetyHeight());
        this.units.setSelectedIndex(unitIdx(ds.getUnits()));
      } catch (Exception e) {
        GUIHelpers.displayErrorDialog("Problem loading Dowel Settings, defaults have been restored.");
      }
    }
  }
}