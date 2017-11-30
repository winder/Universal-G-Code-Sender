/*
    Copyright 2017 Will Winder

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
package com.willwinder.ugp.gcodetools;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.Translator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import static com.willwinder.universalgcodesender.utils.SwingHelpers.selectedUnit;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugp.gcodetools//GcodeTiler//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "GcodeTilerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(
        category = GcodeTilerTopComponent.GcodeTilerCategory,
        id = GcodeTilerTopComponent.GcodeTilerActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "GcodeTiler",
        preferredID = "GcodeTilerTopComponent"
)
public final class GcodeTilerTopComponent extends TopComponent {
  public final static String GcodeTilerTitle = Localization.getString("platform.plugin.gcode-tools.tiler.window", lang);
  public final static String GcodeTilerTooltip = Localization.getString("platform.plugin.gcode-tools.tiler.tooltip", lang);
  public final static String GcodeTilerActionId = "com.willwinder.ugp.gcodetools.GcodeTilerTopComponent";
  public final static String GcodeTilerCategory = LocalizingService.CATEGORY_WINDOW;

  @OnStart
  public static class Localizer extends TopComponentLocalizer {
    public Localizer() {
      super(GcodeTilerCategory, GcodeTilerActionId, GcodeTilerTitle);
    }
  }

  private final BackendAPI backend;

  static String ERROR_GENERATING = "An error occurred generating tile program: ";
  static String ERROR_LOADING = "An error occurred loading generated tile program: ";

  private final JButton generateGcodeButton = new JButton(
          Localization.getString("platform.plugin.dowel-module.generate"));
  private final JButton exportGcodeButton = new JButton(
          Localization.getString("platform.plugin.dowel-module.export"));

  private final SpinnerNumberModel numCopiesX;
  private final SpinnerNumberModel numCopiesY;
  private final SpinnerNumberModel padding;
  private final JComboBox<String> units;

  // Temporary state.
  private static Double xWidth = null;
  private static Double yWidth = null;

  File outputFile = null;

  public GcodeTilerTopComponent() {
    setName(GcodeTilerTitle);
    setToolTipText(GcodeTilerTooltip);

    backend = CentralLookup.getDefault().lookup(BackendAPI.class);

    numCopiesX = new SpinnerNumberModel(3, 1, 1000, 1);
    numCopiesY = new SpinnerNumberModel(3, 1, 1000, 1);
    padding = new SpinnerNumberModel(5, 0, 1000, 0.1);
    units = new JComboBox<>(SwingHelpers.getUnitOptions());

    // Button callbacks
    generateGcodeButton.addActionListener(al -> generateGcode());
    exportGcodeButton.addActionListener(al -> exportGcode());

    // Change listeners
    numCopiesX.addChangeListener(l -> controlChangeListener());
    numCopiesY.addChangeListener(l -> controlChangeListener());
    padding.addChangeListener(l -> controlChangeListener());

    // Dowel settings
    setLayout(new MigLayout("fillx, wrap 4"));

    add(new JLabel(
            Localization.getString("platform.plugin.dowel-module.x")), "growx");
    add(new JSpinner(numCopiesX), "growx");

    add(new JLabel(Localization.getString("gcode.setting.units")), "growx");
    add(units, "growx");

    add(new JLabel(
            Localization.getString("platform.plugin.dowel-module.y")), "growx");
    add(new JSpinner(numCopiesY), "growx");

    add(new JLabel(
            Localization.getString("platform.plugin.gcode-tools.tiler.padding")), "growx");
    add(new JSpinner(padding), "growx");

    add(generateGcodeButton, "growx, span2");
    add(exportGcodeButton, "growx, span2");
  }

  private void controlChangeListener() {
    // Clear the cached output file if settings are changed.
    if (this.outputFile != null && this.outputFile.exists()) {
      this.outputFile.delete();
    }
  }

  private boolean loadDimensions() {
    if (backend.getProcessedGcodeFile() == null) {
      GUIHelpers.displayErrorDialog("Must load gcode file first.");
      return false;
    }

    Settings.FileStats fs = backend.getSettings().getFileStats();

    UnitUtils.Units u = selectedUnit(this.units.getSelectedIndex());
    Position min = fs.minCoordinate.getPositionIn(u);
    Position max = fs.maxCoordinate.getPositionIn(u);
    this.xWidth = max.x - min.x;
    this.yWidth = max.y - min.y;

    return true;
  }

  private void generateAndLoadGcode(File file) {
    try {
      if (this.outputFile == null) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
          double padding = SwingHelpers.getDouble(this.padding);
          double stepX = padding + this.xWidth;
          double stepY = padding + this.yWidth;

          // loop over offsets and call generateOneTile a bunch
          for (int x = 0; x < SwingHelpers.getInt(this.numCopiesX); x++) {
            for (int y = 0; y < SwingHelpers.getInt(this.numCopiesY); y++) {
              this.generateOneTile(
                  (x * stepX),
                  (y * stepY),
                  writer);
            }
          }
        }
      }
      backend.setGcodeFile(file);
    } catch (IOException e) {
      GUIHelpers.displayErrorDialog(ERROR_GENERATING + e.getLocalizedMessage());
    } catch (Exception e) {
      GUIHelpers.displayErrorDialog(ERROR_LOADING + e.getLocalizedMessage());
    }
  }

  private void generateGcode() {
    if (!loadDimensions()) return;

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
    if (!loadDimensions()) return;
    String sourceDir = backend.getSettings().getLastOpenedFilename();
    SwingHelpers
          .createFile(sourceDir)
          .ifPresent(file -> generateAndLoadGcode(file));
  }

  // Helpers to actually process the gcode
  private void applyTranslation(String command, GcodeParser parser, PrintWriter output) throws GcodeParserException {
    if (StringUtils.isEmpty(command)) return;

    Collection<String> lines = parser.preprocessCommand(command, parser.getCurrentState());
    for(String processedLine : lines) {
      output.println(processedLine);
    }
    parser.addCommand(command);
  }

  private void generateOneTile(double offsetX, double offsetY, PrintWriter output) {
    String gcodeFile = backend.getGcodeFile().getAbsolutePath();

    UnitUtils.Units u = selectedUnit(this.units.getSelectedIndex());

    GcodeParser parser = new GcodeParser();
    parser.addCommandProcessor(new Translator(new Position(offsetX, offsetY, 0.0, u)));
    parser.addCommandProcessor(new M30Processor());
   
    output.println(GcodeUtils.unitCommand(u) + "G90");
    output.println("G0X" + offsetX + "Y" + offsetY);

    try {
      File file = new File(gcodeFile);
      try {
          try (GcodeStreamReader gsr = new GcodeStreamReader(file)) {
            while (gsr.getNumRowsRemaining() > 0) {
              GcodeCommand next = gsr.getNextCommand();
              applyTranslation(next.getCommandString(), parser, output);
            }
          }
      } catch (GcodeStreamReader.NotGcodeStreamFile e) {
        List<String> linesInFile;
        try (
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fstream);
            BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis))) {
          String line;
          while ((line = fileStream.readLine()) != null) {
            applyTranslation(line, parser, output);
          }
        }
      }
    } catch (GcodeParserException|IOException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  @Override
  public void componentOpened() {
  }

  @Override
  public void componentClosed() {
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
  }
}
